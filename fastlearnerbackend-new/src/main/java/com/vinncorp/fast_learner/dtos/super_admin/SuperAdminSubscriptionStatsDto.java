package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminSubscriptionStatsDto {
    private long activeSubscriptions;
    private long standardPlanCount;
    private long premiumPlanCount;
    /** ULTIMATE plan_type mapped as enterprise in admin UI */
    private long enterprisePlanCount;
}
