package com.vinncorp.fast_learner.repositories.subscription;

import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SubscribedUserRepository extends JpaRepository<SubscribedUser, Long> {
    Optional<SubscribedUser> findByCustomerProfileId(String customerProfileId);
    Optional<SubscribedUser> findByUserEmail(String email);

    @Transactional
    @Modifying
    void deleteByUserId(Long id);

    SubscribedUser findBySubscribedId(String subscribedId);

    @Query(value = """
            SELECT
                user_id,
                CASE
                    WHEN subscription_id = 2 THEN 9
                    ELSE 6.75
                END AS subscription_fee
            FROM
                subscribed_user
            WHERE
                subscribed_id != '0000000'
                AND is_active = true
                AND subscribed_id IS NOT NULL
                AND payment_status = 'PAID'
                AND current_date >= start_date + interval '15 days';
            """,
            nativeQuery = true
    )
    List<Tuple> fetchAllPaidUsers();

    List<SubscribedUser> findAllByEndDateIsNotNull();

    List<SubscribedUser> findAllByCouponIsNotNull();
}