package com.vinncorp.fast_learner.dtos.payment.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChargePayment {
    private String opaqueData;
    private long courseId;
    private String coupon;
    private String affiliateUUID;
}
