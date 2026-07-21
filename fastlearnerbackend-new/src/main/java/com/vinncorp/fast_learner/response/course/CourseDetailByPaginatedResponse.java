package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.response.page.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailByPaginatedResponse extends PageResponse {

    private List<CourseDetailResponse> data;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int pages;
}
