package com.vinncorp.fast_learner.services.subscription;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ISubscriptionService {
    Message<List<Subscription>> fetchAllSubscription(String email) throws EntityNotFoundException;

    Message<Subscription> findBySubscriptionId(Long subscriptionId) throws EntityNotFoundException;

    Message<Boolean> isSubscibed(String name) throws EntityNotFoundException;
}
