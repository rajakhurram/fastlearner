package com.vinncorp.fast_learner.request.course;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RelatedCoursesRequest {

    private Long courseId;
    private int pageNo;
    private int pageSize;
    private String courseDetail;
}
