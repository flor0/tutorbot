package ch.frupp.tutorbot.course.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDto {
    public String id;
    public String name;
    public String summary;

    public static TopicDto from(Topic t) {
        if (t == null) return null;
        return new TopicDto(t.getId(), t.getName(), t.getSummary());
    }

    public Topic toTopic() {
        Topic t = new Topic();
        t.setId(this.id);
        t.setName(this.name);
        t.setSummary(this.summary);
        // quizIds are backend-only and intentionally not set from the DTO
        return t;
    }
}
