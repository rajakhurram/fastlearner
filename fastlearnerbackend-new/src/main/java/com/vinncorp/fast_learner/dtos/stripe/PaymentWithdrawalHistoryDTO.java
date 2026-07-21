package com.vinncorp.fast_learner.dtos.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentWithdrawalHistoryDTO {
    private Long id;
    private String bankName;
    private String payoutId;
    private double amount;
    private Date withdrawalAt;
}
