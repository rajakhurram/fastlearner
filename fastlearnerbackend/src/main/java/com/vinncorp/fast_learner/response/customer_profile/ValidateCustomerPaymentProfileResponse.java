package com.vinncorp.fast_learner.response.customer_profile;

import com.vinncorp.fast_learner.response.subscription.ApiResponse;
import lombok.Data;

@Data
public class ValidateCustomerPaymentProfileResponse extends ApiResponse {
    private String directResponse;
}
