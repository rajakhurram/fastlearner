package com.vinncorp.fast_learner.response.enrollment;

import com.vinncorp.fast_learner.dtos.enrollment.EnrolledCourseDetail;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledCourseResponse {
    private List<EnrolledCourseDetail> myCourses;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
}
