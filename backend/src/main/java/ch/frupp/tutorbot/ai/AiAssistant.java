package ch.frupp.tutorbot.ai;

import ch.frupp.tutorbot.course.topic.quiz.Quiz;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.SystemMessage;

// @AiService
public interface AiAssistant {

    @UserMessage("""
        Based on the following chapter, generate exactly 1 multiple-choice question.
        Output a short, clear question, 4 answer choices, and the correct answer index.
        Question must be under 15 words.
        Return a valid JSON object matching the Quiz class.
        Chapter: {{chapter}}
        """)
    Quiz generateQuiz(@V("chapter") String chapterText);


    @SystemMessage("You are an expert assistant that writes concise, factual summaries for academic topics. " +
                   "Produce a single short paragraph, avoid lists or metadata, do not include quotes or markdown. " +
                   "Keep it clear and relevant to the given topic.")
    @UserMessage("Summarize the following topic in one concise paragraph: {{topic}}")
    String summarizeTopic(@V("topic") String topic, InvocationParameters parameters);
}

