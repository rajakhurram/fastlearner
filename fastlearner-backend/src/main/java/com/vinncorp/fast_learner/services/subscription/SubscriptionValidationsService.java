package com.vinncorp.fast_learner.services.subscription;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionValidationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionValidationsService implements ISubscriptionValidationsService {
    private final SubscriptionValidationsRepository repo;
    @Override
    public SubscriptionValidations findByValidationNameAndSubscriptionAndIsActive(String validationName, Subscription subscriptionId, Boolean isActive) {
        log.info("Attempting to find SubscriptionValidation with validationName: {}, subscriptionId: {}, isActive: {}",
                validationName, subscriptionId != null ? subscriptionId : "null", isActive);

        SubscriptionValidations subscriptionValidations = repo.findByValidationNameAndSubscriptionAndIsActive(validationName, subscriptionId, isActive);

        if (subscriptionValidations != null) {
            log.info("Found SubscriptionValidation: {}", subscriptionValidations);
            return subscriptionValidations;
        }

        log.info("No SubscriptionValidation found for validationName: {}, subscriptionId: {}, isActive: {}",
                validationName, subscriptionId != null ? subscriptionId: "null", isActive);
        return null;
    }

}
