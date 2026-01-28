package com.vinncorp.fast_learner.request.course;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CourseByCategoryRequest {

    private Long categoryId;
    private Long courseLevelId;
    private int pageNo;
    private int pageSize;
}
