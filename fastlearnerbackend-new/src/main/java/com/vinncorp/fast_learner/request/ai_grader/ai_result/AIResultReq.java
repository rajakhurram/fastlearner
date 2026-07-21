package com.vinncorp.fast_learner.request.ai_grader.ai_result;

import com.vinncorp.fast_learner.util.enums.AIResultSort;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AIResultReq {

    @NotNull(message = "assignmentId is required")
    private Long assignmentId;
    @NotNull(message = "classId is required")
    private Long classId;
    private String search;
    private AIResultSort aiResultSort;
    private String studEmail;
}
