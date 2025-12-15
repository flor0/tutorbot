package ch.frupp.tutorbot.ai.dataprocessing;

public record IngestionResult(Integer inputTokens, Integer outputTokens, Integer totalTokens) {

    public static IngestionResult empty() {
        return new IngestionResult(0, 0, 0);
    }
}

