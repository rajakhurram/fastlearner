package com.vinncorp.fast_learner.repositories.coupon;

import com.vinncorp.fast_learner.models.coupon.CouponCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponCourseRepository extends JpaRepository<CouponCourse, Long > {
    void deleteByCouponId(Long id);
    List<CouponCourse> findByCouponId(Long couponId);
}
