package com.vinncorp.fast_learner.response.ai_grader.ai_assessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentStatusCountResponse {
    private Long id;
    private String name;
    private String full_name;
    private Double score;
    private Double grade;
    private Long process;
    private Long graded;
    private Long approved;
}
