package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardMaskedType {
    private String cardNumber;
    private String expirationDate;
    private String cardType;
    private String issuerNumber;
    private Boolean isPaymentToken;
}
