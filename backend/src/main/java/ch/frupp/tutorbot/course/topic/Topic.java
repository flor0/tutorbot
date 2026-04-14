package ch.frupp.tutorbot.course.topic;

import ch.frupp.tutorbot.course.Course;
import ch.frupp.tutorbot.course.topic.quiz.Quiz;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // A short machine-friendly name / keyword for the topic, e.g. "Integrals"
    @Column(unique = true, nullable = false)
    private String name;

    // A short summary describing the topic
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    // A Topic owns multiple Quizzes
    @JsonIgnore
    @OneToMany(mappedBy = "topic", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    // A Topic is owned by a Course
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

}
