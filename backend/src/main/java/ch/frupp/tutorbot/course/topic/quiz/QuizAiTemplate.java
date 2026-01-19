package ch.frupp.tutorbot.course.topic.quiz;

import java.util.List;

public record QuizAiTemplate(
        String question,
        List<String> choices,
        int correctAnswerIndex
) { }
