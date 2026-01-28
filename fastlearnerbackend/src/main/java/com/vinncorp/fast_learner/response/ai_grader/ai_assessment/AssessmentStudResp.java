package com.vinncorp.fast_learner.response.ai_grader.ai_assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentStudResp {
    private Long id;
    private String name;
    private String full_name;
    private Double score;
    private Date created_date;
    private Long process;
    private Long graded;
    private Long approved;
}
