package com.vinncorp.fast_learner.request.section;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSectionReviewRequest {

    private int value;
    private Long sectionId;
    private Long courseId;
    private String comment;
}
