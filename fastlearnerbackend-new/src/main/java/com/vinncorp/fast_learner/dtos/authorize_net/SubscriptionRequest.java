package com.vinncorp.fast_learner.dtos.authorize_net;

import com.vinncorp.fast_learner.dtos.authorize_net.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.validation.subscription.subscription_request.ValidSubscriptionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private Double amount;
    private Boolean immediatelyApply;

    @Valid
    private PaymentProfileDetailRequest paymentDetail;

}
