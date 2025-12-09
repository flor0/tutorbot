package ch.frupp.lecturevault.course.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "topics")
@CompoundIndex(def = "{ 'userId' : 1, 'name' : 1 }", unique = true)
public class Topic {

    @Id
    private String id;

    // Owner user id (store as string to be generic across JPA vs other ids)
    private Integer userId;

    private String courseId;

    // A short machine-friendly name / keyword for the topic, e.g. "Integrals"
    private String name;

    // A short summary describing the topic
    private String summary;

    // References to quizzes related to this topic (store quiz document ids)
    private List<String> quizIds;

    // Convenience method to add a quiz id
    public void addQuizId(String quizId) {
        if (this.quizIds == null) this.quizIds = new ArrayList<>();
        this.quizIds.add(quizId);
    }
}
