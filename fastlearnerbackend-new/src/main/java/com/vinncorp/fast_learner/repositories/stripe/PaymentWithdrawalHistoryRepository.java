package com.vinncorp.fast_learner.repositories.stripe;

import com.vinncorp.fast_learner.models.stripe.PaymentWithdrawalHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWithdrawalHistoryRepository extends JpaRepository<PaymentWithdrawalHistory, Long> {

    Page<PaymentWithdrawalHistory> findByUserId(Long userId, Pageable pageable);
}
