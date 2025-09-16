package com.vinncorp.fast_learner.repositories.user;

import com.vinncorp.fast_learner.models.user.UserProfileVisit;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProfileVisitRepository extends JpaRepository<UserProfileVisit, Long> {

    Optional<UserProfileVisit> findByUser_Id(Long userId);

    @Query(value = """
            SELECT
                user_profile_id,
                CASE
                    WHEN :period = 'monthly' OR :period = 'previous_month' THEN DATE_TRUNC('month', created_date)
                    WHEN :period = 'yearly' OR :period = 'previous_year' THEN DATE_TRUNC('year', created_date)
                END AS period,
                COALESCE(COUNT(DISTINCT user_id), 0) AS total_users
            FROM
                user_profile_visit
            WHERE
                user_profile_id = :id
                AND created_date >=
                CASE
                    WHEN :period = 'monthly' THEN DATE_TRUNC('month', CURRENT_DATE)
                    WHEN :period = 'yearly' THEN DATE_TRUNC('year', CURRENT_DATE)
                    WHEN :period = 'previous_month' THEN DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month'
                    WHEN :period = 'previous_year' THEN DATE_TRUNC('year', CURRENT_DATE) - INTERVAL '1 year'
                END
                AND created_date <
                CASE
                    WHEN :period = 'monthly' THEN DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
                    WHEN :period = 'yearly' THEN DATE_TRUNC('year', CURRENT_DATE) + INTERVAL '1 year'
                    WHEN :period = 'previous_month' THEN DATE_TRUNC('month', CURRENT_DATE)
                    WHEN :period = 'previous_year' THEN DATE_TRUNC('year', CURRENT_DATE)
                END
            GROUP BY
                user_profile_id, period
            ORDER BY
                user_profile_id, period;
                        
            """,
        nativeQuery = true
    )
    List<Tuple> findMonthlyOrYearlyProfileVisitors(@Param("period") String period, @Param("id") Long id);
}
