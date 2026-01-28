package com.vinncorp.fast_learner.response.quiz;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Data
public class QuestionEvaluationResponse {
    private String questionText;
    private List<String> correctAnswers;
    private List<String> studentAnswers;
    private Boolean isCorrect;
}
