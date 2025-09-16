package com.vinncorp.fast_learner.response.subscription;

import lombok.Data;

@Data
public class SubscriptionCustomerProfileType extends CustomerProfileExType{
    private CustomerPaymentProfileMaskedType paymentProfile;
}
