package ch.frupp.tutorbot.course.topic.quiz;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findAllByUserIdAndTopicId(Integer userId, String topicId);
}
