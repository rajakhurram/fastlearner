package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminPayoutDetailDto {
    private Long instructorId;
    private String instructorName;
    private String email;
    private double totalEarnings;
    private double paid;
    private double remaining;
    private List<SuperAdminPayoutCourseBreakdownDto> courseBreakdown;
    private List<SuperAdminPayoutHistoryDto> payoutHistory;
}
