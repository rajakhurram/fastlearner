package com.vinncorp.fast_learner.integration.subscription;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;

public class SubscriptionTestData {

    public static SubscriptionRequest subscriptionRequest() {
        return SubscriptionRequest.builder()
                .subscriptionId(2L)
                .paymentDetail(PaymentProfileDetailRequest.builder()
                        .firstName("Qasim")
                        .lastName("Ali")
                        .expiryYear("2029")
                        .expiryMonth("02")
                        .cvv("900")
                        .cardNumber("4111111111111111")
                        .build())
                .build();
    }
}
