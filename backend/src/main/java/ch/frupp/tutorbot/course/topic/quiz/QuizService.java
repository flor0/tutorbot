package ch.frupp.tutorbot.course.topic.quiz;

import ch.frupp.tutorbot.ai.AiAssistant;
import ch.frupp.tutorbot.course.topic.Topic;
import ch.frupp.tutorbot.course.topic.TopicRepository;
import ch.frupp.tutorbot.user.User;
import dev.langchain4j.invocation.InvocationParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final AiAssistant aiAssistant;
    private final TopicRepository topicRepository;

    public QuizService(QuizRepository quizRepository, AiAssistant aiAssistant, TopicRepository topicRepository) {
        this.quizRepository = quizRepository;
        this.aiAssistant = aiAssistant;
        this.topicRepository = topicRepository;
    }

    public Optional<List<Quiz>> getAllQuizzesByUserAndTopicId(User user, Integer topicId) {
        // TODO: Validate user ownership
        List<Quiz> quizzes = quizRepository.findAllByTopicId(topicId);
        log.info("Found {} quizzes for user {} and topic {}", quizzes.size(), user.getId(), topicId);

        // Remove and log quizzes not owned by the user
        quizzes.stream().filter(quiz -> !validateQuizOwnership(user, quiz)).forEach(quiz -> {
            log.warn("Quiz with ID {} does not belong to user {} and will be removed from the response", quiz.getId(), user.getId());
            quizzes.remove(quiz);
        });

        if (!quizzes.isEmpty()) {
            return Optional.of(quizzes);
        }
        return Optional.empty();
    }

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Quiz updateQuiz(User user, Quiz quiz) {
        if (!validateQuizOwnership(user, quiz)) {
            throw new IllegalArgumentException("User does not own the quiz");
        }
        return quizRepository.save(quiz);
    }

    public void deleteQuizById(Integer quizId) {
        quizRepository.deleteById(quizId);
    }

    private boolean validateQuizOwnership(User user, Quiz quiz) {
        Integer dbUserId = quiz.getTopic().getCourse().getUser().getId();
        return Objects.equals(user.getId(), dbUserId);
    }

    public Quiz generateAndSaveQuiz(User user, Integer topicId) {

        // get the Topic that owns the Quiz
        Topic topic = topicRepository.getReferenceById(topicId);

        // Create an AI template for the prompt
        InvocationParameters parameters = InvocationParameters.from(Map.of(
                "userid", String.valueOf(user.getId())
//                "courseid", ...
        ));
        QuizAiTemplate aiTemplate = aiAssistant.generateQuizQuestions(topic.getName(), parameters);
        Quiz generatedQuiz = Quiz.builder()
                .question(aiTemplate.question())
                .choices(aiTemplate.choices())
                .correctAnswerIndex(aiTemplate.correctAnswerIndex())
                .topic(topic)
                .build();
        log.info("Generated quiz: {}", generatedQuiz);

        return quizRepository.save(generatedQuiz);
    }
}
