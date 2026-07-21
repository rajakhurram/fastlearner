package com.vinncorp.fast_learner.response.ai_grader.ai_assessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentWithClassInfo {
    private Long id;
    private String name;
    private Long process;
    private Long graded;
    private Long approved;
    private Long classId;
    private String className;
}
