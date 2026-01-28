package com.vinncorp.fast_learner.request.quiz;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestionReport {
    private String questionText;
    private List<String> correctAnswer;
    private List<String> studentAnswer;
    private Boolean isStudentAnswerCorrect = false;
}
