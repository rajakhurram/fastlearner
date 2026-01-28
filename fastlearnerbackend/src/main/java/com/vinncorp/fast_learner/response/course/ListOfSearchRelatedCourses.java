package com.vinncorp.fast_learner.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListOfSearchRelatedCourses {
    private Long id;
    private Double similarity;
    private String title;
    private String description;
    private String thumbnail;
}
