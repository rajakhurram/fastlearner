package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionLog;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.response.subscription.CreateSubscriptionResponse;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.notification.IInstructorPerformanceInsightService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component("create")
public class CreateProces implements SubscriptionStrategyService {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISubscribedUserService subscribedUserService;
    @Autowired
    private IPaymentProfileService paymentProfileService;
    @Autowired
    private ISubscribedUserProfileService subscribedUserProfileService;
    @Autowired
    private ITransactionHistoryService transactionHistoryService;
    @Autowired
    private IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;
    @Autowired
    private SubscriptionLogRepository subscriptionLogRepo;
    @Autowired
    private IInstructorPerformanceInsightService instructorPerformanceInsightService;


    /**
     * Create subscription when user doesn't have any subscription i.e. if user signup firstly then he has a FREE
     * PLAN. So we have to check that if the user has no Plan or FREE plan subscribed then we create a subscription for
     * that user.<br><br>
     *
     * <b>PROCESS:</b><br>
     * Validate subscription request.<br>
     */
    @Transactional(rollbackFor = {InternalServerException.class, BadRequestException.class})
    @Override
    public Message<String> processSubscription(SubscriptionRequest requestDTO, SubscribedUser subscribedUser, Subscription nextSubscription, User user, Coupon coupon)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Creating payment subscription for user: {}", user.getEmail());

        // Check if the user subscribing for free subscription
        if (nextSubscription.getPlanType() == PlanType.FREE) {
            return createFreeSubscription(nextSubscription, user);
        }

        // Creating transaction history for a subscription plan
        TransactionHistory transactionHistory = saveTransactionHistory(nextSubscription, user);
        if(Objects.nonNull(coupon)){
            transactionHistory.setCoupon(coupon);
        }

        CreateSubscriptionResponse response = paymentAdditionalSubscriptionService.createFirstTime(
                nextSubscription, requestDTO, null, null, user.getEmail(),
                user.getId(), null, transactionHistory, coupon);

        // Creating subscribed user for subscription based request
        createPaidSubscription(nextSubscription, response, user, coupon);

        subscribedUserProfileService.saveSubscribedUserProfile(response.getProfile().getCustomerProfileId(), response.getProfile().getCustomerPaymentProfileId(),
                user.getEmail(), true);

        log.info("Saved the transaction history.");

        saveSubscriptionLog(requestDTO, user, response);

        // In user table is subscribed column should be true
        user.setSubscribed(true);
        userService.save(user);

        // Send subscription notification to user
        if(nextSubscription.getPlanType() != PlanType.FREE)
            instructorPerformanceInsightService.notifyToUserOnNewSubscription(nextSubscription.getDuration() == 1 ? "Monthly" : "Yearly", user.getId());
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Subscription: " + nextSubscription.getName() + " has been subscribed by " + user.getEmail())
                .setData("You have successfully subscribed this plan");
    }

    private void createPaidSubscription(Subscription nextSubscription, CreateSubscriptionResponse response, User user, Coupon coupon) throws InternalServerException {

        Date date = null;

        if (coupon != null) {
            LocalDateTime currentDate = LocalDateTime.now();
            currentDate = currentDate.plusMonths(coupon.getDurationInMonth());
            date = Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant());
        }

        SubscribedUser subscribedUser = SubscribedUser.builder()
                .user(user)
                .subscribedId(response.getSubscriptionId())
                .paymentSubscriptionId(response.getSubscriptionId())
                .customerProfileId(response.getProfile().getCustomerProfileId())
                .subscription(nextSubscription)
                .paymentStatus(PaymentStatus.PAID)
                .coupon(coupon)
                .couponValidTill(date)
                .startDate(new Date())
                .isActive(true)
                .build();
        subscribedUserService.save(subscribedUser);
    }

    private static TransactionHistory saveTransactionHistory(Subscription nextSubscription, User user) {
        log.info("Creating the transaction history for user: {}", user.getId());
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .subscription(nextSubscription)
                .user(user)
                .subscriptionStatus(SubscriptionStatus.PENDING)
                .subscriptionAmount(nextSubscription.getPrice())
                .status(GenericStatus.ACTIVE)
                .creationAt(new Date())
                .build();
        return transactionHistory;
    }

    private static TransactionHistory saveTransactionHistoryForFree(Subscription nextSubscription, User user) {
        log.info("Creating the transaction history for user: {}", user.getId());
        return TransactionHistory.builder()
                .subscription(nextSubscription)
                .user(user)
                .subscriptionStatus(SubscriptionStatus.SUCCESS)
                .subscriptionAmount(nextSubscription.getPrice())
                .status(GenericStatus.ACTIVE)
                .creationAt(new Date())
                .build();
    }

    private void saveSubscriptionLog(SubscriptionRequest requestDTO, User user, CreateSubscriptionResponse response) {
        log.info("Creating subscription logs...");
        subscriptionLogRepo.save(SubscriptionLog.builder()
                .customerProfileId(response.getProfile().getCustomerProfileId())
                .paymentProfileId(response.getProfile().getCustomerPaymentProfileId())
                .currentAuthSubscriptionId(response.getSubscriptionId())
                .prevAuthSubscriptionId(requestDTO.getPrevAuthSubscriptionId())
                .prevSubscriptionId(null)
                .currentSubscriptionId(requestDTO.getSubscriptionId())
                .userId(user.getId())
                .createdAt(new Date())
                .build());
        log.info("Subscription log created.");
    }

    public Message<String> createFreeSubscription(Subscription nextSubscription, User user) throws InternalServerException {
        SubscribedUser subscribedUser = SubscribedUser.builder()
                .user(user)
                .subscription(nextSubscription)
                .paymentStatus(PaymentStatus.PAID)
                .startDate(new Date())
                .isActive(true)
                .build();
        subscribedUserService.save(subscribedUser);

        user.setSubscribed(true);
        userService.save(user);

        var transactionHistory = saveTransactionHistoryForFree(nextSubscription, user);
        transactionHistory.setAuthSubscriptionId("FREE");
        transactionHistory.setResponseText(PlanType.FREE.name());

        transactionHistoryService.save(transactionHistory);

        log.info("Subscription: " + nextSubscription.getName() + " has been subscribed by " + user.getEmail());
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Subscription: " + nextSubscription.getName() + " has been subscribed by " + user.getEmail())
                .setData("You have successfully subscribed the free plan");
    }
}
