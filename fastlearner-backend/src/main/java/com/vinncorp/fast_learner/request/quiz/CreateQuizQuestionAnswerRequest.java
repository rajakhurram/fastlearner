package com.vinncorp.fast_learner.request.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuizQuestionAnswerRequest {

    private Long id;
    private Boolean delete;
    private String answerText;
    private Boolean isCorrectAnswer;
}
