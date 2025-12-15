package ch.frupp.tutorbot.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    Logger logger = LoggerFactory.getLogger(CourseService.class);

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public List<Course> findByUserId(Integer userId) {
        var courses = courseRepository.findByUserId(userId);
        if (courses == null || courses.isEmpty()) {
            logger.info("Found 0 courses for user with UID {}", userId);
        } else {
            logger.info("Found {} courses for user with UID {}. First: {}", courses.size(), userId, courses.getFirst().toString());
        }
        return courses;
    }

    public Optional<Course> findById(String id) {
        return courseRepository.findById(id);
    }

    public Course save(Course course) {
        logger.info("Saving course {}", course);
        return courseRepository.save(course);
    }

    public void deleteByIdAndUserId(String id, Integer userId) {
        courseRepository.deleteByIdAndUserId(id, userId);
    }

    public void deleteById(String id) {
        courseRepository.deleteById(id);
    }
}
