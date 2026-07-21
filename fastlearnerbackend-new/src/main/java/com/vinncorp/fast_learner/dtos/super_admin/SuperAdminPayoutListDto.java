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
public class SuperAdminPayoutListDto {
    private Long instructorId;
    private String instructorName;
    private String email;
    private double totalEarnings;
    private double pending;
    private double paid;
    private Date lastPayoutDate;
    private String status;           // Pending, Partially Paid, Paid
}
