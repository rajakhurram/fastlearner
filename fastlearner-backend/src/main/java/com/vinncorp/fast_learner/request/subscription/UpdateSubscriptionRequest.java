package com.vinncorp.fast_learner.request.subscription;

import lombok.Data;

@Data
public class UpdateSubscriptionRequest {
    private String subscriptionId;
    private String customerProfileId;
    private String customerPaymentProfileId;
}
