package com.vinncorp.fast_learner.repositories.coupon;

import com.vinncorp.fast_learner.models.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query(value = """
            SELECT c.*
            FROM public.coupon c
            LEFT JOIN public.coupon_user cu ON cu.coupon_id = c.id AND cu.is_active = true
            LEFT JOIN public.coupon_email_domain ced ON ced.coupon_id = c.id AND ced.is_active = true
            LEFT JOIN public.coupon_course cc ON cc.coupon_id = c.id AND cc.is_active = true
            WHERE c.redeem_code = :couponCode
              AND CURRENT_DATE BETWEEN c.start_date AND c.end_date
              AND c.is_active = true
              AND (
                c.is_restricted = false
                OR (
                  c.is_restricted = true
                  AND (
                    (
                      -- SUBSCRIPTION logic
                      (:couponType = 'SUBSCRIPTION' AND (c.coupon_type = 'SUBSCRIPTION' OR c.coupon_type = 'BOTH')) AND (
                        (
                          NOT EXISTS (
                            SELECT 1 FROM public.coupon_user cu2
                            WHERE cu2.coupon_id = c.id AND cu2.is_active = true
                          ) AND
                          NOT EXISTS (
                            SELECT 1 FROM public.coupon_email_domain ced2
                            WHERE ced2.coupon_id = c.id AND ced2.is_active = true
                          )
                        )
                        OR EXISTS (
                          SELECT 1 FROM public.coupon_user cu2
                          WHERE cu2.coupon_id = c.id AND cu2.is_active = true AND cu2.email = :email
                        )
                        OR EXISTS (
                          SELECT 1 FROM public.coupon_email_domain ced2
                          WHERE ced2.coupon_id = c.id AND ced2.is_active = true AND ced2.domain = :emailDomain
                        )
                      )
                    )
                    OR
                    (
                      -- PREMIUM logic
                      (:couponType = 'PREMIUM' AND (c.coupon_type = 'PREMIUM' OR c.coupon_type = 'BOTH')) AND (
                        (
                          (
                            NOT EXISTS (
                              SELECT 1 FROM public.coupon_user cu2
                              WHERE cu2.coupon_id = c.id AND cu2.is_active = true
                            ) AND
                            NOT EXISTS (
                              SELECT 1 FROM public.coupon_email_domain ced2
                              WHERE ced2.coupon_id = c.id AND ced2.is_active = true
                            )
                          )
                          OR EXISTS (
                            SELECT 1 FROM public.coupon_user cu2
                            WHERE cu2.coupon_id = c.id AND cu2.is_active = true AND cu2.email = :email
                          )
                          OR EXISTS (
                            SELECT 1 FROM public.coupon_email_domain ced2
                            WHERE ced2.coupon_id = c.id AND ced2.is_active = true AND ced2.domain = :emailDomain
                          )
                        )
                        AND (
                          c.allow_all_course = true
                          OR (
                            NOT EXISTS (
                              SELECT 1 FROM public.coupon_course cc2
                              WHERE cc2.coupon_id = c.id AND cc2.is_active = true
                            )
                            OR EXISTS (
                              SELECT 1 FROM public.coupon_course cc2
                              WHERE cc2.coupon_id = c.id AND cc2.is_active = true AND cc2.course_id = :courseId
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            LIMIT 1;
            """, nativeQuery = true)
    Optional<Coupon> validateCoupon(
            String couponCode, String email, String emailDomain, String couponType, Long courseId);

    Optional<Coupon> findByRedeemCode(String code);
}
