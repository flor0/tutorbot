package ch.frupp.tutorbot.ai.dataprocessing;

import ch.frupp.tutorbot.course.material.CourseMaterial;
import ch.frupp.tutorbot.course.material.CourseMaterialRepository;
import ch.frupp.tutorbot.user.User;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.logical.And;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class PDFIngestionService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final Logger logger = LoggerFactory.getLogger(PDFIngestionService.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    // Resolve the 'pdfs' directory relative to the application's working directory.
    private final Path pdfsDirectory = Paths.get(System.getProperty("user.dir")).resolve("pdfs");

    public PDFIngestionService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel, CourseMaterialRepository courseMaterialRepository) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.courseMaterialRepository = courseMaterialRepository;
    }

    @PostConstruct
    public void postStartUp() {


        logger.info("Starting textbook ingestion...");

        Path path = pdfsDirectory; // e.g., '<project-root>/pdfs'

        if (!Files.exists(path)) {
            logger.warn("PDFs directory '{}' does not exist. Skipping ingestion.", path.toAbsolutePath());
            return;
        }

        // Load PDF documents
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(path);

        // Ingest into the embedding store (splits, embeds, and stores) with a 'system' userid
        ingestDocuments(documents, "system", null);
    }

    private void clearEmbeddingStorage() {
        // Clear the mongodb backend and pgvector embedding store
        logger.warn("Clearing embedding store for pdfs...");
        courseMaterialRepository.deleteAll();
        embeddingStore.removeAll();
    }

    public IngestionResult ingestDocuments(List<Document> documents, String userId, String courseId) {
        if (documents == null || documents.isEmpty()) {
            logger.info("No documents provided for ingestion (userId={}).", userId);
            return IngestionResult.empty();
        }

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentTransformer(document -> {
                    if (userId != null) {
                        document.metadata().put("userid", userId);
                    }
                    if (courseId != null) {
                        document.metadata().put("courseid", courseId);
                    }
                    return document;
                })
                .build();

        dev.langchain4j.store.embedding.IngestionResult result = ingestor.ingest(documents);
        TokenUsage usage = result.tokenUsage();

        Integer inputCount = usage.inputTokenCount();
        Integer outputCount = usage.outputTokenCount();
        Integer totalCount = usage.totalTokenCount();

        logger.info("Document ingestion complete for userId={}: Input tokens: {} Output tokens: {} Total tokens: {}",
                userId, inputCount, outputCount, totalCount);

        return new IngestionResult(inputCount, outputCount, totalCount);
    }

    public IngestionResult ingestUploadedPdf(MultipartFile file, Integer userId, String courseId) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("Empty upload received for ingestion (userId={}).", userId);
            return IngestionResult.empty();
        }

        // Create a CourseMaterial db entry to keep track of the file
        CourseMaterial courseMaterial = new CourseMaterial(userId, courseId, file.getOriginalFilename());
        courseMaterialRepository.save(courseMaterial);
        logger.info("Course db entry created: {}", courseMaterial);

        Path tempDir = Files.createTempDirectory("pdfs");
        Path tempFile = Files.createTempFile(tempDir, "uploaded", ".pdf");
        try {
            file.transferTo(tempFile.toFile());
            List<Document> documents = FileSystemDocumentLoader.loadDocuments(tempDir);
            logger.info("Course successfully ingested: {}", courseMaterial);
            return ingestDocuments(documents, String.valueOf(userId), courseId);
        } finally {
            try { Files.deleteIfExists(tempDir); } catch (IOException ignored) {}
        }
    }

    public List<CourseMaterial> getMaterialsByUserAndCourse(User user, String courseId) {
        var materials = courseMaterialRepository.findByUserIdAndCourseId(user.getId(), courseId);
        logger.info("Course materials found for user {} : {}", user, materials);
        return courseMaterialRepository.findByUserIdAndCourseId(user.getId(), courseId);
    }

    public void deleteMaterialById(User user, String materialId) throws Exception {
        CourseMaterial courseMaterial = courseMaterialRepository.findById(materialId).orElseThrow();
        // Validation: Check user owns material
        if (user.getId() == courseMaterial.getUserId()) {
            this.deleteMaterial(courseMaterial);
        } else {
            throw new Exception("The Course Material to be deleted does not belong to the user {}");
        }

    }

    public void deleteMaterial(CourseMaterial courseMaterial) {

        // Remove from the embedding store
        String userId = String.valueOf(courseMaterial.getUserId());
        String courseId = courseMaterial.getCourseId();
        embeddingStore.removeAll(new And(
                new IsEqualTo("userid", userId),
                new IsEqualTo("courseid", courseId)
        ));

        // Remove from the mongo db
        courseMaterialRepository.delete(courseMaterial);

        logger.info("Course db entry and embeddings deleted: {}", courseMaterial);

    }

}
