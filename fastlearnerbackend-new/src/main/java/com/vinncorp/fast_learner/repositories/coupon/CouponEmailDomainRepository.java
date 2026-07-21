package com.vinncorp.fast_learner.repositories.coupon;

import com.vinncorp.fast_learner.models.coupon.CouponEmailDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponEmailDomainRepository extends JpaRepository<CouponEmailDomain, Long> {
    void deleteByCouponId(Long id);
    List<CouponEmailDomain> findByCouponId(Long couponId);
}
