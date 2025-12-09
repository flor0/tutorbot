package ch.frupp.lecturevault.course.material;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "coursematerials")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourseMaterial {
    @Id
    private String id;

    @Indexed
    private Integer userId;

    @Indexed
    private String courseId;

    private String filename;

    public CourseMaterial(Integer userId, String courseId, String filename) {
        this.userId = userId;
        this.courseId = courseId;
        this.filename = filename;
    }

}


