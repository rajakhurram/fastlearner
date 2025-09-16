package com.vinncorp.fast_learner.dtos.quiz;

import lombok.*;

@Getter
@Setter
public class QuizAnswer {

    private Long answerId;
    private String answerText;
    private Boolean isCorrect;
}
