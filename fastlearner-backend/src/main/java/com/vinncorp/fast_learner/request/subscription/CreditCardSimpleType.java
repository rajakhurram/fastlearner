package com.vinncorp.fast_learner.request.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardSimpleType {
    private String cardNumber;
    private String expirationDate;
}
