package com.vinncorp.fast_learner.response.customer_profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProfileIdType {
    private String customerProfileId;
    private String customerPaymentProfileId;
    private String customerAddressId;
}
