package com.vinncorp.fast_learner.repositories.subscription;

import com.vinncorp.fast_learner.models.subscription.SubscriptionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionLogRepository extends JpaRepository<SubscriptionLog, Long> {
    SubscriptionLog findTopByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserId(Long userId);
}
