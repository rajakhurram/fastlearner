package com.vinncorp.fast_learner.repositories.payout;

import com.vinncorp.fast_learner.models.payout.PayoutWatchTime;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.List;

public interface PayoutWatchTimeRepository extends JpaRepository<PayoutWatchTime, Long> {

    List<PayoutWatchTime> findAllByPayoutForCurrentMonthAndPayoutForCurrentYearAndStudentIdAndInstructorId(int month, int year, Long id, Long id1);

    @Query(value = """
            SELECT DISTINCT student_id FROM payout_watch_time WHERE payout_status = 'PENDING' GROUP BY student_id;
            """, nativeQuery = true)
    List<Long> findAllDistinctStudentId();

    @Query(value = """
            WITH TotalTime AS (
                             SELECT
                                 student_id,
                                 subscription_id,
                                 SUM(time_spend) AS total_time_spend,
                                 MAX(amount_share_per_day) AS max_amount_share_per_day
                             FROM
                                 payout_watch_time
                             WHERE
                                 EXTRACT(MONTH FROM CURRENT_DATE) = payout_for_current_month  -- Dynamic month check
                                 AND EXTRACT(YEAR FROM CURRENT_DATE) = payout_for_current_year  -- Dynamic year check
                             GROUP BY
                                 student_id, subscription_id
                         ),
                         InstructorShare AS (
                             SELECT
                                 p.instructor_id,
                                 p.student_id,
                                 p.stripe_id,
                                 p.subscription_id,
                                 p.time_spend,
                                 t.total_time_spend,
                                 t.max_amount_share_per_day,
                                 (p.time_spend * t.max_amount_share_per_day / t.total_time_spend) AS amount
                             FROM
                                 payout_watch_time p
                             JOIN
                                 TotalTime t
                             ON
                                 p.student_id = t.student_id AND p.subscription_id = t.subscription_id
                             WHERE
                                 EXTRACT(MONTH FROM CURRENT_DATE) = p.payout_for_current_month  -- Dynamic month check
                                 AND EXTRACT(YEAR FROM CURRENT_DATE) = p.payout_for_current_year  -- Dynamic year check
                         ),
                         InstructorTotalByStudent AS (
                             SELECT
                                 student_id,
                                 instructor_id,
                                 stripe_id,
                                 SUM(amount) AS total_amount
                             FROM
                                 InstructorShare
                             GROUP BY
                                 student_id, instructor_id, stripe_id
                         )
                         SELECT
                             instructor_id,
                             stripe_id,
                             SUM(total_amount) AS overall_total_amount
                         FROM
                             InstructorTotalByStudent
                         GROUP BY
                             instructor_id, stripe_id
                         ORDER BY
                             instructor_id;
            """, nativeQuery = true)
    List<Tuple> findAllPayoutAmountForEachInstructors();

    @Modifying
    @Query(value = """
            UPDATE payout_watch_time
            SET
                payout_calculated_at = CURRENT_DATE
            WHERE
            	EXTRACT(MONTH FROM CURRENT_DATE) = payout_for_current_month AND
            	EXTRACT(YEAR FROM CURRENT_DATE) = payout_for_current_year;
            """, nativeQuery = true)
    void updateAllPayoutForCurrentPeriod();

    @Query(
            value = """
            SELECT pw.*
            FROM payout_watch_time pw
            INNER JOIN users u ON pw.instructor_id = u.id
            WHERE pw.payout_status != :payout_status
              AND pw.created_at BETWEEN CAST(:endDate AS DATE) - INTERVAL '3 months' AND CAST(:endDate AS DATE)
              AND (u.stripe_account_id IS NOT NULL AND u.stripe_account_id != '')
            """,
            nativeQuery = true
    )
    List<PayoutWatchTime> findPendingPayoutsWithinLastThreeMonths(
            @Param("payout_status") String payout_status,
            @Param("endDate") Date endDate
    );

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE payout_watch_time 
        SET payout_status = :status, 
            external_transaction_id = :transactionId,
            settled_date = :settledDate, 
            stripe_response = :stripeResponse 
        WHERE id IN (:ids)
        """, nativeQuery = true)
    int updatePayoutDetails(
            @Param("status") String status,
            @Param("transactionId") String transactionId,
            @Param("settledDate") LocalDateTime settledDate,
            @Param("stripeResponse") String stripeResponse,
            @Param("ids") List<Long> ids
    );

    @Query(value = """
    SELECT * FROM payout_watch_time 
    WHERE CAST(created_at AS DATE) = :today 
    AND student_id = :stdId
    """, nativeQuery = true)
    List<PayoutWatchTime> findAllByStudentIdAndCreatedAt(
            @Param("stdId") Long stdId,
            @Param("today") Date today
    );
}
