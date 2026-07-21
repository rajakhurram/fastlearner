package com.vinncorp.fast_learner.response.ai_grader;

import com.vinncorp.fast_learner.response.subscription.ApiResponse;
import lombok.Data;

@Data
public class AiGraderClassResponse extends ApiResponse {
    private String name;
    private String description;
}
