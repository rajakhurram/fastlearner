package com.vinncorp.fast_learner.request.subscription;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileIdType;
import com.vinncorp.fast_learner.response.subscription.ApiResponse;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateSubscriptionResponse extends ApiResponse {
    private CustomerProfileIdType profile;
}
