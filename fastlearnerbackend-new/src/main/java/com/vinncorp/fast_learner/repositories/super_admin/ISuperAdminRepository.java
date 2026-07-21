package com.vinncorp.fast_learner.repositories.super_admin;

import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.vinncorp.fast_learner.repositories.super_admin.SuperAdminSqlFragments.NET_SUBSCRIPTION_PAID_AMOUNT;

@Repository
public interface ISuperAdminRepository extends JpaRepository<User, Long> {

    // ─────────────────────────────────────────────────────────────
    // User Stats
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                COUNT(DISTINCT u.id)                                                          AS total_users,
                COUNT(DISTINCT CASE WHEN s.plan_type = 'FREE' OR su.id IS NULL THEN u.id END) AS free_users,
                COUNT(DISTINCT CASE WHEN s.plan_type = 'STANDARD'               THEN u.id END) AS standard_users,
                COUNT(DISTINCT CASE WHEN s.plan_type IN ('PREMIUM','ULTIMATE')  THEN u.id END) AS total_premium_users,
                COUNT(DISTINCT CASE WHEN s.plan_type = 'PREMIUM'               THEN u.id END) AS premium_users,
                COUNT(DISTINCT CASE WHEN s.plan_type = 'ULTIMATE'              THEN u.id END) AS enterprise_users
            FROM users u
            LEFT JOIN subscribed_user su ON su.user_id = u.id AND su.is_active = true
            LEFT JOIN subscription s     ON su.subscription_id = s.id
            """, nativeQuery = true)
    Tuple getUserStats();

    // ─────────────────────────────────────────────────────────────
    // User List (paginated)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                u.id,
                u.full_name,
                u.email,
                CAST(u.created_date AS date) AS created_date,
                u.is_active,
                COALESCE(s.plan_type, 'FREE')      AS plan_type,
                s.name                              AS subscription_name,
                su.is_active                        AS subscription_active,
                su.end_date                         AS subscription_end_date
            FROM users u
            LEFT JOIN subscribed_user su ON su.user_id = u.id AND su.is_active = true
            LEFT JOIN subscription s     ON su.subscription_id = s.id
            WHERE 1=1 
                 AND ( (CAST(:accountType AS text) IS NULL  OR CAST(:accountType AS text) = ''  OR CAST(:accountType AS text) = 'ALL' ) 
                OR (:accountType = 'ACTIVE' AND u.is_active = TRUE)
                OR (:accountType = 'INACTIVE' AND (u.is_active IS NULL OR u.is_active = false))
                )
                   AND (CAST(:search AS text) IS NULL
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR u.email     ILIKE CONCAT('%', :search, '%')
                OR CAST(u.id AS TEXT) LIKE CONCAT('%', :search, '%'))
              AND (
                CAST(:planType AS text) IS NULL
                OR COALESCE(s.plan_type, 'FREE') = :planType
                OR (:planType = 'ULTIMATE' AND COALESCE(s.plan_type, 'FREE') = 'ULTIMATE')
              )
              AND (CAST(:dateFrom AS date) IS NULL OR CAST(u.created_date AS date) >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo AS date)   IS NULL OR CAST(u.created_date AS date) <= CAST(:dateTo AS date))
              AND (CAST(:subscriptionStatus AS text) IS NULL
                OR (:subscriptionStatus = 'None'    AND (su.is_active IS NULL OR su.is_active = false))
                OR (:subscriptionStatus = 'Expired' AND su.is_active = true AND su.end_date < NOW())
                OR (:subscriptionStatus = 'Active'  AND su.is_active = true AND su.end_date >= NOW()))
            ORDER BY u.created_date DESC
            """,
            countQuery = """
            SELECT COUNT(u.id)
            FROM users u
            LEFT JOIN subscribed_user su ON su.user_id = u.id AND su.is_active = true
            LEFT JOIN subscription s     ON su.subscription_id = s.id
            WHERE 1=1 
                          
                AND ( ( CAST(:accountType AS text) IS NULL OR CAST(:accountType AS text) = ''  OR CAST(:accountType AS text) = 'ALL' ) 
                OR (:accountType = 'ACTIVE' AND u.is_active = TRUE)
                OR (:accountType = 'INACTIVE' AND (u.is_active IS NULL OR u.is_active = false))
                )
                
                AND (CAST(:search AS text) IS NULL
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR u.email     ILIKE CONCAT('%', :search, '%')
                OR CAST(u.id AS TEXT) LIKE CONCAT('%', :search, '%'))
              AND (
                CAST(:planType AS text) IS NULL
                OR COALESCE(s.plan_type, 'FREE') = :planType
                OR (:planType = 'ULTIMATE' AND COALESCE(s.plan_type, 'FREE') = 'ULTIMATE')
              )
              AND (CAST(:dateFrom AS date) IS NULL OR CAST(u.created_date AS date) >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo AS date)   IS NULL OR CAST(u.created_date AS date) <= CAST(:dateTo AS date))
              AND (CAST(:subscriptionStatus AS text) IS NULL
                OR (:subscriptionStatus = 'None'    AND (su.is_active IS NULL OR su.is_active = false))
                OR (:subscriptionStatus = 'Expired' AND su.is_active = true AND su.end_date < NOW())
                OR (:subscriptionStatus = 'Active'  AND su.is_active = true AND su.end_date >= NOW()))
            """,
            nativeQuery = true)
    Page<Tuple> findAllUsersForAdmin(@Param("search") String search,
                                     @Param("planType") String planType,
                                     @Param("subscriptionStatus") String subscriptionStatus,
                                     @Param("dateFrom") Date dateFrom,
                                     @Param("dateTo") Date dateTo,
                                     @Param("accountType") String accountType,
                                     Pageable pageable);


    @Query(value = """
    SELECT
        u.id,
        u.full_name,
        u.email,
        CAST(u.created_date AS date) AS created_date,
        u.is_active,

        CASE 
            WHEN COALESCE(s.plan_type, 'FREE') = 'ULTIMATE' THEN 'ENTERPRISE'
            ELSE COALESCE(s.plan_type, 'FREE')
        END AS plan_type,

        s.name AS subscription_name,
        su.is_active AS subscription_active,
        su.end_date AS subscription_end_date

    FROM users u
    LEFT JOIN subscribed_user su 
        ON su.user_id = u.id AND su.is_active = true
    LEFT JOIN subscription s 
        ON su.subscription_id = s.id

    WHERE 1=1 

        AND (
            (CAST(:accountType AS text) IS NULL OR :accountType = '' OR :accountType = 'ALL')
            OR (:accountType = 'ACTIVE' AND u.is_active = TRUE)
            OR (:accountType = 'INACTIVE' AND (u.is_active IS NULL OR u.is_active = false))
        )

        AND (
            CAST(:search AS text) IS NULL
            OR u.full_name ILIKE CONCAT('%', :search, '%')
            OR u.email ILIKE CONCAT('%', :search, '%')
            OR CAST(u.id AS TEXT) LIKE CONCAT('%', :search, '%')
        )

        AND (
            CAST(:planType AS text) IS NULL 
            OR CASE 
                WHEN COALESCE(s.plan_type, 'FREE') = 'ULTIMATE' THEN 'ENTERPRISE'
                ELSE COALESCE(s.plan_type, 'FREE')
            END = :planType
        )

        AND (CAST(:dateFrom AS date) IS NULL 
            OR CAST(u.created_date AS date) >= CAST(:dateFrom AS date))

        AND (CAST(:dateTo AS date) IS NULL 
            OR CAST(u.created_date AS date) <= CAST(:dateTo AS date))

        AND (
            CAST(:subscriptionStatus AS text) IS NULL
            OR (:subscriptionStatus = 'None' AND (su.is_active IS NULL OR su.is_active = false))
            OR (:subscriptionStatus = 'Expired' AND su.is_active = true AND su.end_date < NOW())
            OR (:subscriptionStatus = 'Active' AND su.is_active = true AND su.end_date >= NOW())
        )

    ORDER BY u.created_date DESC
    """,
            nativeQuery = true)
    List<Tuple> findAllUsersForExport(
            @Param("search") String search,
            @Param("planType") String planType,
            @Param("subscriptionStatus") String subscriptionStatus,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("accountType") String accountType
    );

    // ─────────────────────────────────────────────────────────────
    // User Overview (single user detail)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                u.id,
                u.full_name,
                u.email,
                u.is_active,
                s.name                                                              AS subscription_name,
                COALESCE(s.plan_type, 'FREE')                                       AS plan_type,
                su.start_date                                                       AS subscription_start_date,
                su.end_date                                                         AS subscription_end_date,
                su.payment_status,
                (SELECT COUNT(*) FROM enrollment e
                 WHERE e.student_id = u.id AND e.is_active = true)                  AS courses_enrolled,
                (SELECT COALESCE(SUM(
                    ROUND(CAST((
                    CASE
                        WHEN th.coupon_id IS NOT NULL THEN
                            CASE
                                WHEN cp.discount_unit = 'FIXED_AMOUNT' THEN GREATEST(
                                    COALESCE(th.subscription_amount, 0) - COALESCE(cp.discount, 0),
                                    0
                                )
                                ELSE GREATEST(
                                    COALESCE(th.subscription_amount, 0)
                                    - (
                                        COALESCE(th.subscription_amount, 0)
                                        * (COALESCE(cp.discount, 0) / 100.0)
                                    ),
                                    0
                                )
                            END
                        ELSE COALESCE(th.subscription_amount, 0)
                    END
                    ) AS numeric), 2)
                ), 0)
                 FROM transaction_history th
                 LEFT JOIN coupon cp ON cp.id = th.coupon_id
                 WHERE th.user_id = u.id)               AS total_spent
            FROM users u
            LEFT JOIN subscribed_user su ON su.user_id = u.id AND su.is_active = true
            LEFT JOIN subscription s     ON su.subscription_id = s.id
            WHERE u.id = :userId
            """, nativeQuery = true)
    Optional<Tuple> findUserOverview(@Param("userId") Long userId);

    // ─────────────────────────────────────────────────────────────
    // User Subscriptions tab
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                s.id                AS subscription_id,
                s.name              AS plan_name,
                s.plan_type,
                ROUND(CAST((
                CASE
                    WHEN th_pay.subscription_amount IS NOT NULL THEN
                        CASE
                            WHEN th_pay.coupon_id IS NOT NULL THEN
                                CASE
                                    WHEN cp.discount_unit = 'FIXED_AMOUNT'
                                        THEN GREATEST(th_pay.subscription_amount - COALESCE(cp.discount, 0), 0)
                                    ELSE GREATEST(
                                        th_pay.subscription_amount
                                        - (th_pay.subscription_amount * (COALESCE(cp.discount, 0) / 100.0)),
                                        0
                                    )
                                END
                            ELSE th_pay.subscription_amount
                        END
                    ELSE s.price
                END
                ) AS numeric), 2)                 AS price,
                su.start_date,
                su.end_date,
                su.payment_status,
                su.is_active        AS subscription_active
            FROM subscribed_user su
            JOIN subscription s ON su.subscription_id = s.id
            LEFT JOIN LATERAL (
                SELECT th.subscription_amount, th.coupon_id
                FROM transaction_history th
                WHERE th.user_id = su.user_id
                  AND th.subscription_id = su.subscription_id
                ORDER BY th.created_at DESC
                LIMIT 1
            ) th_pay ON true
            LEFT JOIN coupon cp ON cp.id = th_pay.coupon_id
            WHERE su.user_id = :userId
            ORDER BY su.start_date DESC
            """, nativeQuery = true)
    List<Tuple> findUserSubscriptions(@Param("userId") Long userId);

    // ─────────────────────────────────────────────────────────────
    // User Courses tab  (progress = completed topics / total topics)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                c.id                        AS course_id,
                c.title                     AS course_title,
                u.full_name                 AS instructor_name,
                CASE
                    WHEN total_topics.cnt = 0 THEN 0
                    ELSE ROUND((done_topics.cnt * 100.0) / total_topics.cnt)
                END                         AS progress_percent
            FROM enrollment e
            JOIN course c ON c.id = e.course_id
            LEFT JOIN users u ON u.id = c.instructor_id
            LEFT JOIN LATERAL (
                SELECT COUNT(*) AS cnt
                FROM topic t
                JOIN section sec ON sec.id = t.section_id
                WHERE sec.course_id = c.id
            ) total_topics ON true
            LEFT JOIN LATERAL (
                SELECT COUNT(*) AS cnt
                FROM user_course_progress ucp
                WHERE ucp.course_id = c.id
                  AND ucp.student_id = e.student_id
                  AND ucp.is_completed = true
            ) done_topics ON true
            WHERE e.student_id = :userId AND e.is_active = true
            ORDER BY e.enrolled_date DESC
            """, nativeQuery = true)
    List<Tuple> findUserCourses(@Param("userId") Long userId);

    // ─────────────────────────────────────────────────────────────
    // User Transactions tab
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                th.id,
                COALESCE(th.external_transaction_id, CONCAT('TXN-', th.id)) AS transaction_id,
                """ + NET_SUBSCRIPTION_PAID_AMOUNT + """
                                                         AS amount,
                th.created_at                                               AS transaction_date,
                CAST(th.subscription_status AS text)                        AS subscription_status
            FROM transaction_history th
            LEFT JOIN coupon cp ON cp.id = th.coupon_id
            WHERE th.user_id = :userId
            ORDER BY th.created_at DESC
            """, nativeQuery = true)
    List<Tuple> findUserTransactions(@Param("userId") Long userId);

    // ─────────────────────────────────────────────────────────────
    // Subscription Stats
    // ─────────────────────────────────────────────────────────────

