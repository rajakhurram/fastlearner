package com.vinncorp.fast_learner.response.ai_grader.ai_assessment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIAssessmentResponse {
    private Long id;
    private String name;
}
