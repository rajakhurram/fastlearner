package com.vinncorp.fast_learner.request.ai_grader.ai_assessment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AIAssessmentRequest {
    @NotBlank(message = "Assessment name cannot be blank")
    private String name;

    @NotNull(message = "Valid classId is required.")
    private Long classId;
}
