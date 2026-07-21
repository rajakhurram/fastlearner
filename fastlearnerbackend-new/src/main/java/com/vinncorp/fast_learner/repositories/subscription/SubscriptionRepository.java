package com.vinncorp.fast_learner.repositories.subscription;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query(value = """
            SELECT * FROM public.subscription ORDER BY id
            """,nativeQuery = true)
    List<Subscription> findAllByIsActive(boolean b);

    @Query(value = """
                    SELECT * FROM public.subscription
                                                              WHERE duration = CASE
                                                                  WHEN :cycle = 'MONTHLY' THEN 1
                                                                  WHEN :cycle = 'YEARLY' THEN 12
                                                              END
                                                              AND is_active = true
                                                              """, nativeQuery = true

    )
    List<Subscription> findAllByDuration(@Param("cycle") String cycle);
}