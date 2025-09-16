package com.vinncorp.fast_learner.services.coupon;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CouponType;

import java.util.List;

public interface ICouponService {
    Coupon findByCouponCode(String code) throws EntityNotFoundException;

    Message<List<Coupon>> fetchAll() throws EntityNotFoundException;

    Message<String> create(CouponRequest request) throws InternalServerException, EntityNotFoundException;

    Message<String> update(CouponRequest request) throws EntityNotFoundException, InternalServerException;

    Message<String> delete(Long id);

    Message<Coupon> validateDiscount(String coupon, String couponType, Long courseId,Long subscriptionId ,String email) throws EntityNotFoundException, BadRequestException;

    Coupon validateCoupon(String coupon, User user, CouponType couponType, Long id) throws EntityNotFoundException;
}
