package com.vinncorp.fast_learner.response.subscriptionpermission;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPermissionResponse {
    private Subscription subscription;
    private Long totalPremiumCourse;
    private Long remainingPremiumCourse;

    private Boolean isAvailablePremium;
}
