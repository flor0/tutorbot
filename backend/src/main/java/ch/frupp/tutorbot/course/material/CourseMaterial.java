package ch.frupp.tutorbot.course.material;

import ch.frupp.tutorbot.course.Course;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_materials")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CourseMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String filename;

    // A CourseMaterial (PDF) belongs to a Course
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

}


