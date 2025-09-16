package com.vinncorp.fast_learner.response.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerResponse {
    private Double percentage;
    private Double passingCriteria;
    private Long totalQuestion;
    private Long totalCorrectAnswer;
    private Long totalAttemptQuestion;
    List<QuestionAnswerResponse> questionAnswerResponses;

}
