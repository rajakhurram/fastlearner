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
public class SuperAdminPaymentListDto {
    private String transactionId;    // external_transaction_id or TXN-{id}
    private Long rawId;
    private String userName;
    private String userEmail;
    private String planType;         // FREE, STANDARD, PREMIUM, ULTIMATE
    private double amount;
    private String method;           // null — not stored in DB
    private Date date;
    private String type;             // Subscription, Course
    private String status;           // Completed, Pending, Failed, Trialed
}
