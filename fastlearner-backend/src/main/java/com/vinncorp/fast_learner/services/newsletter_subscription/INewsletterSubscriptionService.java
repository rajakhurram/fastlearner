package com.vinncorp.fast_learner.services.newsletter_subscription;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;

public interface INewsletterSubscriptionService {
    Message<String> subscribeToNewsletter(String email) throws InternalServerException, EntityAlreadyExistException, BadRequestException;
}
