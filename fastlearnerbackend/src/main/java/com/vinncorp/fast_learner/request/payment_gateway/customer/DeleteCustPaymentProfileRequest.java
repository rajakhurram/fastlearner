package com.vinncorp.fast_learner.request.payment_gateway.customer;

import lombok.Data;

@Data
public class DeleteCustPaymentProfileRequest {
    private String customerProfileId;
    private String customerPaymentProfileId;
}
