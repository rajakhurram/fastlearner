package com.vinncorp.fast_learner.services.coupon;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CouponType;
import org.springframework.data.domain.Page;

public interface ICouponService {
    Coupon findByCouponCode(String code) throws EntityNotFoundException;

    Message<Page<Coupon>> fetchAll(Integer page, Integer size, Boolean isActive) throws EntityNotFoundException;

    Message<CouponRequest> fetchById(Long id) throws EntityNotFoundException;

    Message<String> toggleStatus(Long id) throws EntityNotFoundException;

    Message<String> create(CouponRequest request) throws InternalServerException, EntityAlreadyExistException;

    Message<String> update(CouponRequest request) throws EntityNotFoundException, InternalServerException, EntityAlreadyExistException;

    Message<String> delete(Long id);

    Message<Coupon> validateDiscount(String coupon, String couponType, Long courseId,Long subscriptionId ,String email) throws EntityNotFoundException, BadRequestException;

    Coupon validateCoupon(String coupon, User user, CouponType couponType, Long id) throws EntityNotFoundException;
}
