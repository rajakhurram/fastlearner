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
public class SuperAdminInvoiceDetailDto {
    private String invoiceId;            // INV-001 or external_transaction_id
    private String userName;
    private String userEmail;
    private String plan;                 // subscription plan name, null for course purchase
    private double subtotal;
    private double total;
    private String transactionId;        // external_transaction_id or auth_subscription_id
    private String paymentStatus;        // Completed, Pending, Trialed
    private String subscriptionDisplayId; // SUB-001, null for course purchase
    private Date invoiceDate;
    private Date updatedDate;
}
