package com.vinncorp.fast_learner.dtos.payment;

import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.validation.subscription.subscription_request.ValidSubscriptionRequest;
import jakarta.validation.Valid;
import lombok.*;


@ValidSubscriptionRequest
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {

    private Long subscriptionId;
    private String prevAuthSubscriptionId;
    private String coupon;
    private Boolean immediatelyApply;

    @Valid
    private PaymentProfileDetailRequest paymentDetail;

}
