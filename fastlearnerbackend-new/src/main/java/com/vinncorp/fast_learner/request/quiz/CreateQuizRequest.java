package com.vinncorp.fast_learner.request.quiz;

import com.vinncorp.fast_learner.util.enums.TestType;
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
    private Boolean generateAIReport;
    private String reportPrompt;
    private TestType testType;
    private List<CreateQuizQuestionRequest> questions;
    private Boolean isAllowedToRetake;
}
