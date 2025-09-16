package com.vinncorp.fast_learner.services.subscription;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;

public interface ISubscriptionValidationsService {
    SubscriptionValidations findByValidationNameAndSubscriptionAndIsActive(String validationName, Subscription subscriptionId, Boolean isActive);
}
