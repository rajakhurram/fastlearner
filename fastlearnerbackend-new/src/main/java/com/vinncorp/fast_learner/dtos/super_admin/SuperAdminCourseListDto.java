package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminCourseListDto {
    private String courseId;        // formatted as CRS-001
    private Long rawId;
    private String title;
    private String instructorName;
    private Long instructorId;
    private String categoryName;
    private long studentCount;
    private Double avgRating;       // null if no reviews
    private String status;          // DRAFT, PUBLISHED, UNPUBLISHED
    private String courseType;
}