//    @Query(value = """
//            SELECT
//                COUNT(CASE WHEN su.is_active = true AND su.end_date >= NOW() THEN 1 END)                                          AS active_subscriptions,
//                COUNT(CASE WHEN s.plan_type = 'STANDARD' AND su.is_active = true AND su.end_date >= NOW() THEN 1 END)             AS standard_plan_count,
//                COUNT(CASE WHEN s.plan_type IN ('PREMIUM','ULTIMATE') AND su.is_active = true AND su.end_date >= NOW() THEN 1 END) AS premium_plan_count
//            FROM subscribed_user su
//            JOIN subscription s ON s.id = su.subscription_id
//            """, nativeQuery = true)
//    Tuple getSubscriptionStats();

    @Query(value = """
        SELECT
            COUNT(su.id)                                                        AS active_subscriptions,
            COUNT(CASE WHEN s.plan_type = 'STANDARD' THEN 1 END)               AS standard_plan_count,
            COUNT(CASE WHEN s.plan_type = 'PREMIUM' THEN 1 END)                AS premium_plan_count,
            COUNT(CASE WHEN s.plan_type = 'ULTIMATE' THEN 1 END)               AS enterprise_plan_count
        FROM subscribed_user su
        JOIN subscription s ON s.id = su.subscription_id
        WHERE su.is_active = true
        AND s.plan_type != 'FREE'
        """, nativeQuery = true)
    Tuple getSubscriptionStats();

    // ─────────────────────────────────────────────────────────────
    // Subscriptions List (paginated)
    // ─────────────────────────────────────────────────────────────

