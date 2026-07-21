package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCustomerPaymentProfileResponse extends ApiResponse{
    private String customerProfileId;
    private String customerPaymentProfileId;
    private String validationDirectResponse;
}
