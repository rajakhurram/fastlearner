package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.dtos.course.CourseFeedback;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseFeedbackResponse {

    private CourseFeedback feedback;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
}
