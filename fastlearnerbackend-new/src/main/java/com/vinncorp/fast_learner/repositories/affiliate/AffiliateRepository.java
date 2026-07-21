package com.vinncorp.fast_learner.repositories.affiliate;

import com.vinncorp.fast_learner.models.affiliate.Affiliate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AffiliateRepository extends JpaRepository<Affiliate,Long> {
    Affiliate findByEmail(String email);

    Affiliate findByStripeAccountId(String accountUrl);
}
