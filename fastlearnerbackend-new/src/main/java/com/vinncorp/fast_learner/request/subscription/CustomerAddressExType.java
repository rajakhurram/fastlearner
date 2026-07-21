package com.vinncorp.fast_learner.request.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.subscription.CustomerAddressType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class CustomerAddressExType extends CustomerAddressType {
    private String customerAddressId;
}
