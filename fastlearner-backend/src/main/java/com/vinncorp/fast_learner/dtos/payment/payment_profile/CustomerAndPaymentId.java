package com.vinncorp.fast_learner.dtos.payment.payment_profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAndPaymentId {
    private String customerId;
    private String paymentId;
    private String addressId;
}
