package ch.frupp.lecturevault.course;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String name;

    private Integer userId;

    public Course(Integer userId, String name) { this.userId = userId; this.name = name; }

    public String toString() { return "Course{id=" + id + ", name='" + name + "', userId=" + userId + "}"; }

}
