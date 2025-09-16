package com.vinncorp.fast_learner.response.customer_profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.subscription.ApiResponse;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetCustomerProfileResponse extends ApiResponse {
    private CustomerProfileMaskedType profile;
    private SubscriptionIdList subscriptionIds;
}
