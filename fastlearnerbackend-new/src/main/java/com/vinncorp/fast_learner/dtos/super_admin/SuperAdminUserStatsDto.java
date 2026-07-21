package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminUserStatsDto {
    private long totalUsers;
    private long freeUsers;
    private long standardUsers;
    private long premiumUsers;
    private long totalPremiumUsers;
    private long enterpriseUsers;
}
