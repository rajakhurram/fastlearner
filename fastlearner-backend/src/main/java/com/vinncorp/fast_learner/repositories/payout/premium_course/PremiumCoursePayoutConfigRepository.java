package com.vinncorp.fast_learner.repositories.payout.premium_course;

import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutConfig;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumCoursePayoutConfigRepository extends JpaRepository<PremiumCoursePayoutConfig, Long> {
    PremiumCoursePayoutConfig findByPayoutTypeAndIsActiveTrue(PayoutType payoutType);
}
