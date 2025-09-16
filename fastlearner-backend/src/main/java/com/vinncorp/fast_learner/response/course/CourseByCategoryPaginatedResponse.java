package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.response.page.PageResponse;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseByCategoryPaginatedResponse extends PageResponse {
    private List<CourseByCategoryResponse> data;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int pages;
}
