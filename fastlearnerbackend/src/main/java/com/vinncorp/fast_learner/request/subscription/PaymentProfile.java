package com.vinncorp.fast_learner.request.subscription;

import com.vinncorp.fast_learner.response.subscription.CreditCardMaskedType;
import com.vinncorp.fast_learner.response.subscription.CustomerAddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProfile {

    private CustomerAddressType billTo;
    private CreditCardMaskedType payment;
    private String customerPaymentProfileId;
    private Boolean defaultPaymentProfile;

}
