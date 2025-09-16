package com.vinncorp.fast_learner.integration.coupon;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.coupon.ICouponService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.ICouponBasedSubscriptionService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CouponBasedSubscriptionServiceIntegrationTest {

    @Autowired
    private ICouponBasedSubscriptionService couponBasedSubscriptionService;

    @Autowired
    private ISubscribedUserService subscribedUserService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private ICouponService couponService;

    private static final String SUBSCRIPTION_COUPON = "SUBSCRIPTION";
    private static final String SUBS_1_COUPON = "SUBS-1";
    private static final String PREMIUM_COUPON = "PREMIUM";
    private static final String BOTH = "SUBPREM";

    private SubscriptionRequest request;
    private Subscription subscription;
    private User user;
    private SubscribedUser subscribedUser;
    private Coupon coupon;

    @BeforeEach
    public void init()  {
        subscription = ExceptionUtils.safelyFetch(() -> subscriptionService.findBySubscriptionId(1L).getData());
        subscribedUser = ExceptionUtils.safelyFetch(() -> subscribedUserService.findByUser("testingbalti5@yopmail.com"));
        user =  ExceptionUtils.safelyFetch(() -> userService.findByEmail("testingbalti5@yopmail.com"));

        request = SubscriptionRequest.builder()
                .immediatelyApply(true)
                .build();

    }

    @Test
    void testProcessCouponBasedSubscription_FromFreePlan() throws Exception {
        // Arrange
        coupon = couponService.findByCouponCode(SUBSCRIPTION_COUPON);

        request.setCoupon(SUBSCRIPTION_COUPON);
        request.setImmediatelyApply(true);
        request.setPaymentDetail(PaymentProfileDetailRequest.builder()
                        .cardNumber("4111111111111111")
                        .cvv("900")
                        .firstName("Qasim")
                        .lastName("Ali")
                        .expiryMonth("04")
                        .expiryYear("28")
                .build());

        // Act
        Message<String> response = couponBasedSubscriptionService.processCouponBasedSubscription(request, null, user);

        // Assert
        assertNotEquals(response, null);
    }

    @Test
    void testProcessCouponBasedSubscription_FromOtherPlanWithoutCoupon() throws Exception {
        // Arrange
        coupon = couponService.findByCouponCode(SUBSCRIPTION_COUPON);

        request.setCoupon(SUBS_1_COUPON);
        request.setImmediatelyApply(true);
        request.setPaymentDetail(PaymentProfileDetailRequest.builder()
                .cardNumber("4111111111111111")
                .cvv("900")
                .firstName("Qasim")
                .lastName("Ali")
                .expiryMonth("04")
                .expiryYear("28")
                .build());

        // Act
        Message<String> response = couponBasedSubscriptionService.processCouponBasedSubscription(request, subscribedUser, subscribedUser.getUser());

        // Assert
        assertNotEquals(response, null);
    }

    @Test
    void testProcessCouponBasedSubscription_FromCouponBasedToPaid() throws Exception {
        // Arrange


        // Act
        Message<String> response = couponBasedSubscriptionService.processCouponBasedSubscription(request, subscribedUser, subscribedUser.getUser());

        // Assert
        assertNotEquals(response, null);
    }

    @Test
    void testProcessCouponBasedSubscription_FromCouponBasedToCouponBased() throws Exception {
        // Arrange

        // Act
        Message<String> response = couponBasedSubscriptionService.processCouponBasedSubscription(request, subscribedUser, subscribedUser.getUser());

        // Assert
        assertNotEquals(response, null);
    }

    @Test
    void testProcessCouponBasedSubscription_ThrowsBadRequestException_WhenInvalidInput() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            couponBasedSubscriptionService.processCouponBasedSubscription(request, null, subscribedUser.getUser());
        });
    }
}
