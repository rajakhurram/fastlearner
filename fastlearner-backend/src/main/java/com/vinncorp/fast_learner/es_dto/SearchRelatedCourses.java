package com.vinncorp.fast_learner.es_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRelatedCourses {

    private Long id;
    private String thumbnail;
    private String title;
    private String description;
}
