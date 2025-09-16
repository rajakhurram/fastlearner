package com.vinncorp.fast_learner.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompletedCourseByPaginated {
    private List<CompletedCourseResponse> data;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int pages;
}
