package com.vinncorp.fast_learner.request.ai_grader.ai_assessment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentRequest {
    @NotNull(message = "classId is required")
    private Long classId;
    private Long assessmentId;
    private String studEmail;

}
