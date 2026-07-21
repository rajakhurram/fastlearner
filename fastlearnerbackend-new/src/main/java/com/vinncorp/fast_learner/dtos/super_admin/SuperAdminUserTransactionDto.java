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
public class SuperAdminUserTransactionDto {
    private String transactionId;   // e.g. "TXN-001" or external ID
    private double amount;
    private Date transactionDate;
    private String status;          // Completed, Refunded, Pending
}
