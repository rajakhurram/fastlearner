package com.vinncorp.fast_learner.repositories.tansaction_history;

import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.transaction_history.TransactionHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory,Long> {

    @Query(value = """
            WITH transaction_data AS (
                SELECT *
                FROM public.transaction_history
                WHERE user_id = :studentId
                  AND (status = 'ACTIVE' OR subscription_status = 'CONTINUE')
                  AND CURRENT_DATE BETWEEN (trial_end_date - INTERVAL '1 day')
                                        AND (subscription_next_cycle + INTERVAL '1 day')
            ),
            row_count AS (
                SELECT COUNT(*) AS total_rows FROM transaction_data
            )
            SELECT *
            FROM transaction_data
            WHERE (SELECT total_rows FROM row_count) = 1
               OR (status = 'ACTIVE' AND (SELECT total_rows FROM row_count) > 1);;
            """, nativeQuery = true)
    Optional<TransactionHistory> fetchCurrentSubscription(Long studentId);
    List<TransactionHistory> findAllBySubscriptionStatusOrStatusAndUser_Id(SubscriptionStatus subscriptionStatus, GenericStatus status, Long id);
    TransactionHistory findByAuthSubscriptionId(String paymentSubscriptionId);
    @Query(value = """
            select * from transaction_history where status=:status and auth_subscription_id=:authSubscriptionId order by created_at desc limit 1
            """,nativeQuery = true)
    TransactionHistory findFirstByStatusAndAuthSubscriptionIdOrderByCreationAtDesc(
            @Param("status") String status,
            @Param("authSubscriptionId") String authSubscriptionId);

    @Query("""
    SELECT new com.vinncorp.fast_learner.response.transaction_history.TransactionHistoryResponse(
        th.id,
        th.creationAt,
        s.name,
        th.responseText,
        th.status,
        CASE
            WHEN th.coupon.id IS NOT NULL THEN (th.subscriptionAmount - (th.subscriptionAmount * (c.discount / 100.0)))
            ELSE th.subscriptionAmount
        END
    )
    FROM TransactionHistory th
    INNER JOIN subscription s ON s.id = th.subscription.id
    LEFT JOIN coupon c ON c.id = th.coupon.id
    WHERE th.user.id = :userId
    ORDER BY th.id DESC
    """)
    Page<TransactionHistoryResponse> findByUserOrderByIdDesc(@Param("userId") Long userId, Pageable pageable);


    TransactionHistory findFirstByStatusAndAuthSubscriptionIdAndSubscriptionStatusOrderByCreationAtDesc(GenericStatus genericStatus, String paymentSubscriptionId, SubscriptionStatus subscriptionStatus);


    TransactionHistory findFirstByStatusAndUserIdOrderByCreationAtDesc(GenericStatus status, Long userId);

    TransactionHistory findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(Long userId, SubscriptionStatus subscriptionStatus);
}
