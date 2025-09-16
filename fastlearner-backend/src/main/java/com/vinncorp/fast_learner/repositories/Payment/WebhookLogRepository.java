package com.vinncorp.fast_learner.repositories.Payment;

import com.vinncorp.fast_learner.models.Payment.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
}