//    @Query(value = """
//            SELECT
//                su.id,
//                u.full_name                 AS user_name,
//                u.email                     AS user_email,
//                s.name                      AS plan_name,
//                s.duration_in_word          AS billing_cycle,
//                s.price                     AS amount,
//                su.start_date,
//                CASE
//                    WHEN su.is_active = false THEN NULL
//                    WHEN su.end_date < NOW()  THEN NULL
//                    ELSE su.end_date
//                END                         AS renewal_date,
//                CASE
//                    WHEN su.is_active = false THEN 'Cancelled'
//                    WHEN su.end_date < NOW()  THEN 'Expired'
//                    ELSE 'Active'
//                END                         AS status
//            FROM subscribed_user su
//            JOIN users u        ON u.id  = su.user_id
//            JOIN subscription s ON s.id  = su.subscription_id
//            WHERE (CAST(:search AS text) IS NULL
//                OR u.full_name ILIKE CONCAT('%', :search, '%')
//                OR u.email     ILIKE CONCAT('%', :search, '%')
//                OR CAST(su.id AS TEXT) LIKE CONCAT('%', :search, '%'))
//              AND (CAST(:planType AS text) IS NULL OR s.plan_type = :planType)
//              AND (CAST(:status AS text) IS NULL
//                OR (:status = 'Cancelled' AND su.is_active = false)
//                OR (:status = 'Expired'   AND su.is_active = true AND su.end_date < NOW())
//                OR (:status = 'Active'    AND su.is_active = true AND su.end_date >= NOW()))
//              AND (CAST(:billingCycle AS text) IS NULL OR LOWER(s.duration_in_word) = LOWER(:billingCycle))
//              AND (CAST(:dateFrom AS date) IS NULL OR su.start_date >= CAST(:dateFrom AS date))
//              AND (CAST(:dateTo AS date)   IS NULL OR su.start_date <= CAST(:dateTo AS date))
//            ORDER BY su.start_date DESC
//            """,
//            countQuery = """
//            SELECT COUNT(su.id)
//            FROM subscribed_user su
//            JOIN users u        ON u.id  = su.user_id
//            JOIN subscription s ON s.id  = su.subscription_id
//            WHERE (CAST(:search AS text) IS NULL
//                OR u.full_name ILIKE CONCAT('%', :search, '%')
//                OR u.email     ILIKE CONCAT('%', :search, '%')
//                OR CAST(su.id AS TEXT) LIKE CONCAT('%', :search, '%'))
//              AND (CAST(:planType AS text) IS NULL OR s.plan_type = :planType)
//              AND (CAST(:status AS text) IS NULL
//                OR (:status = 'Cancelled' AND su.is_active = false)
//                OR (:status = 'Expired'   AND su.is_active = true AND su.end_date < NOW())
//                OR (:status = 'Active'    AND su.is_active = true AND su.end_date >= NOW()))
//              AND (CAST(:billingCycle AS text) IS NULL OR LOWER(s.duration_in_word) = LOWER(:billingCycle))
//              AND (CAST(:dateFrom AS date) IS NULL OR su.start_date >= CAST(:dateFrom AS date))
//              AND (CAST(:dateTo AS date)   IS NULL OR su.start_date <= CAST(:dateTo AS date))
//            """,
//            nativeQuery = true)
//    Page<Tuple> findAllSubscriptionsForAdmin(@Param("search") String search,
//                                             @Param("planType") String planType,
//                                             @Param("status") String status,
//                                             @Param("billingCycle") String billingCycle,
//                                             @Param("dateFrom") Date dateFrom,
//                                             @Param("dateTo") Date dateTo,
//                                             Pageable pageable);




    @Query(value = """
        SELECT
            su.id,
            u.full_name                 AS user_name,
            u.email                     AS user_email,
            s.name                      AS plan_name,
            s.duration_in_word          AS billing_cycle,
            ROUND(CAST((
            CASE
                WHEN th_latest.subscription_amount IS NOT NULL THEN
                    CASE
                        WHEN th_latest.coupon_id IS NOT NULL THEN
                            CASE
                                WHEN cp_latest.discount_unit = 'FIXED_AMOUNT'
                                    THEN GREATEST(th_latest.subscription_amount - COALESCE(cp_latest.discount, 0), 0)
                                ELSE GREATEST(
                                    th_latest.subscription_amount
                                    - (th_latest.subscription_amount * (COALESCE(cp_latest.discount, 0) / 100.0)),
                                    0
                                )
                            END
                        ELSE th_latest.subscription_amount
                    END
                ELSE s.price
            END
            ) AS numeric), 2)                         AS amount,
            su.start_date,
            CASE
                WHEN su.is_active = false THEN NULL
                WHEN su.end_date IS NOT NULL AND su.end_date < NOW() THEN NULL
                WHEN s.plan_type = 'FREE' THEN NULL
                ELSE COALESCE(su.end_date, th_latest.subscription_next_cycle)
            END                         AS renewal_date,
            CASE
                WHEN su.is_active = false THEN 'Cancelled'
                WHEN su.is_active = true AND su.end_date IS NOT NULL AND su.end_date >= NOW() THEN 'Cancelled'
                WHEN su.end_date < NOW()  THEN 'Expired'
                ELSE 'Active'
            END                         AS status
        FROM subscribed_user su
        JOIN users u        ON u.id  = su.user_id
        JOIN subscription s ON s.id  = su.subscription_id
        LEFT JOIN (
            SELECT DISTINCT ON (th.user_id, th.subscription_id)
                th.user_id,
                th.subscription_id,
                th.subscription_next_cycle,
                th.subscription_amount,
                th.coupon_id
            FROM transaction_history th
            ORDER BY th.user_id, th.subscription_id, th.created_at DESC
        ) th_latest ON th_latest.user_id = su.user_id
                   AND th_latest.subscription_id = su.subscription_id
        LEFT JOIN coupon cp_latest ON cp_latest.id = th_latest.coupon_id
        WHERE (CAST(:search AS text) IS NULL
            OR u.full_name ILIKE CONCAT('%', :search, '%')
            OR u.email     ILIKE CONCAT('%', :search, '%')
            OR CAST(su.id AS TEXT) LIKE CONCAT('%', :search, '%'))
          AND (CAST(:planType AS text) IS NULL OR s.plan_type = :planType)
          AND (CAST(:status AS text) IS NULL
            OR (:status = 'Cancelled' AND (
                    su.is_active = false
                    OR (su.is_active = true AND su.end_date IS NOT NULL AND su.end_date >= NOW())
               ))
            OR (:status = 'Expired'   AND su.is_active = true AND su.end_date IS NOT NULL AND su.end_date < NOW())
            OR (:status = 'Active'    AND su.is_active = true AND su.end_date IS NULL))
          AND (CAST(:billingCycle AS text) IS NULL
            OR (:billingCycle = 'MONTHLY' AND s.duration = 1)
            OR (:billingCycle = 'YEARLY'  AND s.duration = 12)
            OR (:billingCycle = 'Per Month' AND s.duration = 1)
            OR (:billingCycle = 'Per Year'  AND s.duration = 12)
            OR LOWER(s.duration_in_word) = LOWER(:billingCycle))
          AND (CAST(:dateFrom AS date) IS NULL OR CAST(su.start_date AS date) >= CAST(:dateFrom AS date))
          AND (CAST(:dateTo AS date)   IS NULL OR CAST(su.start_date AS date) <= CAST(:dateTo AS date))
        ORDER BY su.start_date DESC
        """,
            countQuery = """
SELECT COUNT(*)
FROM subscribed_user su
JOIN users u        ON u.id  = su.user_id
JOIN subscription s ON s.id  = su.subscription_id
WHERE (CAST(:search AS text) IS NULL
    OR u.full_name ILIKE CONCAT('%', :search, '%')
    OR u.email     ILIKE CONCAT('%', :search, '%')
    OR CAST(su.id AS TEXT) LIKE CONCAT('%', :search, '%'))
  AND (CAST(:planType AS text) IS NULL OR s.plan_type = :planType)
  AND (CAST(:status AS text) IS NULL
    OR (:status = 'Cancelled' AND (
            su.is_active = false
            OR (su.is_active = true AND su.end_date IS NOT NULL AND su.end_date >= NOW())
       ))
    OR (:status = 'Expired'   AND su.is_active = true AND su.end_date IS NOT NULL AND su.end_date < NOW())
    OR (:status = 'Active'    AND su.is_active = true AND su.end_date IS NULL))
  AND (CAST(:billingCycle AS text) IS NULL
    OR (:billingCycle = 'MONTHLY' AND s.duration = 1)
    OR (:billingCycle = 'YEARLY'  AND s.duration = 12)
    OR (:billingCycle = 'Per Month' AND s.duration = 1)
    OR (:billingCycle = 'Per Year'  AND s.duration = 12)
    OR LOWER(s.duration_in_word) = LOWER(:billingCycle))
  AND (CAST(:dateFrom AS date) IS NULL OR CAST(su.start_date AS date) >= CAST(:dateFrom AS date))
  AND (CAST(:dateTo AS date)   IS NULL OR CAST(su.start_date AS date) <= CAST(:dateTo AS date))
""",
            nativeQuery = true)
    Page<Tuple> findAllSubscriptionsForAdmin(@Param("search") String search,
                                             @Param("planType") String planType,
                                             @Param("status") String status,
                                             @Param("billingCycle") String billingCycle,
                                             @Param("dateFrom") Date dateFrom,
                                             @Param("dateTo") Date dateTo,
                                             Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Subscription Invoices (invoices by subscribed_user id)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                th.id,
                th.external_transaction_id AS invoice_id,
                u.full_name          AS user_name,
                u.email              AS user_email,
                """ + NET_SUBSCRIPTION_PAID_AMOUNT + """
                AS amount,
                'Subscription'       AS type,
                s.name               AS plan_name,
                s.plan_type,
                CAST(th.subscription_status AS text)                        AS subscription_status,
                CAST(th.created_at AS date)  AS invoice_date
            FROM transaction_history th
            JOIN users u         ON u.id  = th.user_id
            JOIN subscribed_user su ON su.user_id = th.user_id AND su.subscription_id = th.subscription_id
            LEFT JOIN subscription s ON s.id = th.subscription_id
            LEFT JOIN coupon cp ON cp.id = th.coupon_id
            WHERE su.id = :subscriptionId
              AND (:search   IS NULL
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(th.external_transaction_id) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:type     IS NULL OR 'Subscription'    = :type)
              AND (:planType IS NULL OR s.plan_type       = :planType)
              AND (:status   IS NULL
                OR (CAST(:status AS text) = 'SUCCESS' AND CAST(th.subscription_status AS text) IN ('SUCCESS','CONTINUE'))
                OR (CAST(:status AS text) <> 'SUCCESS' AND CAST(th.subscription_status AS text) = CAST(:status AS text)))
              AND (CAST(:dateFrom AS date) IS NULL OR CAST(th.created_at AS date) >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo   AS date) IS NULL OR CAST(th.created_at AS date) <= CAST(:dateTo   AS date))
            ORDER BY th.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(th.id)
            FROM transaction_history th
            JOIN users u         ON u.id  = th.user_id
            JOIN subscribed_user su ON su.user_id = th.user_id AND su.subscription_id = th.subscription_id
            LEFT JOIN subscription s ON s.id = th.subscription_id
            LEFT JOIN coupon cp ON cp.id = th.coupon_id
            WHERE su.id = :subscriptionId
              AND (:search   IS NULL
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(th.external_transaction_id) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:type     IS NULL OR 'Subscription'    = :type)
              AND (:planType IS NULL OR s.plan_type       = :planType)
              AND (:status   IS NULL
                OR (CAST(:status AS text) = 'SUCCESS' AND CAST(th.subscription_status AS text) IN ('SUCCESS','CONTINUE'))
                OR (CAST(:status AS text) <> 'SUCCESS' AND CAST(th.subscription_status AS text) = CAST(:status AS text)))
              AND (CAST(:dateFrom AS date) IS NULL OR CAST(th.created_at AS date) >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo   AS date) IS NULL OR CAST(th.created_at AS date) <= CAST(:dateTo   AS date))
            """,
            nativeQuery = true)
    Page<Tuple> findInvoicesBySubscriptionId(@Param("subscriptionId") Long subscriptionId,
                                             @Param("search") String search,
                                             @Param("type") String type,
                                             @Param("planType") String planType,
                                             @Param("status") String status,
                                             @Param("dateFrom") String dateFrom,
                                             @Param("dateTo") String dateTo,
                                             Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Invoice Detail
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                th.id,
                COALESCE(th.external_transaction_id, CONCAT('INV-', th.id))    AS invoice_id,
                u.full_name                                                      AS user_name,
                u.email                                                          AS user_email,
                s.name                                                           AS plan_name,
                th.subscription_amount                                           AS subtotal,
                """ + NET_SUBSCRIPTION_PAID_AMOUNT + """
                AS total,
                CAST(th.subscription_status AS text)                             AS subscription_status,
                th.created_at                                                   AS invoice_date,
                th.updated_date,
                COALESCE(th.external_transaction_id, th.auth_subscription_id)   AS transaction_id,
                su.id                                                            AS subscribed_user_id
            FROM transaction_history th
            JOIN users u ON u.id = th.user_id
            LEFT JOIN subscription s ON s.id = th.subscription_id
            LEFT JOIN coupon cp ON cp.id = th.coupon_id
            LEFT JOIN LATERAL (
                SELECT su2.id FROM subscribed_user su2
                WHERE su2.user_id = th.user_id
                  AND su2.subscription_id = th.subscription_id
                ORDER BY su2.start_date DESC
                LIMIT 1
            ) su ON true
            WHERE th.id = :invoiceId
            """, nativeQuery = true)
    Optional<Tuple> findInvoiceDetail(@Param("invoiceId") Long invoiceId);

    // ─────────────────────────────────────────────────────────────
    // Courses list (paginated)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                c.id,
                c.title,
                c.course_status     AS status,
                u.id                AS instructor_id,
                u.full_name         AS instructor_name,
                cc.name             AS category_name,
                c.course_type       AS course_type,
                (SELECT COUNT(*) FROM enrollment e
                 WHERE e.course_id = c.id AND e.is_active = true)           AS student_count,
                (SELECT AVG(cr.rating) FROM course_review cr
                 WHERE cr.course_id = c.id)                                 AS avg_rating
            FROM course c
            LEFT JOIN users u           ON u.id = c.instructor_id
            LEFT JOIN course_category cc ON cc.id = c.course_category_id
            WHERE c.course_status != 'DELETE'
              AND (CAST(:search AS text) IS NULL
                OR c.title ILIKE CONCAT('%', :search, '%')
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR CAST(c.id AS TEXT) LIKE CONCAT('%', :search, '%'))
              AND (CAST(:status AS text) IS NULL OR c.course_status = :status)
              AND (CAST(:category AS bigint) IS NULL OR cc.id = CAST(:category AS bigint))
              AND (CAST(:instructor AS bigint) IS NULL OR u.id = CAST(:instructor AS bigint))
            ORDER BY c.id DESC
            """,
            countQuery = """
            SELECT COUNT(c.id)
            FROM course c
            LEFT JOIN users u            ON u.id = c.instructor_id
            LEFT JOIN course_category cc ON cc.id = c.course_category_id
            WHERE c.course_status != 'DELETE'
              AND (CAST(:search AS text) IS NULL
                OR c.title ILIKE CONCAT('%', :search, '%')
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR CAST(c.id AS TEXT) LIKE CONCAT('%', :search, '%'))
              AND (CAST(:status AS text) IS NULL OR c.course_status = :status)
              AND (CAST(:category AS bigint) IS NULL OR cc.id = CAST(:category AS bigint))
              AND (CAST(:instructor AS bigint) IS NULL OR u.id = CAST(:instructor AS bigint))
            """,
            nativeQuery = true)
    Page<Tuple> findAllCoursesForAdmin(@Param("search") String search,
                                       @Param("status") String status,
                                       @Param("category") String category,
                                       @Param("instructor") String instructor,
                                       Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Payments List (paginated)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
        SELECT
            combined.id,
            combined.transaction_id,
            combined.user_name,
            combined.user_email,
            combined.plan_type,
            combined.amount,
            CAST(combined.payment_ts AS date) AS payment_date,
            combined.type,
            combined.subscription_status
        FROM (
            SELECT
                th.id,
                COALESCE(th.external_transaction_id, CONCAT('TXN-', th.id)) AS transaction_id,
                u.full_name AS user_name,
                u.email AS user_email,
                CAST(s.plan_type AS text) AS plan_type,

                """ + NET_SUBSCRIPTION_PAID_AMOUNT + """
                AS amount,

                th.created_at AS payment_ts,
                'Subscription' AS type,
                CAST(th.subscription_status AS text) AS subscription_status

            FROM transaction_history th
            INNER JOIN users u ON u.id = th.user_id
            INNER JOIN subscription s ON s.id = th.subscription_id
            LEFT JOIN coupon cp ON cp.id = th.coupon_id

            WHERE CAST(s.plan_type AS text) != 'FREE'

            UNION ALL

            SELECT
                pcth.id,
                pcth.transaction_id,
                u.full_name,
                u.email,
                CAST(c.course_type AS text) AS plan_type,
                COALESCE(c.price, 0) AS amount,
                pcth.created_date AS payment_ts,
                'Course' AS type,
                'PAID' AS subscription_status

            FROM (
                SELECT DISTINCT ON (p.transaction_id)
                    p.id,
                    p.transaction_id,
                    p.student_id,
                    p.course_id,
                    p.created_date
                FROM premium_course_payout_transaction_history p
                ORDER BY p.transaction_id, p.id DESC
            ) pcth

            INNER JOIN users u ON u.id = pcth.student_id
            INNER JOIN course c ON c.id = pcth.course_id

        ) combined

        WHERE (
                CAST(:search AS text) IS NULL
                OR combined.user_name ILIKE CONCAT('%', CAST(:search AS text), '%')
                OR combined.user_email ILIKE CONCAT('%', CAST(:search AS text), '%')
                OR combined.transaction_id ILIKE CONCAT('%', CAST(:search AS text), '%')
                OR CAST(combined.amount AS text) LIKE CONCAT('%', CAST(:search AS text), '%')
        )

        AND (
                CAST(:planType AS text) IS NULL
                OR combined.plan_type = CAST(:planType AS text)
        )

        AND (
                :status IS NULL
                OR (
                    CAST(:status AS text) = 'SUCCESS'
                    AND combined.subscription_status IN ('SUCCESS', 'CONTINUE', 'PAID')
                )
                OR (
                    CAST(:status AS text) <> 'SUCCESS'
                    AND combined.subscription_status = CAST(:status AS text)
                )
        )

        AND (
                CAST(:type AS text) IS NULL
                OR combined.type = CAST(:type AS text)
                OR (
                    CAST(:type AS text) = 'Course Purchase'
                    AND combined.type = 'Course'
                )
        )

        AND (
                CAST(:dateFrom AS date) IS NULL
                OR CAST(combined.payment_ts AS date) >= CAST(:dateFrom AS date)
        )

        AND (
                CAST(:dateTo AS date) IS NULL
                OR CAST(combined.payment_ts AS date) <= CAST(:dateTo AS date)
        )

        ORDER BY combined.payment_ts DESC
        """,

            countQuery = """
        SELECT COUNT(*)
        FROM (

            SELECT
                th.id

            FROM transaction_history th
            INNER JOIN users u ON u.id = th.user_id
            INNER JOIN subscription s ON s.id = th.subscription_id
            LEFT JOIN coupon cp ON cp.id = th.coupon_id

            WHERE CAST(s.plan_type AS text) != 'FREE'

            AND (
                    CAST(:search AS text) IS NULL
                    OR u.full_name ILIKE CONCAT('%', CAST(:search AS text), '%')
                    OR u.email ILIKE CONCAT('%', CAST(:search AS text), '%')
                    OR COALESCE(th.external_transaction_id, CONCAT('TXN-', th.id))
                        ILIKE CONCAT('%', CAST(:search AS text), '%')
                    OR CAST(th.subscription_amount AS text)
                        LIKE CONCAT('%', CAST(:search AS text), '%')
            )

            AND (
                    CAST(:planType AS text) IS NULL
                    OR CAST(s.plan_type AS text) = CAST(:planType AS text)
            )

            AND (
                    :status IS NULL
                    OR (
                        CAST(:status AS text) = 'SUCCESS'
                        AND CAST(th.subscription_status AS text)
                            IN ('SUCCESS', 'CONTINUE')
                    )
                    OR (
                        CAST(:status AS text) <> 'SUCCESS'
                        AND CAST(th.subscription_status AS text)
                            = CAST(:status AS text)
                    )
            )

            AND (
                    CAST(:type AS text) IS NULL
                    OR CAST(:type AS text) = 'Subscription'
            )

            AND (
                    CAST(:dateFrom AS date) IS NULL
                    OR CAST(th.created_at AS date) >= CAST(:dateFrom AS date)
            )

            AND (
                    CAST(:dateTo AS date) IS NULL
                    OR CAST(th.created_at AS date) <= CAST(:dateTo AS date)
            )

            UNION ALL

            SELECT
                pcth.id

            FROM (
                SELECT DISTINCT ON (p.transaction_id)
                    p.id,
                    p.transaction_id,
                    p.student_id,
                    p.course_id,
                    p.created_date
                FROM premium_course_payout_transaction_history p
                ORDER BY p.transaction_id, p.id DESC
            ) pcth

            INNER JOIN users u ON u.id = pcth.student_id
            INNER JOIN course c ON c.id = pcth.course_id

            WHERE (
                    CAST(:search AS text) IS NULL
                    OR u.full_name ILIKE CONCAT('%', CAST(:search AS text), '%')
                    OR u.email ILIKE CONCAT('%', CAST(:search AS text), '%')
                    OR pcth.transaction_id ILIKE CONCAT('%', CAST(:search AS text), '%')
                    OR CAST(c.price AS text) LIKE CONCAT('%', CAST(:search AS text), '%')
            )

            AND (
                    CAST(:planType AS text) IS NULL
                    OR CAST(c.course_type AS text) = CAST(:planType AS text)
            )

            AND (
                    :status IS NULL
                    OR CAST(:status AS text) = 'SUCCESS'
            )

            AND (
                    CAST(:type AS text) IS NULL
                    OR CAST(:type AS text) = 'Course'
                    OR CAST(:type AS text) = 'Course Purchase'
            )

            AND (
                    CAST(:dateFrom AS date) IS NULL
                    OR CAST(pcth.created_date AS date) >= CAST(:dateFrom AS date)
            )

            AND (
                    CAST(:dateTo AS date) IS NULL
                    OR CAST(pcth.created_date AS date) <= CAST(:dateTo AS date)
            )

        ) counted
        """,
            nativeQuery = true)
    Page<Tuple> findAllPaymentsForAdmin(@Param("search") String search,
                                        @Param("planType") String planType,
                                        @Param("status") String status,
                                        @Param("type") String type,
                                        @Param("dateFrom") Date dateFrom,
                                        @Param("dateTo") Date dateTo,
                                        Pageable pageable);

    @Query(value = """
        SELECT
            combined.id,
            combined.transaction_id,
            combined.user_name,
            combined.user_email,
            combined.plan_type,
            combined.amount,
            CAST(combined.payment_ts AS date) AS payment_date,
            combined.type,
            combined.subscription_status
        FROM (
            SELECT
                th.id,
                COALESCE(th.external_transaction_id, CONCAT('TXN-', th.id)) AS transaction_id,
                u.full_name AS user_name,
                u.email AS user_email,
                CAST(s.plan_type AS text) AS plan_type,
                """ + NET_SUBSCRIPTION_PAID_AMOUNT + """
                AS amount,
                th.created_at AS payment_ts,
                'Subscription' AS type,
                CAST(th.subscription_status AS text) AS subscription_status
            FROM transaction_history th
            INNER JOIN users u ON u.id = th.user_id
            INNER JOIN subscription s ON s.id = th.subscription_id
            LEFT JOIN coupon cp ON cp.id = th.coupon_id
            WHERE CAST(s.plan_type AS text) != 'FREE'

            UNION ALL

            SELECT
                pcth.id,
                pcth.transaction_id,
                u.full_name,
                u.email,
                CAST(c.course_type AS text) AS plan_type,
                COALESCE(c.price, 0) AS amount,
                pcth.created_date AS payment_ts,
                'Course' AS type,
                'PAID' AS subscription_status
            FROM (
                SELECT DISTINCT ON (p.transaction_id)
                    p.id,
                    p.transaction_id,
                    p.student_id,
                    p.course_id,
                    p.created_date
                FROM premium_course_payout_transaction_history p
                ORDER BY p.transaction_id, p.id DESC
            ) pcth
            INNER JOIN users u ON u.id = pcth.student_id
            INNER JOIN course c ON c.id = pcth.course_id
        ) combined
        WHERE (
                CAST(:search AS text) IS NULL
                OR combined.user_name ILIKE CONCAT('%', CAST(:search AS text), '%')
                OR combined.user_email ILIKE CONCAT('%', CAST(:search AS text), '%')
                OR combined.transaction_id ILIKE CONCAT('%', CAST(:search AS text), '%')
                OR CAST(combined.amount AS text) LIKE CONCAT('%', CAST(:search AS text), '%')
        )
        AND (
                CAST(:planType AS text) IS NULL
                OR combined.plan_type = CAST(:planType AS text)
        )
        AND (
                :status IS NULL
                OR (
                    CAST(:status AS text) = 'SUCCESS'
                    AND combined.subscription_status IN ('SUCCESS', 'CONTINUE', 'PAID')
                )
                OR (
                    CAST(:status AS text) <> 'SUCCESS'
                    AND combined.subscription_status = CAST(:status AS text)
                )
        )
        AND (
                CAST(:type AS text) IS NULL
                OR combined.type = CAST(:type AS text)
                OR (
                    CAST(:type AS text) = 'Course Purchase'
                    AND combined.type = 'Course'
                )
        )
        AND (
                CAST(:dateFrom AS date) IS NULL
                OR CAST(combined.payment_ts AS date) >= CAST(:dateFrom AS date)
        )
        AND (
                CAST(:dateTo AS date) IS NULL
                OR CAST(combined.payment_ts AS date) <= CAST(:dateTo AS date)
        )
        ORDER BY combined.payment_ts DESC
        """, nativeQuery = true)
    List<Tuple> findAllPaymentsForExport(@Param("search") String search,
                                         @Param("planType") String planType,
                                         @Param("status") String status,
                                         @Param("type") String type,
                                         @Param("dateFrom") Date dateFrom,
                                         @Param("dateTo") Date dateTo);

    // ─────────────────────────────────────────────────────────────
    // Enrollments List (paginated)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                e.id,
                u.full_name                                                         AS student_name,
                u.email                                                             AS student_email,
                c.id                                                                AS course_id,
                c.title                                                             AS course_title,
                CAST(e.enrolled_date AS date)                                       AS enrolled_date,
                e.is_active,
                CASE
                    WHEN COALESCE(tt.cnt, 0) = 0 THEN 0
                    ELSE ROUND((COALESCE(dt.cnt, 0) * 100.0) / tt.cnt)
                END                                                                 AS progress_percent
            FROM enrollment e
            JOIN users u  ON u.id = e.student_id
            JOIN course c ON c.id = e.course_id
            LEFT JOIN (
                SELECT sec.course_id, COUNT(*) AS cnt
                FROM topic t
                JOIN section sec ON sec.id = t.section_id
                GROUP BY sec.course_id
            ) tt ON tt.course_id = c.id
            LEFT JOIN (
                SELECT ucp.course_id, ucp.student_id, COUNT(*) AS cnt
                FROM user_course_progress ucp
                WHERE ucp.is_completed = true
                GROUP BY ucp.course_id, ucp.student_id
            ) dt ON dt.course_id = c.id AND dt.student_id = e.student_id
            WHERE (CAST(:search AS text) IS NULL
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR u.email     ILIKE CONCAT('%', :search, '%')
                OR c.title     ILIKE CONCAT('%', :search, '%')
                OR CAST(e.id AS TEXT) LIKE CONCAT('%', :search, '%'))
              AND (CAST(:courseId AS bigint) IS NULL OR c.id = CAST(:courseId AS bigint))
              AND (CAST(:status AS text) IS NULL
                OR (:status = 'Inactive'  AND e.is_active = false)
                OR (:status = 'Completed' AND e.is_active = true
                    AND COALESCE(tt.cnt, 0) > 0
                    AND COALESCE(dt.cnt, 0) >= COALESCE(tt.cnt, 0))
                OR (:status = 'Active'    AND e.is_active = true
                    AND (COALESCE(tt.cnt, 0) = 0 OR COALESCE(dt.cnt, 0) < COALESCE(tt.cnt, 0)))
                OR (:status = 'enrolled'  AND e.is_active = true
                    AND COALESCE(dt.cnt, 0) = 0)
                OR (:status = 'inprogress' AND e.is_active = true
                    AND COALESCE(dt.cnt, 0) > 0 AND COALESCE(dt.cnt, 0) < COALESCE(tt.cnt, 0)))
              AND (CAST(:progress AS text) IS NULL
                OR (:progress = 'notStarted' AND (COALESCE(tt.cnt, 0) = 0 OR COALESCE(dt.cnt, 0) = 0))
                OR (:progress = 'inProgress' AND COALESCE(dt.cnt, 0) > 0 AND COALESCE(dt.cnt, 0) < COALESCE(tt.cnt, 0))
                OR (:progress = 'completed'   AND COALESCE(tt.cnt, 0) > 0 AND COALESCE(dt.cnt, 0) >= COALESCE(tt.cnt, 0)))
            ORDER BY e.enrolled_date DESC
            """,
            countQuery = """
            SELECT COUNT(e.id)
            FROM enrollment e
            JOIN users u  ON u.id = e.student_id
            JOIN course c ON c.id = e.course_id
            LEFT JOIN (
                SELECT sec.course_id, COUNT(*) AS cnt
                FROM topic t
                JOIN section sec ON sec.id = t.section_id
                GROUP BY sec.course_id
            ) tt ON tt.course_id = c.id
            LEFT JOIN (
                SELECT ucp.course_id, ucp.student_id, COUNT(*) AS cnt
                FROM user_course_progress ucp
                WHERE ucp.is_completed = true
                GROUP BY ucp.course_id, ucp.student_id
            ) dt ON dt.course_id = c.id AND dt.student_id = e.student_id
            WHERE (CAST(:search AS text) IS NULL
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR u.email     ILIKE CONCAT('%', :search, '%')
                OR c.title     ILIKE CONCAT('%', :search, '%')
                OR CAST(e.id AS TEXT) LIKE CONCAT('%', :search, '%'))
              AND (CAST(:courseId AS bigint) IS NULL OR c.id = CAST(:courseId AS bigint))
              AND (CAST(:status AS text) IS NULL
                OR (:status = 'Inactive'  AND e.is_active = false)
                OR (:status = 'Completed' AND e.is_active = true
                    AND COALESCE(tt.cnt, 0) > 0
                    AND COALESCE(dt.cnt, 0) >= COALESCE(tt.cnt, 0))
                OR (:status = 'Active'    AND e.is_active = true
                    AND (COALESCE(tt.cnt, 0) = 0 OR COALESCE(dt.cnt, 0) < COALESCE(tt.cnt, 0)))
                OR (:status = 'enrolled'  AND e.is_active = true
                    AND COALESCE(dt.cnt, 0) = 0)
                OR (:status = 'inprogress' AND e.is_active = true
                    AND COALESCE(dt.cnt, 0) > 0 AND COALESCE(dt.cnt, 0) < COALESCE(tt.cnt, 0)))
              AND (CAST(:progress AS text) IS NULL
                OR (:progress = 'notStarted' AND (COALESCE(tt.cnt, 0) = 0 OR COALESCE(dt.cnt, 0) = 0))
                OR (:progress = 'inProgress' AND COALESCE(dt.cnt, 0) > 0 AND COALESCE(dt.cnt, 0) < COALESCE(tt.cnt, 0))
                OR (:progress = 'completed'   AND COALESCE(tt.cnt, 0) > 0 AND COALESCE(dt.cnt, 0) >= COALESCE(tt.cnt, 0)))
            """,
            nativeQuery = true)
    Page<Tuple> findAllEnrollmentsForAdmin(@Param("search") String search,
                                           @Param("courseId") Long courseId,
                                           @Param("status") String status,
                                           @Param("progress") String progress,
                                           Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Course Detail
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                c.id,
                c.title,
                c.description,
                c.course_status     AS status,
                c.price,
                c.course_type       AS course_type,
                u.id                AS instructor_id,
                u.full_name         AS instructor_name,
                u.email             AS instructor_email,
                cc.name             AS category_name,
                c.created_date      AS created_date,
                (SELECT COUNT(*) FROM enrollment e
                 WHERE e.course_id = c.id AND e.is_active = true) AS student_count,
                (SELECT AVG(cr.rating) FROM course_review cr
                 WHERE cr.course_id = c.id)                       AS avg_rating
            FROM course c
            LEFT JOIN users u            ON u.id  = c.instructor_id
            LEFT JOIN course_category cc ON cc.id = c.course_category_id
            WHERE c.id = :courseId
            """, nativeQuery = true)
    Optional<Tuple> findCourseDetail(@Param("courseId") Long courseId);

    // ─────────────────────────────────────────────────────────────
    // Invoices list (paginated)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT combined.id,
                   combined.invoice_id,
                   combined.user_name,
                   combined.user_email,
                   combined.amount,
                   combined.type,
                   combined.plan_name,
                   combined.plan_type,
                   combined.subscription_status,
                   combined.invoice_date
            FROM (
                SELECT
                    th.id,
                    COALESCE(th.external_transaction_id, CONCAT('INV-', th.id)) AS invoice_id,
                    u.full_name          AS user_name,
                    u.email              AS user_email,
                    """ + NET_SUBSCRIPTION_PAID_AMOUNT + """
                    AS amount,
                    'Subscription'       AS type,
                    s.name               AS plan_name,
                    s.plan_type          AS plan_type,
                    CAST(th.subscription_status AS text) AS subscription_status,
                    th.creation_at       AS invoice_date
                FROM transaction_history th
                JOIN users u         ON u.id = th.user_id
                LEFT JOIN subscription s ON s.id = th.subscription_id
                LEFT JOIN coupon cp ON cp.id = th.coupon_id

                UNION ALL

                SELECT
                    cp.id,
                    cp.transaction_id    AS invoice_id,
                    u.full_name          AS user_name,
                    u.email              AS user_email,
                    c.price              AS amount,
                    'Course Purchase'    AS type,
                    c.title              AS plan_name,
                    NULL                 AS plan_type,
                    'PAID'               AS subscription_status,
                    cp.created_date      AS invoice_date
                FROM (
                    SELECT DISTINCT ON (p.transaction_id) p.*
                    FROM premium_course_payout_transaction_history p
                    ORDER BY p.transaction_id, p.id
                ) cp
                JOIN users u ON u.id = cp.student_id
                JOIN course c ON c.id = cp.course_id
            ) combined
            WHERE (:search   IS NULL
                OR LOWER(combined.user_email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(combined.invoice_id) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:type     IS NULL OR combined.type          = :type)
              AND (:planType IS NULL OR combined.plan_type     = :planType)
              AND (:status   IS NULL
                OR (CAST(:status AS text) = 'SUCCESS' AND combined.subscription_status IN ('SUCCESS', 'CONTINUE', 'PAID'))
                OR (CAST(:status AS text) <> 'SUCCESS' AND combined.subscription_status = CAST(:status AS text)))
              AND (:dateFrom IS NULL OR combined.invoice_date  >= :dateFrom)
              AND (:dateTo   IS NULL OR combined.invoice_date  <= :dateTo)
            ORDER BY combined.invoice_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT th.id
                FROM transaction_history th
                JOIN users u         ON u.id = th.user_id
                LEFT JOIN subscription s ON s.id = th.subscription_id
                WHERE (:search   IS NULL
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(th.external_transaction_id) LIKE LOWER(CONCAT('%', :search, '%')))
                  AND (:type     IS NULL OR 'Subscription'     = :type)
                  AND (:planType IS NULL OR s.plan_type        = :planType)
                  AND (:status   IS NULL
                    OR (CAST(:status AS text) = 'SUCCESS' AND CAST(th.subscription_status AS text) IN ('SUCCESS', 'CONTINUE'))
                    OR (CAST(:status AS text) <> 'SUCCESS' AND CAST(th.subscription_status AS text) = CAST(:status AS text)))
                  AND (:dateFrom IS NULL OR th.creation_at     >= :dateFrom)
                  AND (:dateTo   IS NULL OR th.creation_at     <= :dateTo)

                UNION ALL

                SELECT cp.id
                FROM (
                    SELECT DISTINCT ON (p.transaction_id) p.*
                    FROM premium_course_payout_transaction_history p
                    ORDER BY p.transaction_id, p.id
                ) cp
                JOIN users u ON u.id = cp.student_id
                JOIN course c ON c.id = cp.course_id
                WHERE (:search   IS NULL
                    OR LOWER(u.email)        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(cp.transaction_id) LIKE LOWER(CONCAT('%', :search, '%')))
                  AND (:type     IS NULL OR 'Course Purchase'  = :type)
                  AND (:planType IS NULL)
                  AND (:status   IS NULL OR CAST(:status AS text) = 'SUCCESS')
                  AND (:dateFrom IS NULL OR cp.created_date    >= :dateFrom)
                  AND (:dateTo   IS NULL OR cp.created_date    <= :dateTo)
            ) counted
            """,
            nativeQuery = true)
    Page<Tuple> findAllInvoicesForAdmin(@Param("search") String search,
                                        @Param("type") String type,
                                        @Param("planType") String planType,
                                        @Param("status") String status,
                                        @Param("dateFrom") Date dateFrom,
                                        @Param("dateTo") Date dateTo,
                                        Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Payout Stats
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                (SELECT COALESCE(SUM(amount), 0)      FROM premium_course_payout_transaction_history)        +
                (SELECT COALESCE(SUM(total_sales), 0) FROM instructor_sales)                                  AS total_earnings,

                (SELECT COALESCE(SUM(amount), 0)      FROM premium_course_payout_transaction_history WHERE payout_status = 'PENDING') +
                (SELECT COALESCE(SUM(total_sales), 0) FROM instructor_sales WHERE status = 'PENDING')         AS pending_payouts,

                (SELECT COALESCE(SUM(amount), 0)      FROM premium_course_payout_transaction_history WHERE payout_status = 'PROCESSED') +
                (SELECT COALESCE(SUM(total_sales), 0) FROM instructor_sales WHERE status = 'PROCESSED')       AS total_paid_out,

                (SELECT COALESCE(SUM(amount), 0) FROM premium_course_payout_transaction_history
                 WHERE payout_status = 'PROCESSED'
                   AND EXTRACT(MONTH FROM created_date) = EXTRACT(MONTH FROM NOW())
                   AND EXTRACT(YEAR  FROM created_date) = EXTRACT(YEAR  FROM NOW())) +
                (SELECT COALESCE(SUM(total_sales), 0) FROM instructor_sales
                 WHERE status = 'PROCESSED'
                   AND EXTRACT(MONTH FROM creation_date) = EXTRACT(MONTH FROM NOW())
                   AND EXTRACT(YEAR  FROM creation_date) = EXTRACT(YEAR  FROM NOW()))                         AS this_month_payouts
            """, nativeQuery = true)
    Tuple getPayoutStats();

    // ─────────────────────────────────────────────────────────────
    // Payouts List (paginated)
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            WITH course_earnings AS (
                SELECT
                    c.instructor_id,
                    COALESCE(SUM(p.amount), 0)                                                          AS total_earned,
                    COALESCE(SUM(CASE WHEN p.payout_status = 'PROCESSED' THEN p.amount ELSE 0 END), 0)  AS paid,
                    COALESCE(SUM(CASE WHEN p.payout_status != 'PROCESSED' THEN p.amount ELSE 0 END), 0) AS pending,
                    MAX(CASE WHEN p.payout_status = 'PROCESSED' THEN p.created_date END)                AS last_payout_date
                FROM premium_course_payout_transaction_history p
                JOIN course c ON c.id = p.course_id
                WHERE (CAST(:dateFrom AS date) IS NULL OR CAST(p.created_date AS date) >= CAST(:dateFrom AS date))
                  AND (CAST(:dateTo   AS date) IS NULL OR CAST(p.created_date AS date) <= CAST(:dateTo   AS date))
                GROUP BY c.instructor_id
            ),
            subscription_earnings AS (
                SELECT
                    ins.instructor_id,
                    COALESCE(SUM(ins.total_sales), 0)                                                           AS total_earned,
                    COALESCE(SUM(CASE WHEN ins.status = 'PROCESSED' THEN ins.total_sales ELSE 0 END), 0)        AS paid,
                    COALESCE(SUM(CASE WHEN ins.status != 'PROCESSED' THEN ins.total_sales ELSE 0 END), 0)       AS pending,
                    MAX(CASE WHEN ins.status = 'PROCESSED' THEN ins.creation_date END)                          AS last_payout_date
                FROM instructor_sales ins
                WHERE (CAST(:dateFrom AS date) IS NULL OR CAST(ins.creation_date AS date) >= CAST(:dateFrom AS date))
                  AND (CAST(:dateTo   AS date) IS NULL OR CAST(ins.creation_date AS date) <= CAST(:dateTo   AS date))
                GROUP BY ins.instructor_id
            ),
            combined AS (
                SELECT
                    COALESCE(ce.instructor_id, se.instructor_id)                    AS instructor_id,
                    COALESCE(ce.total_earned, 0) + COALESCE(se.total_earned, 0)     AS total_earnings,
                    COALESCE(ce.paid, 0)         + COALESCE(se.paid, 0)             AS paid,
                    COALESCE(ce.pending, 0)      + COALESCE(se.pending, 0)          AS pending,
                    CASE
                        WHEN ce.last_payout_date IS NULL THEN se.last_payout_date
                        WHEN se.last_payout_date IS NULL THEN ce.last_payout_date
                        ELSE GREATEST(ce.last_payout_date, se.last_payout_date)
                    END                                                              AS last_payout_date
                FROM course_earnings ce
                FULL OUTER JOIN subscription_earnings se ON se.instructor_id = ce.instructor_id
            )
            SELECT
                u.id,
                u.full_name,
                u.email,
                c.total_earnings,
                c.paid,
                c.pending,
                c.last_payout_date,
                CASE
                    WHEN c.paid = 0                    THEN 'Pending'
                    WHEN c.paid >= c.total_earnings    THEN 'Paid'
                    ELSE 'Partially Paid'
                END AS status
            FROM combined c
            JOIN users u ON u.id = c.instructor_id
            WHERE (CAST(:search AS text) IS NULL
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR u.email     ILIKE CONCAT('%', :search, '%'))
              AND (CAST(:status AS text) IS NULL
                OR (:status = 'Pending'       AND c.paid = 0)
                OR (:status = 'Paid'          AND c.paid >= c.total_earnings)
                OR (:status = 'Partially Paid' AND c.paid > 0 AND c.paid < c.total_earnings))
            ORDER BY c.total_earnings DESC
            """,
            countQuery = """
            WITH course_earnings AS (
                SELECT c.instructor_id,
                    COALESCE(SUM(p.amount), 0) AS total_earned,
                    COALESCE(SUM(CASE WHEN p.payout_status = 'PROCESSED' THEN p.amount ELSE 0 END), 0) AS paid
                FROM premium_course_payout_transaction_history p
                JOIN course c ON c.id = p.course_id
                WHERE (CAST(:dateFrom AS date) IS NULL OR CAST(p.created_date AS date) >= CAST(:dateFrom AS date))
                  AND (CAST(:dateTo   AS date) IS NULL OR CAST(p.created_date AS date) <= CAST(:dateTo   AS date))
                GROUP BY c.instructor_id
            ),
            subscription_earnings AS (
                SELECT ins.instructor_id,
                    COALESCE(SUM(ins.total_sales), 0) AS total_earned,
                    COALESCE(SUM(CASE WHEN ins.status = 'PROCESSED' THEN ins.total_sales ELSE 0 END), 0) AS paid
                FROM instructor_sales ins
                WHERE (CAST(:dateFrom AS date) IS NULL OR CAST(ins.creation_date AS date) >= CAST(:dateFrom AS date))
                  AND (CAST(:dateTo   AS date) IS NULL OR CAST(ins.creation_date AS date) <= CAST(:dateTo   AS date))
                GROUP BY ins.instructor_id
            ),
            combined AS (
                SELECT COALESCE(ce.instructor_id, se.instructor_id) AS instructor_id,
                    COALESCE(ce.total_earned, 0) + COALESCE(se.total_earned, 0) AS total_earnings,
                    COALESCE(ce.paid, 0) + COALESCE(se.paid, 0) AS paid
                FROM course_earnings ce
                FULL OUTER JOIN subscription_earnings se ON se.instructor_id = ce.instructor_id
            )
            SELECT COUNT(*)
            FROM combined c
            JOIN users u ON u.id = c.instructor_id
            WHERE (CAST(:search AS text) IS NULL
                OR u.full_name ILIKE CONCAT('%', :search, '%')
                OR u.email     ILIKE CONCAT('%', :search, '%'))
              AND (CAST(:status AS text) IS NULL
                OR (:status = 'Pending'        AND c.paid = 0)
                OR (:status = 'Paid'           AND c.paid >= c.total_earnings)
                OR (:status = 'Partially Paid' AND c.paid > 0 AND c.paid < c.total_earnings))
            """,
            nativeQuery = true)
    Page<Tuple> findAllPayoutsForAdmin(@Param("search") String search,
                                       @Param("status") String status,
                                       @Param("dateFrom") Date dateFrom,
                                       @Param("dateTo") Date dateTo,
                                       Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Payout Detail — instructor summary
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                u.id,
                u.full_name,
                u.email,
                COALESCE(ce.total_earned, 0) + COALESCE(se.total_earned, 0) AS total_earnings,
                COALESCE(ce.paid, 0)         + COALESCE(se.paid, 0)         AS paid
            FROM users u
            LEFT JOIN (
                SELECT c.instructor_id,
                    COALESCE(SUM(p.amount), 0)                                                         AS total_earned,
                    COALESCE(SUM(CASE WHEN p.payout_status = 'PROCESSED' THEN p.amount ELSE 0 END), 0) AS paid
                FROM premium_course_payout_transaction_history p
                JOIN course c ON c.id = p.course_id
                WHERE c.instructor_id = :instructorId
                GROUP BY c.instructor_id
            ) ce ON ce.instructor_id = u.id
            LEFT JOIN (
                SELECT ins.instructor_id,
                    COALESCE(SUM(ins.total_sales), 0)                                                          AS total_earned,
                    COALESCE(SUM(CASE WHEN ins.status = 'PROCESSED' THEN ins.total_sales ELSE 0 END), 0)       AS paid
                FROM instructor_sales ins
                WHERE ins.instructor_id = :instructorId
                GROUP BY ins.instructor_id
            ) se ON se.instructor_id = u.id
            WHERE u.id = :instructorId
            """, nativeQuery = true)
    Optional<Tuple> findPayoutDetailByInstructor(@Param("instructorId") Long instructorId);

    // ─────────────────────────────────────────────────────────────
    // Payout Detail — course earnings breakdown
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                c.id                                                                                    AS course_id,
                c.title                                                                                 AS course_title,
                COALESCE(SUM(p.amount), 0)                                                             AS total_earned,
                COALESCE(SUM(CASE WHEN p.payout_status = 'PROCESSED' THEN p.amount ELSE 0 END), 0)    AS paid,
                COALESCE(SUM(CASE WHEN p.payout_status != 'PROCESSED' THEN p.amount ELSE 0 END), 0)   AS pending
            FROM premium_course_payout_transaction_history p
            JOIN course c ON c.id = p.course_id
            WHERE c.instructor_id = :instructorId
            GROUP BY c.id, c.title
            ORDER BY total_earned DESC
            """, nativeQuery = true)
    List<Tuple> findPayoutCourseBreakdown(@Param("instructorId") Long instructorId);

    // ─────────────────────────────────────────────────────────────
    // Payout Detail — payout history
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT amount, 'Course' AS type, p.created_date AS payout_date, payout_status AS status
            FROM premium_course_payout_transaction_history p
            JOIN course c ON c.id = p.course_id
            WHERE c.instructor_id = :instructorId AND p.payout_status = 'PROCESSED'

            UNION ALL

            SELECT total_sales AS amount, 'Subscription' AS type, creation_date AS payout_date, status
            FROM instructor_sales
            WHERE instructor_id = :instructorId AND status = 'PROCESSED'

            ORDER BY payout_date DESC
            """, nativeQuery = true)
    List<Tuple> findPayoutHistory(@Param("instructorId") Long instructorId);

    // ─────────────────────────────────────────────────────────────
    // Instructors dropdown list
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT u.id, u.full_name
            FROM users u
            JOIN role r ON r.id = u.role_id
            WHERE r.type = 'INSTRUCTOR'
              AND (u.is_active = true OR u.is_active IS NULL)
            ORDER BY u.full_name ASC
            """, nativeQuery = true)
    List<Tuple> findAllInstructors();

    // ─────────────────────────────────────────────────────────────
    // Settings — Admin Users list
    // ─────────────────────────────────────────────────────────────

    @Query(value = """
            SELECT
                u.id,
                u.full_name,
                u.email,
                r.type          AS role,
                u.is_active,
                u.login_timestamp
            FROM users u
            JOIN role r ON r.id = u.role_id
            WHERE r.type = 'SUPER_ADMIN'
            ORDER BY u.id ASC
            """, nativeQuery = true)
    List<Tuple> findAllAdminUsers();
}

