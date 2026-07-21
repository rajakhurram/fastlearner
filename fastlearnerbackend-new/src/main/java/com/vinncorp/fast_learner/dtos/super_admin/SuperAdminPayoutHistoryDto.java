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
public class SuperAdminPayoutHistoryDto {
    private double amount;
    private String type;             // Subscription, Course
    private Date payoutDate;
    private String status;           // Processed
}
