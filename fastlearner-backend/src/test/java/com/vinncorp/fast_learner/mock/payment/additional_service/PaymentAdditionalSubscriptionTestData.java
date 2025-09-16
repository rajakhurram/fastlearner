package com.vinncorp.fast_learner.mock.payment.additional_service;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.mock.payment.payment_profile.PaymentProfileTestData;

public class PaymentAdditionalSubscriptionTestData {

    public static SubscriptionRequest subscriptionRequest() {
        return SubscriptionRequest.builder()
                .subscriptionId(2L)
                .paymentDetail(PaymentProfileTestData.paymentProfileDetailRequest())
                .build();
    }
}
