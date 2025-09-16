package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.subscription.RemainingBalance;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.additional_service.PaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.additional_service.UpgradePaymentSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.subscription.SubscriptionService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component("upgrade")
public class UpgradeProcess implements SubscriptionStrategyService {

    @Autowired
    private IPaymentProfileService paymentProfileService;
    @Autowired
    private ISubscribedUserProfileService subscribedUserProfileService;
    @Autowired
    private ITransactionHistoryService transactionHistoryService;
    @Autowired
    private UpgradePaymentSubscriptionService upgradePaymentSubscriptionService;
    @Autowired
    private PaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public Message<String> processSubscription(SubscriptionRequest requestDTO, SubscribedUser subscribedUser, Subscription nextSubscription, User user, Coupon coupon)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        log.info("Upgrading the subscription...");

        // Check if Upgrading from downgraded subscriptions

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


        Message<String> response = null;

        // Check if the subscribedUser current subscription is free
        if (Objects.isNull(subscribedUser.getEndDate()) && subscribedUser.getSubscription().getPlanType() == PlanType.FREE) { // process upgrade from FREE plan
            transactionHistory.setSubscription(nextSubscription);
            // Create subscription
            response = upgradePaymentSubscriptionService.upgradeFromFreePlan(nextSubscription, subscribedUserProfile,
                    user, transactionHistory, previousSubscribedUser, subscribedUser);

        } else { // process upgrade from PAID plans
            // Calculate the remaining balance for current subscription
            RemainingBalance remainingBalance = calculateRemainingBalance(oldTransactionHistory, nextSubscription);
            transactionHistory.setDeductedAmount(remainingBalance.getRemainingBalance());
            // Create subscription from PAID plans
            response = upgradePaymentSubscriptionService.upgradeFromPaidPlan(nextSubscription, subscribedUserProfile,
                    user, transactionHistory, previousSubscribedUser, subscribedUser, remainingBalance);

            // Cancel current subscription if the subscription is paid.
            cancelPreviousSubscription(previousSubscribedUser.getSubscribedId());
        }

        if (oldTransactionHistory == null) {
            transactionHistoryService.save(transactionHistory);
        }
        return response;
    }

    private void cancelPreviousSubscription(String subscribedId) throws InternalServerException {
        log.info("Cancelling the previous subscription...");
        // Delete to Payment subscription from payment platform
        paymentAdditionalSubscriptionService.cancelPaymentSubscription(subscribedId);
    }

    public RemainingBalance calculateRemainingBalance(TransactionHistory oldTransactionHistory, Subscription nextSubscription) {
        log.info("Calculating remaining balance...");

        LocalDate currentDate = LocalDate.now();
        LocalDate trialEndDate = toLocalDate(oldTransactionHistory.getTrialEndDate());
        LocalDate subscriptionNextCycleDate = toLocalDate(oldTransactionHistory.getSubscriptionNextCycle());

        double remainingBalance = calculateRemainingBalance(oldTransactionHistory, currentDate, trialEndDate, subscriptionNextCycleDate);

        if (remainingBalance <= 0) {
            log.warn("User doesn't have any balance available");
            return null;
        }

        int trialDays = calculateTrialDays(remainingBalance, nextSubscription);

        if (remainingBalance > nextSubscription.getPrice() || trialDays > 7) {
            return new RemainingBalance(remainingBalance, trialDays);
        }

        RemainingBalance.TRIAL_PERIOD trialPeriod = determineTrialPeriod(nextSubscription.getDuration());
        return new RemainingBalance(nextSubscription.getPrice() - remainingBalance, trialPeriod);
    }

    private double calculateRemainingBalance(TransactionHistory oldTransactionHistory, LocalDate currentDate,
                                             LocalDate trialEndDate, LocalDate subscriptionNextCycleDate) {
        if (isBeforeOrEqual(currentDate, trialEndDate)) {
            log.info("Upgrading subscription before trial ends. Full amount is in balance.");
            return oldTransactionHistory.getSubscription().getPrice();
        }

        if (currentDate.isBefore(subscriptionNextCycleDate)) {
            log.info("Calculating remaining balance during paid subscription cycle...");
            return calculateProratedBalance(oldTransactionHistory, currentDate, trialEndDate, subscriptionNextCycleDate);
        }

        log.info("No remaining balance present.");
        return 0.0;
    }

    private double calculateProratedBalance(TransactionHistory oldTransactionHistory, LocalDate currentDate,
                                            LocalDate trialEndDate, LocalDate subscriptionNextCycleDate) {
        long daysConsumed = ChronoUnit.DAYS.between(trialEndDate, currentDate);
        long totalDays = ChronoUnit.DAYS.between(trialEndDate, subscriptionNextCycleDate);
        double pricePerDay = oldTransactionHistory.getSubscription().getPrice() / totalDays;

        double remainingBalance = oldTransactionHistory.getSubscription().getPrice() - (pricePerDay * daysConsumed);

        log.info(String.format("Days consumed: %d, Total days: %d, Price per day: %.2f, Remaining balance: %.2f",
                daysConsumed, totalDays, pricePerDay, remainingBalance));

        return remainingBalance;
    }

    private int calculateTrialDays(double remainingBalance, Subscription nextSubscription) {
        LocalDate currentDate = LocalDate.now();
        LocalDate nextCycle = currentDate.plusMonths(nextSubscription.getDuration());

        long totalDays = ChronoUnit.DAYS.between(currentDate, nextCycle);
        double pricePerDay = nextSubscription.getPrice() / totalDays;

        return (int) Math.ceil(remainingBalance / pricePerDay);
    }

    private RemainingBalance.TRIAL_PERIOD determineTrialPeriod(int duration) {
        return duration == 1 ? RemainingBalance.TRIAL_PERIOD.MONTHLY : RemainingBalance.TRIAL_PERIOD.YEARLY;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean isBeforeOrEqual(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2) || date1.equals(date2);
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
}
