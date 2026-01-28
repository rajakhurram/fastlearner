package com.vinncorp.fast_learner.request.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.subscription.CustomerPaymentProfileBaseType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class CustomerPaymentProfileType extends CustomerPaymentProfileBaseType {
    private PaymentType payment;
}
