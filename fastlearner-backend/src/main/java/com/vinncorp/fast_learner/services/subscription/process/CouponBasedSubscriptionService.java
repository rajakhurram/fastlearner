package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.coupon.ICouponService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.date.DateUtils;
import com.vinncorp.fast_learner.util.enums.CouponType;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionProcessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class CouponBasedSubscriptionService implements ICouponBasedSubscriptionService {

    private final ISubscribedUserService subscribedUserService;
    private final ICouponService couponService;
    private final IPaymentAdditionalSubscriptionService paymentService;
    private final ITransactionHistoryService transactionHistoryService;
    private final ISubscriptionService subscriptionService;
    private final SubscriptionContextService subscriptionContextService;

    public CouponBasedSubscriptionService(@Lazy ISubscribedUserService subscribedUserService, ICouponService couponService,
                                          @Lazy IPaymentAdditionalSubscriptionService paymentService,
                                          @Lazy ITransactionHistoryService transactionHistoryService,
                                          @Lazy ISubscriptionService subscriptionService,
                                          @Lazy SubscriptionContextService subscriptionContextService) {
        this.subscribedUserService = subscribedUserService;
        this.couponService = couponService;
        this.paymentService = paymentService;
        this.transactionHistoryService = transactionHistoryService;
        this.subscriptionService = subscriptionService;
        this.subscriptionContextService = subscriptionContextService;
    }

    @Override
    public Message<String> processCouponBasedSubscription(SubscriptionRequest subscriptionRequest, SubscribedUser subscribedUser, User user)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        log.info("Processing coupon based subscription...");

        // If user is on paid subscription then cancelled the subscription firstly.
        if(subscribedUser != null && subscribedUser.getEndDate() == null && subscribedUser.getSubscription().getPlanType() != PlanType.FREE) {
            paymentService.cancelPaymentSubscription(subscribedUser.getPaymentSubscriptionId());
        }
        // If user has cancelled the paid subscription (user is using the remaining days of the
        // cancelled subscription).
        if (subscribedUser != null && subscribedUser.getEndDate() != null) {
            // Remove subscribed user data for ongoing subscription after cancelled.
            removeOngoingSubscriptionStatus(subscribedUser);
        }

        return processCouponBasedSubscriptionFromFreePlan(subscriptionRequest, subscribedUser, user);
    }

    private void removeOngoingSubscriptionStatus(SubscribedUser subscribedUser) throws EntityNotFoundException, InternalServerException {
       log.info("Removing the cancelled subscription data of subscribed user id {}", subscribedUser.getId());
       Subscription subscription = subscriptionService.findBySubscriptionId(1L).getData();

       subscribedUser.setEndDate(null);
       subscribedUser.setSubscription(subscription);
       subscribedUser.setStartDate(new Date());
       subscribedUser.setSubscribedId(null);
       subscribedUser.setPaymentSubscriptionId(null);

       subscribedUserService.save(subscribedUser);

    }

    // COUPON BASED SUBSCRIPTION FROM FREE PLAN
    private Message<String> processCouponBasedSubscriptionFromFreePlan(SubscriptionRequest request, SubscribedUser subscribedUser, User user)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        log.info("Processing coupon based subscription from free plan...");

        // Validate coupon and apply the changes to subscribed user table
        Coupon coupon = validateCouponAndApplyChanges(request.getCoupon(), subscribedUser, user);

        if (subscribedUser != null) {
            // Create the subscription
            subscriptionContextService.setProcess(SubscriptionProcessType.COUPON);

            subscriptionContextService.process(request, subscribedUser, coupon.getSubscription(), user, coupon);
        }else {
            // Create the subscription
            subscriptionContextService.setProcess(SubscriptionProcessType.CREATE);

            subscriptionContextService.process(request, null, coupon.getSubscription(), user, coupon);
        }

        log.info("Coupon based subscription is subscribed from FREE plan.");
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Coupon based subscription is subscribed from FREE plan.")
                .setData("Coupon based subscription is subscribed from FREE plan.");
    }

    private Coupon validateCouponAndApplyChanges(String coupon, SubscribedUser subscribedUser, User user) throws EntityNotFoundException, InternalServerException {
        log.info("Validating coupon and applying subscription plan in progress...");

        var validCoupon = couponService.validateCoupon(coupon, user, CouponType.SUBSCRIPTION, null);

        if (Objects.nonNull(subscribedUser)) {
            if (Objects.nonNull(subscribedUser.getCoupon()) && Objects.equals(validCoupon.getId(), subscribedUser.getCoupon().getId()))
                throw new EntityNotFoundException("This coupon is already in use.");
        }

        log.info("Validated coupon and applied the coupon's subscription plan.");
        return validCoupon;
    }

    private Message<String> validateCouponWithCurrentSubscriptionPlan(String coupon, SubscribedUser subscribedUser) throws EntityNotFoundException {
        log.info("Processing coupon applying validation...");

        Coupon savedCoupon = couponService.validateCoupon(coupon, subscribedUser.getUser(), CouponType.SUBSCRIPTION, null);

        if (subscribedUser.getSubscription().getPlanType() != PlanType.FREE) {
            if (subscribedUser.getSubscription().getId().equals(savedCoupon.getSubscription().getId())) {
                log.info("Current plan and coupon's plan is same.");
                return new Message<String>()
                        .setStatus(HttpStatus.BAD_REQUEST.value())
                        .setCode(HttpStatus.BAD_REQUEST.name())
                        .setMessage("The coupon provided plan is " + savedCoupon.getSubscription().getName() + " which is already subscribed.")
                        .setData("The coupon provided plan is " + savedCoupon.getSubscription().getName() + " which is already subscribed.");
            } else {
                log.info("Current subscription and coupon's subscription is not same.");
                return new Message<String>()
                        .setStatus(HttpStatus.BAD_REQUEST.value())
                        .setCode(HttpStatus.BAD_REQUEST.name())
                        .setMessage("The coupon provided plan is " + savedCoupon.getSubscription().getName() + " so the current plan will be cancelled immediately.")
                        .setData("The coupon provided plan is " + savedCoupon.getSubscription().getName() + " so the current plan will be cancelled immediately.");
            }
        }

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Your coupon's plan is " + savedCoupon.getSubscription().getName() + " which will be valid till " +
                        DateUtils.showDate(savedCoupon.getEndDate()))
                .setData("Your coupon's plan is " + savedCoupon.getSubscription().getName() + " which will be valid till " +
                        DateUtils.showDate(savedCoupon.getEndDate()));
    }

    /**
     * Scheduler will be used to invalidate those subscription which are coupon based by checking the coupon's validity
     * end date. This will be run daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public void updateCouponExpiryScheduler() {
        System.out.println("----------------------------------------------------------------");
        System.out.println("|    UPDATE COUPON BASED SUBSCRIPTION ON COUPON EXPIRATION     |");
        System.out.println("----------------------------------------------------------------");

        try {
            List<SubscribedUser> subscribedUserList = subscribedUserService.fetchAllCouponBasedSubscriptions();

            Date currentDate = new Date();

            for (SubscribedUser subscribedUser : subscribedUserList) {
                // When coupon valid till date present then update the subscribed user
                if (subscribedUser.getCouponValidTill() != null && subscribedUser.getCouponValidTill().before(currentDate)) {
                    subscribedUser.setCoupon(null);
                    subscribedUser.setCouponValidTill(null);
                    subscribedUserService.save(subscribedUser);
                    log.info("Subscription updated for user: {} due to expired coupon.", subscribedUser.getUser().getId());
                }
            }
        } catch (EntityNotFoundException e) {
            log.error("No coupon-based subscription found.");
        } catch (Exception e) {
            log.error("Error occurred while updating coupon-based subscriptions: {} ", e);
        }
    }
}
