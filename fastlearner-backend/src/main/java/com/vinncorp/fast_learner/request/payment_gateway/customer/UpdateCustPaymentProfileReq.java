package com.vinncorp.fast_learner.request.payment_gateway.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateCustPaymentProfileReq {
    private String email;
    private String firstName;
    private String lastName;
    private String company;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String customerPaymentProfileId;
    private String expiryDate;
    private String cardNumber;
    private String cvv;
    private Boolean defaultPaymentProfile;
    private String customerProfileId;
    private String validationMode;
}
