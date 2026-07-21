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
public class SuperAdminUserSubscriptionDto {
    private Long subscriptionId;
    private String planName;        // e.g. "Yearly Plan"
    private String planType;        // FREE, STANDARD, PREMIUM, ULTIMATE
    private Date startDate;
    private Date endDate;
    private String status;          // Active, Expired, Cancelled
    private double price;
}
