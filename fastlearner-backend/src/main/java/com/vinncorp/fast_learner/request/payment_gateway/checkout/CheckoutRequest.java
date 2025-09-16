package com.vinncorp.fast_learner.request.payment_gateway.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {

    private String opaqueData;
    private BigDecimal amount;
}

