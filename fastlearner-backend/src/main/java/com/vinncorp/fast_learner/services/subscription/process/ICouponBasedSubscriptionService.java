package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Message;

public interface ICouponBasedSubscriptionService {

    Message<String> processCouponBasedSubscription(SubscriptionRequest subscriptionRequest, SubscribedUser subscribedUser, User user)
            throws InternalServerException, EntityNotFoundException, BadRequestException;
}
