package ch.frupp.tutorbot.course.topic.quiz;

import ch.frupp.tutorbot.course.topic.Topic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


import java.util.List;

@Entity
@Table(name = "quizzes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> choices;

    @Column(nullable = false)
    private int correctAnswerIndex;

    // A Quiz is owned by a Topic
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Override
    public String toString() {
        return "Quiz{" +
                "question='" + question + '\'' +
                ", choices=" + choices +
                ", correctAnswerIndex=" + correctAnswerIndex +
                '}';
    }

    // Optional: validation method for unit tests
    public boolean isValid() {
        return question != null && !question.isEmpty() &&
                choices != null && choices.size() == 4 &&
                correctAnswerIndex >= 0 && correctAnswerIndex < 4;
    }
}


