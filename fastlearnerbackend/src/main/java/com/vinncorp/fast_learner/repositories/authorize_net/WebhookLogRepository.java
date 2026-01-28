package com.vinncorp.fast_learner.repositories.authorize_net;

import com.vinncorp.fast_learner.models.authorize_net.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
}
