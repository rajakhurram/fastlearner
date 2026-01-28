package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.dtos.course.RelatedCourses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatedCoursesResponse {

    private List<RelatedCourses> courses;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}