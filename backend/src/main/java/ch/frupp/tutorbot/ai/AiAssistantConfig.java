package ch.frupp.tutorbot.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Configuration
public class AiAssistantConfig {

    Logger logger = LoggerFactory.getLogger(AiAssistantConfig.class);

    @Bean ChatModel chatModel() {
        return OllamaChatModel.builder()
                .modelName("gemma3:12b")
                .baseUrl("http://localhost:11434")
                .logger(logger)
                .logResponses(true)
                .logRequests(true)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        // This retriever will find the N most relevant text segments from your store
        // Pass an explicit EmbeddingModel to avoid ambiguity when multiple models exist on the classpath.
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5) // Retrieve, for example, 5 most relevant segments
//                .minScore(0.75)
                .dynamicFilter(query -> {
                    // TODO: Filter by course as well
                    String userId = query.metadata().invocationParameters().get("userid");
                    return metadataKey("userid").isEqualTo(userId);
                })
                .build();
    }

    // Create an AiAssistant only when a ChatModel is available AND there isn't already
    // an AiAssistant bean (the library may auto-register one via @AiService).
    @Bean
    public AiAssistant aiAssistant(ChatModel chatModel, ContentRetriever contentRetriever) {
        return AiServices.builder(AiAssistant.class)
                .chatModel(chatModel) // Autowired from application.properties
                .contentRetriever(contentRetriever)
                // Optional: .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) for conversational context
                .build();
    }
}
