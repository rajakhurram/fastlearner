package com.vinncorp.fast_learner.services.newsletter_subscription;

import com.vinncorp.fast_learner.repositories.newsletter_subscription.NewsletterSubscriptionRepository;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.newsletter_subscription.NewsletterSubscription;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterSubscriptionSrvice implements INewsletterSubscriptionService{

    private final NewsletterSubscriptionRepository repo;

    @Override
    public Message<String> subscribeToNewsletter(String email) throws InternalServerException, EntityAlreadyExistException {
        log.info("A user with email: " + email  + " has subscribed the newsletter.");
        if (repo.existsByEmail(email.trim().toLowerCase())) {
            throw new EntityAlreadyExistException("Newsletter already subscribed by this email: " + email);
        }
        try {
            repo.save(NewsletterSubscription.builder()
                    .email(email.trim().toLowerCase())
                    .createdAt(new Date())
                    .build());
            log.info("Newsletter subscription successfully save.");
        } catch (Exception e) {
            throw new InternalServerException("Newsletter subscription " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully subscribed the newsletter.");
    }
}
