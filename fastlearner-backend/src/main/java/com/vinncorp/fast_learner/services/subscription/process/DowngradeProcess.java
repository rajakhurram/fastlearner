package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.additional_service.PaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.additional_service.DowngradePaymentSubscriptionService;
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
@Component("downgrade")
public class DowngradeProcess implements SubscriptionStrategyService {

    @Autowired
    private IPaymentProfileService paymentProfileService;
    @Autowired
    private ISubscribedUserProfileService subscribedUserProfileService;
    @Autowired
    private ITransactionHistoryService transactionHistoryService;
    @Autowired
    private DowngradePaymentSubscriptionService downgradePaymentSubscriptionService;
    @Autowired
    private PaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Override
    public Message<String> processSubscription(SubscriptionRequest requestDTO, SubscribedUser subscribedUser, Subscription nextSubscription, User user, Coupon coupon) throws InternalServerException, EntityNotFoundException {
        log.info("Downgrading current subscription...");

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
                transactionHistoryService.save(oldTransactionHistory);
                oldTransactionHistory = Optional.ofNullable(
                                transactionHistoryService.findByLatestUserAndSubscriptionStatus(user.getId(), SubscriptionStatus.PENDING))
                        .orElseGet(() -> transactionHistoryService.findByLatestUserAndSubscriptionStatus(user.getId(), SubscriptionStatus.CONTINUE));
                if (oldTransactionHistory == null) {
                    log.info("Only found free transaction history.");
                }
            }
        } else {
            throw new EntityNotFoundException("You are not allowed to change the subscription for now.Please contact support");
        }
        TransactionHistory transactionHistory = savingTransactionHistory(nextSubscription, user, oldTransactionHistory);

        // If oldTransactionHistory has same subscription id then its mean the upgrading process is first time so the transactionHistory
        // should be new else the transactionHistory should be update
        if (!oldTransactionHistory.getSubscription().getId().equals(subscribedUser.getSubscription().getId())) {
            log.info("You have already downgraded from a subscription.\n " +
                    "Now the downgraded subscription will be the current subscription on which you are downgraded.");
            transactionHistory = transactionHistoryService.findById(oldTransactionHistory.getOldTransactionId());
            transactionHistory.setSubscription(nextSubscription);
            transactionHistory.setCreationAt(new Date());
            transactionHistory.setSubscriptionAmount(nextSubscription.getPrice());

            // Cancel the subscription of above transactionHistory's
            cancelPreviousSubscription(transactionHistory.getAuthSubscriptionId());
        } else {
            // Cancel current subscription if the subscription is paid.
            cancelPreviousSubscription(subscribedUser.getSubscribedId());
        }


        Message<String> response = downgradePaymentSubscriptionService.downgrade(nextSubscription, subscribedUserProfile, user, transactionHistory,
                previousSubscribedUser, subscribedUser, oldTransactionHistory.getSubscriptionNextCycle());


        // Update previous transaction history
        oldTransactionHistory.setStatus(GenericStatus.INACTIVE);
        oldTransactionHistory.setUpdatedDate(new Date());
        transactionHistoryService.save(oldTransactionHistory);

        return response;
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
    private TransactionHistory savingTransactionHistory(Subscription nextSubscription, User user, TransactionHistory oldTransactionHistory) {
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

    private void cancelPreviousSubscription(String subscribedId) throws InternalServerException {
        log.info("Cancelling the previous subscription...");
        // Delete to Payment subscription from payment platform
        paymentAdditionalSubscriptionService.cancelPaymentSubscription(subscribedId);
    }
}
