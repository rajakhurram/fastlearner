package com.vinncorp.fast_learner.response.subscription;

import lombok.Data;

@Data
public class GetSubscriptionResponse extends ApiResponse{
    private SubscriptionMaskedType subscription;
}
