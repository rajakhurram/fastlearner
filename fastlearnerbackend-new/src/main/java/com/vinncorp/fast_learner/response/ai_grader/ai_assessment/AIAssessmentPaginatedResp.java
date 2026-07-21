package com.vinncorp.fast_learner.response.ai_grader.ai_assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AIAssessmentPaginatedResp {
    private List<AIAssessmentResponse> aiAssessments;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int pages;
}
