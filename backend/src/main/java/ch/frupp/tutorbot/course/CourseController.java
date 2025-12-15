package ch.frupp.tutorbot.course;

import ch.frupp.tutorbot.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> findAllByUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();
        return courseService.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> findById(@PathVariable String id) {
        return courseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public Course create(@RequestBody CourseDto course, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();
        return courseService.save(new Course(
                userId,
                course.name()
        ));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer userId = user.getId();

        courseService.deleteByIdAndUserId(id, userId);
    }

}
