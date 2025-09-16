package com.vinncorp.fast_learner.request.course;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCourseRequest {
    private String searchValue;
    private Double reviewFrom;
    private Double reviewTo;
    private Boolean isNlpSearch;
    private int pageNo;
    private int pageSize;
}
