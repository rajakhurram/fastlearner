package com.vinncorp.fast_learner.repositories.payout.premium_course;

import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutTransactionHistory;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import com.vinncorp.fast_learner.util.enums.StripeAccountStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PremiumCoursePayoutTransactionHistoryRepository extends JpaRepository<PremiumCoursePayoutTransactionHistory, Long> {
    Page<PremiumCoursePayoutTransactionHistory> findAllByCourse_Instructor_EmailOrderByCreationDateDesc(String email, Pageable pageable);

//    @Query(value = """
//    SELECT *
//    FROM premium_course_payout_transaction_history p
//    WHERE p.payout_status NOT IN (:payoutStatuses)
//    AND p.stripe_account_status = :stripeAccountStatus
//""", nativeQuery = true)
//    List<PremiumCoursePayoutTransactionHistory> findByPayoutStatusAndStripeAccStatus(
//            @Param("payoutStatuses") List<String> payoutStatuses,
//            @Param("stripeAccountStatus") String stripeAccountStatus);

            List<PremiumCoursePayoutTransactionHistory> findByPayoutStatusNotInAndStripeAccountStatus(
            List<PayoutStatus> payoutStatuses, StripeAccountStatus stripeAccountStatus);


    @Transactional
    @Modifying
    @Query(value = """
                UPDATE premium_course_payout_transaction_history
                   SET payout_status = :payoutProcess, payout_id = :payoutId, stripe_response= :stripeResponse
                   WHERE id IN (:successfulTransactionIds)
            """, nativeQuery = true)
    int markTransactionsAsProcessed(@Param("successfulTransactionIds") List<Long> successfulTransactionIds,
                                    @Param("payoutId") String payoutId,
                                    @Param("payoutProcess") String payoutProcess,
                                    @Param("stripeResponse") String stripeResponse);
}
