package ch.frupp.tutorbot.course.topic;

public record TopicDto(Integer id, String name, String summary) {
    public static TopicDto fromTopic(Topic t) {
        if (t == null) return null;
        return new TopicDto(t.getId(), t.getName(), t.getSummary());
    }

    public Topic toTopic() {
        return Topic.builder()
                .id(this.id)
                .name(this.name)
                .summary(this.summary)
                .build();
    }
}
