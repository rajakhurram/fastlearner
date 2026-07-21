package com.vinncorp.fast_learner.repositories.super_admin;

/**
 * Reusable native SQL fragments for Super Admin payment amount calculations.
 * Expects {@code th} = transaction_history and {@code cp} = coupon aliases.
 * Uses CAST(... AS numeric) instead of ::numeric so Hibernate does not treat :: as a named parameter.
 */
public final class SuperAdminSqlFragments {

    private SuperAdminSqlFragments() {
    }

    public static final String NET_SUBSCRIPTION_PAID_AMOUNT = """
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
            ) AS numeric), 2)""";
}
