package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.request.subscription.PaymentScheduleType;
import com.vinncorp.fast_learner.request.subscription.SubscriptionStatusEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionMaskedType {

    private String name;
    private PaymentScheduleType paymentSchedule;
    private BigDecimal amount;
    private BigDecimal trialAmount;
    private SubscriptionStatusEnum status;
    private SubscriptionCustomerProfileType profile;
    private TransactionList arbTransactions;
}
