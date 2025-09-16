package com.vinncorp.fast_learner.request.subscription;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;

@Data
public class ValidateCustomerPaymentProfileRequest {
    private String customerProfileId;

    private String customerPaymentProfileId;
    private String customerShippingAddressId;
    private String cardCode;
}
