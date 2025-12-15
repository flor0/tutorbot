package ch.frupp.tutorbot.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGConfiguration {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // You can use an in-memory store for development/simple cases
        // For production, consider persistent vector databases like Pinecone, Chroma, etc.
        // which would require their respective LangChain4j integrations and configuration.
//        return new InMemoryEmbeddingStore<>();
        // TODO: After setting up environments, set these dynamically from ENV
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("lecturevault")
                .user("postgres")
                .password("secret")
                .table("rag_embeddings")
                .dimension(embeddingModel().dimension())
                .build();

    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
        // Or: AllMiniLmL6V2QuantizedEmbeddingModel.builder().build(); // Smaller/faster
    }

}
