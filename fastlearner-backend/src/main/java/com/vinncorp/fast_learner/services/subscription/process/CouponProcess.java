package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
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
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component("coupon")
public class CouponProcess implements SubscriptionStrategyService{

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

    @Override
    public Message<String> processSubscription(SubscriptionRequest requestDTO, SubscribedUser subscribedUser, Subscription nextSubscription, User user, Coupon coupon) throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Creating payment subscription for user: {}", user.getEmail());
        // Deep copy the subscribed user because we have to update the subscribed user object as per our following
        // implementation
        SubscribedUser previousSubscribedUser = new SubscribedUser(subscribedUser);

        // Check and create subscribedUserProfile
        SubscribedUserProfile subscribedUserProfile = subscribedUserProfileService.getDefaultBySubscribedUserId(subscribedUser.getId(), true);
        if (Objects.isNull(subscribedUserProfile)) {
            subscribedUserProfile = createCustomerProfile(subscribedUser, requestDTO);
        }

        var oldTransactionHistory = transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(user.getId(), GenericStatus.ACTIVE);
        if (oldTransactionHistory != null) {
            if (oldTransactionHistory.getSubscription().getPlanType().equals(PlanType.FREE)) {
                oldTransactionHistory.setStatus(GenericStatus.INACTIVE);
                oldTransactionHistory.setUpdatedDate(new Date());
                oldTransactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
                oldTransactionHistory = transactionHistoryService.save(oldTransactionHistory);
            }
        }//new transaction data set
        TransactionHistory transactionHistory = savingTransactionHistory(nextSubscription, user, oldTransactionHistory);
        if(Objects.nonNull(coupon)){
            transactionHistory.setCoupon(coupon);
        }

        TransactionHistory continueTransactionHistory = Optional.ofNullable(
                        transactionHistoryService.findByLatestUserAndSubscriptionStatus(user.getId(), SubscriptionStatus.PENDING))
                .orElseGet(() -> transactionHistoryService.findByLatestUserAndSubscriptionStatus(user.getId(), SubscriptionStatus.CONTINUE));

        if (continueTransactionHistory != null) {
            continueTransactionHistory.setUpdatedDate(new Date());
            continueTransactionHistory.setStatus(GenericStatus.INACTIVE);
            continueTransactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
            continueTransactionHistory = transactionHistoryService.save(continueTransactionHistory);
            if (continueTransactionHistory != null) {
                oldTransactionHistory.setSubscription(continueTransactionHistory.getSubscription());
                oldTransactionHistory.setTrialEndDate(continueTransactionHistory.getTrialEndDate());
                oldTransactionHistory.setSubscriptionNextCycle(continueTransactionHistory.getSubscriptionNextCycle());
                log.info("Subscription status is updated to discontinue in transaction history successfully");
            }
        }

        CreateSubscriptionResponse response = paymentAdditionalSubscriptionService.createCouponBased(
                nextSubscription, requestDTO, subscribedUserProfile.getCustomerPaymentId(), subscribedUserProfile.getCustomerPaymentProfileId(), user.getEmail(),
                user.getId(), null, transactionHistory, coupon);

        // Creating subscribed user for subscription based request
        updateSubscribedUser(subscribedUser, nextSubscription, response, user, coupon);

        if (oldTransactionHistory == null) {
            transactionHistoryService.save(transactionHistory);
        }

        log.info("Saved the transaction history.");

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

    private void updateSubscribedUser(SubscribedUser subscribedUser, Subscription nextSubscription, CreateSubscriptionResponse response, User user, Coupon coupon) throws InternalServerException {

        Date date = null;

        if (coupon != null) {
            LocalDateTime currentDate = LocalDateTime.now();
            currentDate = currentDate.plusMonths(coupon.getDurationInMonth());
            date = Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant());
        }

        subscribedUser.setSubscribedId(response.getSubscriptionId());
        subscribedUser.setPaymentSubscriptionId(response.getSubscriptionId());
        subscribedUser.setCustomerProfileId(response.getProfile().getCustomerProfileId());
        subscribedUser.setSubscription(nextSubscription);
        subscribedUser.setCoupon(coupon);
        subscribedUser.setCouponValidTill(date);
        subscribedUser.setStartDate(new Date());
        subscribedUser.setActive(true);
        subscribedUserService.save(subscribedUser);
    }

    // Fetching current active transaction history
    private static TransactionHistory saveTransactionHistory(Subscription nextSubscription, User user) {
        log.info("Fetching current active transaction history for user: {}", user.getId());
        return null;
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

    private SubscribedUserProfile createCustomerProfile(SubscribedUser subscribedUser, SubscriptionRequest request) throws InternalServerException {
        log.info("Creating customer payment profile...");
        var resp = paymentProfileService.createCustomerProfile(subscribedUser.getUser().getEmail(), request);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.warn("Pause interrupted: {}", e.getMessage());
        }

        SubscribedUserProfile subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setIsDefault(true);
        subscribedUserProfile.setSubscribedUser(subscribedUser);
        subscribedUserProfile.setCustomerPaymentId(resp.getCustomerId());
        subscribedUserProfile.setCustomerPaymentProfileId(resp.getPaymentId());

        log.info("Created new customer profile: {}, {}", resp.getCustomerId(), resp.getPaymentId());

        return subscribedUserProfileService.save(subscribedUserProfile);
    }

    @NotNull
    private static TransactionHistory savingTransactionHistory(Subscription nextSubscription, User user, TransactionHistory oldTransactionHistory) {
        TransactionHistory transactionHistory = TransactionHistory.builder().build();
        transactionHistory.setSubscriptionStatus(SubscriptionStatus.PENDING);
        transactionHistory.setStatus(GenericStatus.ACTIVE);
        transactionHistory.setSubscription(nextSubscription);
        transactionHistory.setCreationAt(new Date());
        transactionHistory.setSubscriptionAmount(nextSubscription.getPrice());
        transactionHistory.setUser(user);
        transactionHistory.setOldTransactionId(oldTransactionHistory == null ? null : oldTransactionHistory.getId());
        return transactionHistory;
    }
}
