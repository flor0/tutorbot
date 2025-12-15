package ch.frupp.tutorbot.course;


import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByUserId(Integer userId);
    void deleteByIdAndUserId(String id, Integer userId);
}
