package com.vinncorp.fast_learner.request.payment_gateway.customer;

import lombok.Data;

@Data
public class CreateCustPaymentProfileRequest {
    private String customerProfileId;
    private String email;
    private String firstName;
    private String lastName;
    private String cvv;
    private String cardNo;
    private String cardExpiry;
    private String validationMode;
}
