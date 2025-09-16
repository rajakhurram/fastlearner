package com.vinncorp.fast_learner.repositories.subscription;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionValidationsRepository extends JpaRepository<SubscriptionValidations,Long> {
    SubscriptionValidations findByValidationNameAndSubscriptionAndIsActive(String validationName, Subscription subscriptionId, Boolean isActive);
}
