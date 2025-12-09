package ch.frupp.lecturevault.course.topic;

import ch.frupp.lecturevault.course.topic.summary.SummaryService;
import ch.frupp.lecturevault.user.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository repository;
    private final SummaryService summaryService;

    public TopicService(TopicRepository repository, SummaryService summaryService) {
        this.repository = repository;
        this.summaryService = summaryService;
    }

    public List<Topic> listTopicsForUserAndCourse(User user, String courseId) {
        return repository.findByuserIdAndCourseId(user.getId(), courseId);
    }

    public Topic createTopicForUser(User user, Topic topic, String courseId) {
        // ensure the topic is bound to the user
        topic.setUserId(user.getId());
        topic.setCourseId(courseId);

        // enforce uniqueness: check if name exists for user
        repository.findByUserIdAndName(user.getId(), topic.getName()).ifPresent(_ -> {
            throw new DuplicateKeyException("Topic with name already exists for user");
        });

        // Generate a summary for the topic in advance
        topic = summaryService.addSummary(topic, user);

        return repository.save(topic);
    }

    public void deleteTopicForUser(User user, String topicId) {
        repository.deleteByUserIdAndId(user.getId(), topicId);
    }
}

