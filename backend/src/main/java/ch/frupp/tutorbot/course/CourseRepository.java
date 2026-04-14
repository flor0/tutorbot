package ch.frupp.tutorbot.course;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByUserId(Integer userId);
    void deleteById(Integer id);
}
