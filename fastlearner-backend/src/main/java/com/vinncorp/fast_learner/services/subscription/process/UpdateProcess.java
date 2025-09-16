package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
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
import com.vinncorp.fast_learner.services.payment.additional_service.UpdatePaymentSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
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

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component("update")
public class UpdateProcess implements SubscriptionStrategyService {

    @Autowired
    private IPaymentProfileService paymentProfileService;
    @Autowired
    private ISubscribedUserProfileService subscribedUserProfileService;
    @Autowired
    private ITransactionHistoryService transactionHistoryService;
    @Autowired
    private UpdatePaymentSubscriptionService updatePaymentSubscriptionService;
    @Autowired
    private PaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Override
    public Message<String> processSubscription(SubscriptionRequest requestDTO, SubscribedUser subscribedUser, Subscription nextSubscription, User user, Coupon coupon)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Upgrading the subscription...");log.info("Updating the subscription...");

        // Deep copy the subscribed user because we have to update the subscribed user object as per our following
        // implementation
        SubscribedUser previousSubscribedUser = new SubscribedUser(subscribedUser);

        // Check and create subscribedUserProfile
        SubscribedUserProfile subscribedUserProfile = subscribedUserProfileService.getDefaultBySubscribedUserId(subscribedUser.getId(), true);
        if (Objects.isNull(subscribedUserProfile)) {
            subscribedUserProfile = createCustomerProfile(subscribedUser, requestDTO);
        }
        boolean isFreePlan=false;
        var oldTransactionHistory = transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(user.getId(), GenericStatus.ACTIVE);
        if (oldTransactionHistory.getSubscription().getPlanType().equals(PlanType.FREE)){
            isFreePlan=true;
            //if it is at free plan than first check if old plan is continue or not ,when old transaction history plan
//            is still continue than another subscription will run after continue subscription when its end.
//            TransactionHistory continueTransactionHistory=transactionHistoryService.findByLatestUserAndSubscriptionStatus(user.getId(),SubscriptionStatus.CONTINUE);

            TransactionHistory continueTransactionHistory = Optional.ofNullable(
                            transactionHistoryService.findByLatestUserAndSubscriptionStatus(user.getId(), SubscriptionStatus.PENDING))
                    .orElseGet(() -> transactionHistoryService.findByLatestUserAndSubscriptionStatus(user.getId(), SubscriptionStatus.CONTINUE));

            if (continueTransactionHistory!=null){
                continueTransactionHistory.setUpdatedDate(new Date());
                continueTransactionHistory.setStatus(GenericStatus.INACTIVE);
//                continueTransactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
                continueTransactionHistory= transactionHistoryService.save(continueTransactionHistory);
                oldTransactionHistory.setTrialEndDate(continueTransactionHistory.getSubscriptionNextCycle());
                oldTransactionHistory.setSubscriptionNextCycle(continueTransactionHistory.getSubscriptionNextCycle());
                if (continueTransactionHistory!=null){
                    log.info("Subscription status is updated to discontinue in transaction history successfully");
                }
            }
        }
        TransactionHistory transactionHistory = savingTransactionHistory(nextSubscription, user, oldTransactionHistory);

        Message<String> response = updatePaymentSubscriptionService.update(nextSubscription, subscribedUserProfile, user,
                transactionHistory, previousSubscribedUser, subscribedUser, oldTransactionHistory.getTrialEndDate(),
                oldTransactionHistory.getSubscriptionNextCycle());

        if (!isFreePlan) {
//            oldTransactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE);/
            // Cancel current subscription if the subscription is paid.
            cancelPreviousSubscription(previousSubscribedUser.getSubscribedId());
        }else {
            oldTransactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
            oldTransactionHistory.setTrialEndDate(null);
            oldTransactionHistory.setSubscriptionNextCycle(null);
        }
        // Update previous transaction history

        oldTransactionHistory.setStatus(GenericStatus.INACTIVE);
        oldTransactionHistory.setUpdatedDate(new Date());
        transactionHistoryService.save(oldTransactionHistory);

        return response;
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
        transactionHistory.setOldTransactionId(oldTransactionHistory.getId());
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

    private void cancelPreviousSubscription(String subscribedId) throws InternalServerException {
        log.info("Cancelling the previous subscription...");
        // Delete to Payment subscription from payment platform
        paymentAdditionalSubscriptionService.cancelPaymentSubscription(subscribedId);
    }
}
