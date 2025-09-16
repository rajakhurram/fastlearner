package com.vinncorp.fast_learner.mock.payment.payment_profile;

import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;

public class PaymentProfileTestData {

    public static PaymentProfileDetailRequest paymentProfileDetailRequest() {
        return PaymentProfileDetailRequest.builder()
                .cardNumber("411111111111111")
                .cvv("900")
                .expiryMonth("11")
                .expiryYear("2028")
                .firstName("Qasim")
                .lastName("Ali")
                .build();
    }
}
