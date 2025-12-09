package ch.frupp.lecturevault.ai.dataprocessing;

public record IngestionResult(Integer inputTokens, Integer outputTokens, Integer totalTokens) {

    public static IngestionResult empty() {
        return new IngestionResult(0, 0, 0);
    }
}

