package ch.frupp.tutorbot.course.material;

import ch.frupp.tutorbot.ai.dataprocessing.IngestionResult;
import ch.frupp.tutorbot.ai.dataprocessing.PDFIngestionService;
import ch.frupp.tutorbot.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/courses")
public class CourseMaterialController {

    private final PDFIngestionService ingestionService;
    private final Logger logger = LoggerFactory.getLogger(CourseMaterialController.class);

    public CourseMaterialController(PDFIngestionService ingestionService, CourseMaterialRepository courseMaterialRepository) {
        this.ingestionService = ingestionService;
    }


    @GetMapping("/{courseId}/materials")
    public ResponseEntity<List<CourseMaterial>> findAllByCourseId(@PathVariable String courseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<CourseMaterial> materials = ingestionService.getMaterialsByUserAndCourse(user, courseId);
        return ResponseEntity.ofNullable(materials);
    }

    @PostMapping(path = "/{courseId}/materials/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadPdf(@PathVariable String courseId, @RequestParam("file") MultipartFile file, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();

        try {
            // Ingest the file into the RAG vector store
            IngestionResult result = ingestionService.ingestUploadedPdf(file, userId, courseId);
            Map<String, Object> body = new HashMap<>();
            body.put("inputTokens", result.inputTokens());
            body.put("outputTokens", result.outputTokens());
            body.put("totalTokens", result.totalTokens());
            return ResponseEntity.ok(body);
        } catch (IOException e) {
            logger.error("Failed to ingest uploaded PDF for userId={} courseId={}", userId, courseId, e);
            return ResponseEntity.status(500).body("Failed to process uploaded PDF");
        }
    }

    @DeleteMapping("/{courseId}/materials/{materialId}")
    public ResponseEntity<?> deleteMaterial(@PathVariable String courseId, @PathVariable String materialId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();
        // Fetch the relevant CourseMaterial for deletion from the repository
        try {
            ingestionService.deleteMaterialById(user, materialId);
        } catch (NoSuchElementException e) {
            logger.error("Failed to delete Material with id={} by user={}", materialId, userId, e);
            return ResponseEntity.status(404).body("No such course material found");
        } catch (Exception e) {
            logger.error("Failed to delete Material with id={} by user={}", materialId, user, e);
            return ResponseEntity.status(403).body("User doesn't own this course material");
        }
        return ResponseEntity.ok().build();
    }
}
