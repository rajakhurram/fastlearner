package com.vinncorp.fast_learner.request.subscription;

import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileIdType;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class SubscriptionTypeReq {
    private String name;
    private PaymentScheduleType paymentSchedule;
    private BigDecimal amount;
    private BigDecimal trialAmount;
    private CustomerProfileIdType profile;
}
