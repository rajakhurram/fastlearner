package com.vinncorp.fast_learner.mock.coupon;

import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
import com.vinncorp.fast_learner.util.enums.CouponType;

import java.util.Date;
import java.util.List;

public class CouponTestData {

    public static final List<Long> SPECIFIC_COURSES = List.of(1L, 2L, 3L);
    public static final List<String> SPECIFIC_DOMAINS = List.of("yopmail.com", "mailinator.com");
    public static final List<String> SPECIFIC_EMAILS = List.of("qasim@mailinator.com");

    public static CouponRequest couponRequest() {
        var request = new CouponRequest();
        request.setCoupon("SUBS10");
        request.setDiscount(10.0d);
        request.setSubscriptionId(2L);
        request.setCouponType(CouponType.SUBSCRIPTION.name());
        request.setStartDate(new Date());
        request.setEndDate(new Date(System.currentTimeMillis() + 172800000)); // 2 days
        request.setDurationInMonth(1);
        request.setSpecifiedCourses(SPECIFIC_COURSES);
        request.setSpecifiedEmailDomains(SPECIFIC_DOMAINS);
        request.setSpecifiedUsers(SPECIFIC_EMAILS);
        request.setAllowAllCourse(false);
        request.setIsActive(true);
        return request;
    }

    public static Coupon coupon() {
        var coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStartDate(new Date());
        coupon.setEndDate(new Date(System.currentTimeMillis() + 172800000)); // 2 days
        coupon.setDurationInMonth(1);
        coupon.setRestricted(true);
        coupon.setIsActive(true);
        return coupon;
    }

    public static Coupon getPremiumCouponForAllCourses() {
        var coupon = coupon();
        coupon.setRedeemCode("PREM10");
        coupon.setCouponType(CouponType.PREMIUM);
        coupon.setAllowAllCourse(true);
        coupon.setDiscount(10.0);
        return coupon;
    }

    public static Coupon standardSubscriptionCoupon() {
        var coupon = coupon();
        coupon.setRedeemCode("STDSUBS10");
        coupon.setCouponType(CouponType.SUBSCRIPTION);
        coupon.setDiscount(10.0);
        coupon.setSubscription(SubscriptionTestData.standardSubscription());
        return coupon;
    }

    public static Coupon premiumSubscriptionCoupon() {
        var coupon = coupon();
        coupon.setRedeemCode("PREMSUB10");
        coupon.setCouponType(CouponType.SUBSCRIPTION);
        coupon.setDiscount(10.0);
        coupon.setSubscription(SubscriptionTestData.premiumSubscription());
        return coupon;
    }

    public static Coupon enterpriseSubscriptionCoupon() {
        var coupon = coupon();
        coupon.setRedeemCode("ENTERSUB10");
        coupon.setCouponType(CouponType.SUBSCRIPTION);
        coupon.setDiscount(10.0);
        coupon.setSubscription(SubscriptionTestData.enterpriseSubscription());
        return coupon;
    }
}
