package com.vinncorp.fast_learner.response.subscription;

import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileIdType;
import lombok.Data;

@Data
public class CreateSubscriptionResponse extends ApiResponse {
    private String subscriptionId;
    private CustomerProfileIdType profile;
}
