package com.vinncorp.fast_learner.dtos.payment.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProfile {
    private String method;
    private String cardType;
    private String cardNo;
    private String expiryDate;
    private String firstName;
    private String lastName;

}
