package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileIdType;
import lombok.Data;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSummaryType {
    protected String transId;

    protected XMLGregorianCalendar submitTimeUTC;
    protected XMLGregorianCalendar submitTimeLocal;
    protected String transactionStatus;
    protected String invoiceNumber;
    protected String firstName;
    protected String lastName;

    protected String accountType;

    protected String accountNumber;

    protected BigDecimal settleAmount;
    protected String marketType;
    protected String product;
    protected String mobileDeviceId;
    protected SubscriptionPaymentType subscription;
    protected Boolean hasReturnedItems;
    protected CustomerProfileIdType profile;
}
