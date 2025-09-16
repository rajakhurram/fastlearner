package com.vinncorp.fast_learner.repositories.payout;

import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface InstructorSalesRepository extends JpaRepository<InstructorSales, Long> {

    @Query(value = """
            SELECT * FROM public.instructor_sales
            WHERE
                stripe_account_id is not null AND status = :payoutStatus
                AND creation_date >= date_trunc('month', current_date)
                AND creation_date < date_trunc('month', current_date) + interval '1 month';
            """,
    nativeQuery = true)
    List<InstructorSales> findAllForPayoutProcess(PayoutStatus payoutStatus);

    @Query(value = """
            SELECT * FROM instructor_sales 
            WHERE status = 'PENDING' AND payout_batch_id is not null 
            AND creation_date >= (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month' + INTERVAL '25 days') 
            AND creation_date < CURRENT_DATE LIMIT 1
            """, nativeQuery = true)
    Optional<InstructorSales> findUnprocessedInstructorsOfCurrentMonth();

    @Modifying
    @Transactional
    @Query(value = "UPDATE instructor_sales SET status = 'PROCESSED' WHERE status = 'PENDING' AND paypal_email is not null AND payout_batch_id = ?1", nativeQuery = true)
    int updatePendingToProcessedByBatchId(String batchId);

    @Query(value = """
        SELECT COALESCE(SUM(total_sales), 0) AS total_sales
        FROM public.instructor_sales
        WHERE instructor_id = :instructorId
        AND
        CASE
            WHEN :period = 'yearly' THEN EXTRACT(YEAR FROM creation_date) = EXTRACT(YEAR FROM CURRENT_DATE)
            WHEN :period = 'previous_year' THEN DATE_TRUNC('year', creation_date) = DATE_TRUNC('year', CURRENT_DATE) - INTERVAL '1 year'
        END;
        """, nativeQuery = true)
    Double fetchSalesByYearly(String period, Long instructorId);

    @Query(value = """
            SELECT COALESCE(SUM(total_sales), 0) AS total_sales
            FROM instructor_sales\s
            WHERE instructor_id = :instructorId
            AND
            CASE
                WHEN :period = 'monthly' THEN DATE_TRUNC('month', creation_date) = DATE_TRUNC('month', CURRENT_DATE)
                WHEN :period = 'previous_month' THEN DATE_TRUNC('month', creation_date) = DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month'
            END;
              """, nativeQuery = true)
    Double fetchSalesByMonthly(String period, Long instructorId);
}
