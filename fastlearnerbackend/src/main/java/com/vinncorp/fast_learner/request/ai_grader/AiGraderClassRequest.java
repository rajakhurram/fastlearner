package com.vinncorp.fast_learner.request.ai_grader;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AiGraderClassRequest {
    private String name;
    private Long instructorId;

}
