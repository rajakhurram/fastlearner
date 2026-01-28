package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.response.course.nlp_search.CourseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseBySearchFilterResponse {
    private List<CourseByCategoryResponse> courses;
    private List<CourseResponse> nlpCourses;
    private Integer pageNo;
    private Integer pageSize;
    private Long totalElements;
    private Integer pages;
}
