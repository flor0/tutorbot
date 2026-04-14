package ch.frupp.tutorbot.course.topic.summary;

import ch.frupp.tutorbot.ai.AiAssistant;
import ch.frupp.tutorbot.course.topic.Topic;
import ch.frupp.tutorbot.user.User;
import dev.langchain4j.invocation.InvocationParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SummaryService {

    private final AiAssistant aiAssistant;

    public SummaryService(AiAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
    }

    public String generateSummary(String topicName, User user) {
        InvocationParameters parameters = InvocationParameters.from(Map.of(
                "userid", String.valueOf(user.getId())
//                "courseid", ...
        ));
        return aiAssistant.summarizeTopic(topicName, parameters);
    }
}
