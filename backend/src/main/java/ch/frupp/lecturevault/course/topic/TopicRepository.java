package ch.frupp.lecturevault.course.topic;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends MongoRepository<Topic, String> {
    List<Topic> findAllByUserId(Integer userId);
    Optional<Topic> findByUserIdAndName(Integer userId, String name);
    List<Topic> findByuserIdAndCourseId(Integer userId, String courseId);
    void deleteByUserIdAndId(Integer userId, String id);
}

