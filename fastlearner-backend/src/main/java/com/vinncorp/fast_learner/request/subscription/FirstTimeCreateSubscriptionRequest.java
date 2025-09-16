package com.vinncorp.fast_learner.request.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.datatype.XMLGregorianCalendar;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FirstTimeCreateSubscriptionRequest {

    private String name;
    private String length;
    private String unit;
    private XMLGregorianCalendar startDate;
    private short totalOccurrences;
    private short trialOccurrences;
    private String amount;
    private String trialAmount;
    private String creditCardNumber;
    private String expirationDate;
    private String firstName;
    private String lastName;
}
