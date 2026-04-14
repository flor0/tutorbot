package ch.frupp.tutorbot.course.topic;

import ch.frupp.tutorbot.course.Course;
import ch.frupp.tutorbot.course.CourseRepository;
import ch.frupp.tutorbot.course.topic.quiz.Quiz;
import ch.frupp.tutorbot.course.topic.quiz.QuizRepository;
import ch.frupp.tutorbot.course.topic.quiz.QuizService;
import ch.frupp.tutorbot.course.topic.summary.SummaryService;
import ch.frupp.tutorbot.user.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SummaryService summaryService;
    private final CourseRepository courseRepository;

    public TopicService(TopicRepository repository, SummaryService summaryService, QuizRepository quizRepository, QuizService quizService, CourseRepository courseRepository) {
        this.topicRepository = repository;
        this.summaryService = summaryService;
        this.courseRepository = courseRepository;
    }

    public List<Topic> listTopicsForUserAndCourse(User user, Integer courseId) {
        // TODO: Validate User ownership
        return topicRepository.findByCourseId(courseId);
    }

    public Topic createTopicForUser(User user, TopicDto topicDto, Integer courseId) {
        // Get the Course object the topic belongs to (If none found throw exception)
        Course course = courseRepository.findById(courseId).orElseThrow();

        Topic newTopic = Topic.builder()
                .name(topicDto.name())
                // Generate AI Summary on the go
                .summary (summaryService.generateSummary(topicDto.name(), user))
                .course(course)
                .build();

        return topicRepository.save(newTopic);
    }

    public void deleteTopicForUser(User user, Integer topicId) {
        //TODO: Validate user ownership
        topicRepository.deleteById(topicId);
    }
}

