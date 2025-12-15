package ch.frupp.tutorbot.ai;

import ch.frupp.tutorbot.course.topic.quiz.Quiz;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiAssistantTest {



    Logger logger = LoggerFactory.getLogger(AiAssistantTest.class);

    @Autowired
    private AiAssistant aiAssistant;


    @Test
    void generateQuiz() {
        String chapterText = "AI Services\n" +
                "\n" +
                "So far, we have been covering low-level components like ChatModel, ChatMessage, ChatMemory, etc. Working at this level is very flexible and gives you total freedom, but it also forces you to write a lot of boilerplate code. Since LLM-powered applications usually require not just a single component but multiple components working together (e.g., prompt templates, chat memory, LLMs, output parsers, RAG components: embedding models and stores) and often involve multiple interactions, orchestrating them all becomes even more cumbersome.\n" +
                "\n" +
                "We want you to focus on business logic, not on low-level implementation details. Thus, there are currently two high-level concepts in LangChain4j that can help with that: AI Services and Chains.\n" +
                "Chains (legacy)\n" +
                "\n" +
                "The concept of Chains originates from Python's LangChain (before the introduction of LCEL). The idea is to have a Chain for each common use case, like a chatbot, RAG, etc. Chains combine multiple low-level components and orchestrate interactions between them. The main problem with them is that they are too rigid if you need to customize something. LangChain4j has only two Chains implemented (ConversationalChain and ConversationalRetrievalChain), and we do not plan to add more at this moment.\n" +
                "AI Services\n" +
                "\n" +
                "We propose another solution called AI Services, tailored for Java. The idea is to hide the complexities of interacting with LLMs and other components behind a simple API.\n" +
                "\n" +
                "This approach is very similar to Spring Data JPA or Retrofit: you declaratively define an interface with the desired API, and LangChain4j provides an object (proxy) that implements this interface. You can think of AI Service as a component of the service layer in your application. It provides AI services. Hence the name.\n" +
                "\n" +
                "AI Services handle the most common operations:\n" +
                "\n" +
                "    Formatting inputs for the LLM\n" +
                "    Parsing outputs from the LLM\n" +
                "\n" +
                "They also support more advanced features:\n" +
                "\n" +
                "    Chat memory\n" +
                "    Tools\n" +
                "    RAG\n" +
                "\n" +
                "AI Services can be used to build stateful chatbots that facilitate back-and-forth interactions, as well as to automate processes where each call to the LLM is isolated.";
        Quiz q = aiAssistant.generateQuiz(chapterText);
        logger.info(q.toString());
    }
}