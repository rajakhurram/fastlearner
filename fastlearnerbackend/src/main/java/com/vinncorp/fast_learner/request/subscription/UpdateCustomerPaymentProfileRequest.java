package com.vinncorp.fast_learner.request.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCustomerPaymentProfileRequest {
    private String customerProfileId;
    private PaymentProfile paymentProfile;
    private String validationMode;

}
