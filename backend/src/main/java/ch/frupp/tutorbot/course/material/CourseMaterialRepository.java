package ch.frupp.tutorbot.course.material;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CourseMaterialRepository extends MongoRepository<CourseMaterial, String> {
    List<CourseMaterial> findByUserId(Integer userId);
    List<CourseMaterial> findByCourseId(String courseId);
    List<CourseMaterial> findByUserIdAndCourseId(Integer userId, String courseId);

    void deleteByUserId(Integer userId);
}
