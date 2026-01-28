package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDetailsType {
    private String transId;
    private String refTransId;
    private String splitTenderId;
    private XMLGregorianCalendar submitTimeUTC;
    private XMLGregorianCalendar submitTimeLocal;
    private String transactionType;
    private String transactionStatus;
    private int responseCode;
    private CustomerAddressType billTo;
    private int responseReasonCode;
    private SubscriptionPaymentType subscription;
    private String responseReasonDescription;
    private String authCode;
    private String avsResponse;
    private String cardCodeResponse;
    private String cavvResponse;
    private String fdsFilterAction;
    private BigDecimal requestedAmount;
    private BigDecimal authAmount;
    private BigDecimal settleAmount;
    private BigDecimal prepaidBalanceRemaining;
    private Boolean taxExempt;
    private PaymentMaskedType payment;
    private CustomerDataType customer;
    private Boolean recurringBilling;
    private String customerIP;
    private String product;
    private String entryMode;
    private String marketType;
    private String mobileDeviceId;
    private String customerSignature;
    private CustomerProfileIdType profile;
    private String networkTransId;
    private String originalNetworkTransId;
    private BigDecimal originalAuthAmount;
    private String authorizationIndicator;
}
