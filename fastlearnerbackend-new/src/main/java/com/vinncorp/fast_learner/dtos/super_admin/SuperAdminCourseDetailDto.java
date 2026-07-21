package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminCourseDetailDto {
    private String courseId;         // CRS-001
    private Long rawId;
    private String title;
    private String description;
    private String categoryName;
    private String status;           // DRAFT, PUBLISHED, UNPUBLISHED
    private Double price;
    private String courseType;       // FREE_COURSE, PREMIUM_COURSE
    private Long instructorId;
    private String instructorName;
    private String instructorEmail;
    private long studentCount;
    private Double avgRating;
    private Date createdDate;
}
