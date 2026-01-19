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

    public Optional<List<Quiz>> getAllQuizzesByUserAndTopicId(User user, String topicId) {

        List<Quiz> quizzes = quizRepository.findAllByUserIdAndTopicId(user.getId(), topicId);
        log.info("Found {} quizzes for user {} and topic {}", quizzes.size(), user.getId(), topicId);
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

    public void deleteQuizById(String quizId) {
        quizRepository.deleteById(quizId);
    }

    private boolean validateQuizOwnership(User user, Quiz quiz) {
        return user.getId() == quiz.getUserId();
    }

    public Quiz generateAndSaveQuiz(User user, String topicId) {

        Quiz generatedQuiz = new Quiz();
        generatedQuiz.setUserId(user.getId());
        generatedQuiz.setTopicId(topicId);

        InvocationParameters parameters = InvocationParameters.from(Map.of(
                "userid", String.valueOf(user.getId())
//                "courseid", ...
        ));

        // Get the name of the topic for quiz generation
        Topic topicObject = topicRepository.findById(topicId).orElse(null);
        assert topicObject != null; // TODO: Better validation and error handling
        QuizAiTemplate quizAiTemplate = aiAssistant.generateQuizQuestions(topicObject.getName(), parameters);

        generatedQuiz.setQuestion(quizAiTemplate.question);
        generatedQuiz.setChoices(quizAiTemplate.choices);
        generatedQuiz.setCorrectAnswerIndex(quizAiTemplate.correctAnswerIndex);

        log.info("Generated quiz: {}", generatedQuiz);

        return quizRepository.save(generatedQuiz);
    }
}
