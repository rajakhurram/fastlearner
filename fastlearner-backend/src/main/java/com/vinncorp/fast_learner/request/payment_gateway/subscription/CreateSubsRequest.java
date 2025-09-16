package com.vinncorp.fast_learner.request.payment_gateway.subscription;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateSubsRequest {
    private short length;
    private String unit;
    private XMLGregorianCalendar startDate;
    private Short totalOccurrences;
    private Short trialOccurrences;
    private String subscriptionTypeName;
    private Double amount;
    private BigDecimal trialAmount;

    private String customerProfileId;
    private String customerPaymentProfileId;
    private String firstName;
    private String lastName;
    private String cardNumber;
    private String expirationDate;
    private String cvv;
    private String email;


}
