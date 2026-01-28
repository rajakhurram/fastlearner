package com.vinncorp.fast_learner.repositories.coupon;

import com.vinncorp.fast_learner.models.coupon.CouponUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUserRepository extends JpaRepository<CouponUser, Long> {
    void deleteByCouponId(Long id);
}
