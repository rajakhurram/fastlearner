package com.vinncorp.fast_learner.response.subscription;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrentSubscriptionResponse {

    private String planName;
    private String planPrice;
    private String freeTrialMessage;
    private String planMessage;
    private boolean isSubscribed;
    private boolean isCouponBasedSubscription;
    private String planType;
    private List<String> permissions;
    private Long subscriptionId;
}
