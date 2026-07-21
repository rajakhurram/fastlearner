package com.vinncorp.fast_learner.mock.authorize_net.additional_service;

import com.vinncorp.fast_learner.dtos.authorize_net.SubscriptionRequest;
import com.vinncorp.fast_learner.mock.authorize_net.payment_profile.PaymentProfileTestData;

public class AuthNetAdditionalSubscriptionTestData {

    public static SubscriptionRequest subscriptionRequest() {
        return SubscriptionRequest.builder()
                .subscriptionId(2L)
                .paymentDetail(PaymentProfileTestData.paymentProfileDetailRequest())
                .build();
    }
}
