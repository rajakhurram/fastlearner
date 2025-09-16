package com.vinncorp.fast_learner.repositories.subscription;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query(value = """
            SELECT * FROM public.subscription ORDER BY id
            """,nativeQuery = true)
    List<Subscription> findAllByIsActive(boolean b);
}