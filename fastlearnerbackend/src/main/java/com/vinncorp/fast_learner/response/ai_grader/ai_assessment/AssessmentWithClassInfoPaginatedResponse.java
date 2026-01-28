package com.vinncorp.fast_learner.response.ai_grader.ai_assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentWithClassInfoPaginatedResponse {

    List<AssessmentWithClassInfo> data;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int pages;
}
