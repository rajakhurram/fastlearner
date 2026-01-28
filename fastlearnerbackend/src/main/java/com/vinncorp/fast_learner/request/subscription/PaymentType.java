package com.vinncorp.fast_learner.request.subscription;

import lombok.Data;

@Data
public class PaymentType {
    private CreditCardType creditCard;
    private OpaqueDataType opaqueData;
}