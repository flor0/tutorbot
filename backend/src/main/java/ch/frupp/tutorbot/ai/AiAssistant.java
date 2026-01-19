package ch.frupp.tutorbot.ai;

import ch.frupp.tutorbot.course.topic.quiz.Quiz;
import ch.frupp.tutorbot.course.topic.quiz.QuizAiTemplate;
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

    @SystemMessage("You are an expert question generator for educational content. Produce exactly one multiple-choice question (with 4 answer options) tailored to the provided topic and any contextual information retrieved by the RAG pipeline. The output must be a single valid JSON object that matches the structure of the QuizAiTemplate Java class. Do NOT output any additional text, commentary, or markdown. Keep language concise, factual, and appropriate for a student audience.")
    @UserMessage("""
        Generate exactly one multiple-choice question about the topic: "{{topic}}".

        Requirements:
        - Produce a JSON object that maps directly to the QuizAiTemplate class (question, answers array of length 4, correctAnswerIndex as an integer).
        - Question length: prefer under 20 words.
        - Provide four distinct, plausible answer choices. One must be correct.
        - The correctAnswerIndex must be a 0-based index into the answers array.
        - Use context from the InvocationParameters (e.g., retrieved RAG fragments) when available to make the question specific and accurate. If contextual fragments are present in the parameters, prefer them as the source of facts.
        - Do not include source citations, metadata, or extra fields beyond the QuizAiTemplate structure.

        Example output (valid JSON):
        {"question":"What is X?","answers":["A","B","C","D"],"correctAnswerIndex":2}
        """)
    QuizAiTemplate generateQuizQuestions(@V("topic") String topic, InvocationParameters parameters);
}
