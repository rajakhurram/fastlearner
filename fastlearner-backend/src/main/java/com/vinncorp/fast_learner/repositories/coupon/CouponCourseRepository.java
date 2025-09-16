package com.vinncorp.fast_learner.repositories.coupon;

import com.vinncorp.fast_learner.models.coupon.CouponCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponCourseRepository extends JpaRepository<CouponCourse, Long > {
    void deleteByCouponId(Long id);
}
