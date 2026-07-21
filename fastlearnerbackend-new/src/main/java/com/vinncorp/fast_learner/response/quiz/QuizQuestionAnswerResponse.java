package com.vinncorp.fast_learner.response.quiz;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizQuestionAnswerResponse {

    private Boolean isCorrect;
    private Long correctAnswerId;
}