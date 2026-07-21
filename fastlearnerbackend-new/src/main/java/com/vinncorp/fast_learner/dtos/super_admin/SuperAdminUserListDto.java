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
public class SuperAdminUserListDto {
    private String userId;          // formatted as USR-1001
    private Long rawId;
    private String name;
    private String email;
    private Date registeredDate;
    private String planType;        // FREE, STANDARD, PREMIUM, ULTIMATE
    private String subscriptionStatus; // Active, Cancelled, Expired, None
    private String accountStatus;   // Active, Suspended
}
