package com.vinncorp.fast_learner.repositories.newsletter_subscription;

import com.vinncorp.fast_learner.models.newsletter_subscription.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {

    boolean existsByEmail(String email);
}
