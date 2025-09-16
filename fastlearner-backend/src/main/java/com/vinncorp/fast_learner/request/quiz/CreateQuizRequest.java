package com.vinncorp.fast_learner.request.quiz;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateQuizRequest {

    private Long id;
    private Boolean delete;
    private String title;
    private Integer durationInMinutes;
    private Double passingCriteria;
    private Integer randomQuestion;
    private List<CreateQuizQuestionRequest> questions;
}
