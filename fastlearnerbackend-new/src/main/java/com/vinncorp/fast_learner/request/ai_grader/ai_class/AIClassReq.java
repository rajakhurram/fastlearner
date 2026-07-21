package com.vinncorp.fast_learner.request.ai_grader.ai_class;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIClassReq {
    private Long classId;
    @NotNull(message = "instructorId is required")
    private Long instructorId;
}
