package com.vinncorp.fast_learner.response.favourite_course;

import com.vinncorp.fast_learner.dtos.favourite_course.FavouriteCourseDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteCourseResponse {
    private List<FavouriteCourseDetail> favouriteCourses;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
}
