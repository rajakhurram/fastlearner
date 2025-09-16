package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerPaymentProfileMaskedType {

    private String customerProfileId;
    private String customerPaymentProfileId;
    private Boolean defaultPaymentProfile;
    private PaymentMaskedType payment;
    private String taxId;
    private SubscriptionList subscriptionIds;
    private String originalNetworkTransId;
    private BigDecimal originalAuthAmount;
    protected CustomerAddressType billTo;
}
