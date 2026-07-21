package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminPayoutStatsDto {
    private double totalEarnings;
    private double pendingPayouts;
    private double totalPaidOut;
    private double thisMonthPayouts;
}
