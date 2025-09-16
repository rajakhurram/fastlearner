package com.vinncorp.fast_learner.dtos.section;

import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestionAnswers {

    private Long id;
    private String answerText;

    public static List<QuizQuestionAnswers> from(List<QuizQuestionAnwser> quizQuestionAnswers) {
        return quizQuestionAnswers.stream()
                .map(e -> QuizQuestionAnswers.builder()
                        .id(e.getId())
                        .answerText(e.getAnswer())
                        .build()).toList();
    }
}
