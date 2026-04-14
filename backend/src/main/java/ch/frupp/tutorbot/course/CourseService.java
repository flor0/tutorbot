package ch.frupp.tutorbot.course;

import ch.frupp.tutorbot.course.material.CourseMaterialRepository;
import ch.frupp.tutorbot.course.topic.TopicRepository;
import ch.frupp.tutorbot.course.topic.TopicService;
import ch.frupp.tutorbot.course.topic.quiz.QuizRepository;
import ch.frupp.tutorbot.user.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final QuizRepository quizRepository;
    private final TopicService topicService;
    private final CourseMaterialRepository courseMaterialRepository;

    public CourseService(CourseRepository courseRepository, TopicRepository topicRepository, QuizRepository quizRepository, TopicService topicService, CourseMaterialRepository courseMaterialRepository) {
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.quizRepository = quizRepository;
        this.topicService = topicService;
        this.courseMaterialRepository = courseMaterialRepository;
    }


    public List<Course> findByUserId(Integer userId) {
        var courses = courseRepository.findByUserId(userId);
        if (courses == null || courses.isEmpty()) {
            log.info("Found 0 courses for user with UID {}", userId);
        } else {
            log.info("Found {} courses for user with UID {}. First: {}", courses.size(), userId, courses.getFirst().toString());
        }
        return courses;
    }

    public Optional<Course> findById(Integer id) {
        return courseRepository.findById(id);
    }

    public Course save(Course course) {
        log.info("Saving course {}", course);
        return courseRepository.save(course);
    }

    public void deleteByIdAndUserId(Integer id, User user) {
        int userId = user.getId();
        // Delete course and all its topics and related data
        log.info("Deleting course with ID {} for user with UID {}", id, userId);
        courseRepository.deleteById(id);
        // Delete all topics and related quizzes and materials for the course...
        // TODO: Verify: Should be done automatically because of the foreign key relationship
    }

    public void updateByIdAndUserId(String id, User user, Course updatedCourse) {

    }

}
