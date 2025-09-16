package com.vinncorp.fast_learner.response.section;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionReviewResponse {

    private Long sectionReviewId;
    private Long sectionId;
    private double value;
    private String comment;
    private long totalReviews;
}
