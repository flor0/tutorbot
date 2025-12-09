package ch.frupp.lecturevault.course.topic.summary;

import ch.frupp.lecturevault.ai.AiAssistant;
import ch.frupp.lecturevault.course.topic.Topic;
import ch.frupp.lecturevault.user.User;
import dev.langchain4j.invocation.InvocationParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SummaryService {

    private final AiAssistant aiAssistant;

    public SummaryService(AiAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
    }

    public Topic addSummary(Topic topic, User user) {
        InvocationParameters parameters = InvocationParameters.from(Map.of(
                "userid", String.valueOf(user.getId())
//                "courseid", ...
        ));
        String summary = aiAssistant.summarizeTopic(topic.getName(), parameters);
        topic.setSummary(summary);
        return topic;
    }
}
