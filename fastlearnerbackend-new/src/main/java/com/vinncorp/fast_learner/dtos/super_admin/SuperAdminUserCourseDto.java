package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminUserCourseDto {
    private Long courseId;
    private String courseTitle;
    private String instructorName;
    private int progressPercent;    // 0-100
}
