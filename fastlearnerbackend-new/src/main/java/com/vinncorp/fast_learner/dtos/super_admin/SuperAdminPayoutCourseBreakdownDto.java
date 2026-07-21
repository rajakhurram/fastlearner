package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminPayoutCourseBreakdownDto {
    private Long courseId;
    private String courseTitle;
    private double totalEarned;
    private double paid;
    private double pending;
}
