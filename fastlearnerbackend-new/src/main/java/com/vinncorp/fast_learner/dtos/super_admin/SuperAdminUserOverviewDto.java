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
public class SuperAdminUserOverviewDto {
    private String userId;
    private String name;
    private String email;

    // Overview tab cards
    private String subscriptionName;    // e.g. "Yearly"
    private String accountStatus;       // Active, Suspended
    private int coursesEnrolled;
    private double totalSpent;

    // System intelligence
    private boolean hasRiskSignals;
    private Integer daysUntilRenewal;   // null if no active subscription

    // Subscription dates for context
    private Date subscriptionStartDate;
    private Date subscriptionEndDate;
    private String planType;
}
