package com.vinncorp.fast_learner.response.quiz;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AnswerResponse {
    private Long answerId;
    private String answerText;

}
