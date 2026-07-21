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
public class SuperAdminSubscriptionListDto {
    private String subscriptionId;   // formatted as SUB-001
    private Long rawId;
    private String userName;
    private String userEmail;
    private String planName;
    private String billingCycle;     // Monthly, Yearly
    private double amount;
    private String status;           // Active, Expired, Cancelled
    private Date startDate;
    private Date renewalDate;        // null if Cancelled or Expired
}
