package ch.frupp.tutorbot.course.topic.quiz;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/topics/{topicId}/quizzes")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public List<Quiz> getQuizzes(@PathVariable String courseId, @PathVariable String topicId, Authentication auth) {
        var user = (ch.frupp.tutorbot.user.User) auth.getPrincipal();
        return quizService.getAllQuizzesByUserAndTopicId(user, topicId).orElse(List.of());
    }

    @PostMapping
    public Quiz createQuiz(@PathVariable String courseId, @PathVariable String topicId, @RequestBody Quiz quiz, Authentication auth) {
        var user = (ch.frupp.tutorbot.user.User) auth.getPrincipal();
        quiz.setUserId(user.getId());
        quiz.setTopicId(topicId);
        return quizService.generateAndSaveQuiz(user, topicId);
    }
}
