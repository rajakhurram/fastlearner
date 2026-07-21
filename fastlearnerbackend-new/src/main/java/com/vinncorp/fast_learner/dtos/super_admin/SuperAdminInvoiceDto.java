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
public class SuperAdminInvoiceDto {
    private String invoiceId;       // e.g. "INV-001" or external transaction ID
    private Long rawId;
    private String userName;
    private String userEmail;
    private double amount;
    private String type;            // Subscription, Course Purchase
    private String planName;        // e.g. "Premium Instructor" or null
    private String planType;        // FREE, STANDARD, PREMIUM, ULTIMATE or null
    private String status;          // Paid, Refunded, Unpaid
    private Date invoiceDate;
}
