package com.vinncorp.fast_learner.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseReviewResponse {
    private Long courseId;
    private String comment;
    private double value;
    private Long courseReviewId;
    private long totalReviews;
}
