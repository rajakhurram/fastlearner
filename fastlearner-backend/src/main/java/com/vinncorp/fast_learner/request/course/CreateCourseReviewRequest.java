package com.vinncorp.fast_learner.request.course;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCourseReviewRequest {

    private int value;
    private String comment;
    private Long courseId;
}
