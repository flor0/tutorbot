package ch.frupp.tutorbot.course.topic.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quizzes")
public class Quiz {
    private String question;
    private List<String> choices;
    private int correctAnswerIndex;

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