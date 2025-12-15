package ch.frupp.tutorbot.course.topic;

import ch.frupp.tutorbot.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses/{courseId}/topics")
public class TopicController {

    private static final Logger logger = LoggerFactory.getLogger(TopicController.class);

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public List<TopicDto> listTopics(Authentication authentication, @PathVariable String courseId) {
        User user = (User) authentication.getPrincipal();
        return topicService.listTopicsForUserAndCourse(user, courseId).stream()
                .map(TopicDto::from)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> createTopic(Authentication authentication, @RequestBody TopicDto dto, @PathVariable String courseId) {
        User user = (User) authentication.getPrincipal();
        Topic topic = dto.toTopic();
        try {
            Topic created = topicService.createTopicForUser(user, topic, courseId);
            logger.info("Created topic for user {} with id {}", user, created.getId());
            return ResponseEntity.ok(TopicDto.from(created));
        } catch (DuplicateKeyException ex) {
            logger.warn("Duplicate topic for user {}: {}", user, dto.name);
            return ResponseEntity.status(409).body("Topic with this name already exists");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTopic(Authentication authentication, @PathVariable String id, @PathVariable String courseId) {
        User user = (User) authentication.getPrincipal();
        topicService.deleteTopicForUser(user, id);
        return ResponseEntity.noContent().build();
    }
}

