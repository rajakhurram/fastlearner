package com.vinncorp.fast_learner.request.ai_grader.ai_assessment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIAssessmentReq {
    @NotNull(message = "classId is required")
    private Long classId;
    private String search;
}
