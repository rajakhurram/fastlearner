package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component("cancel")
public class CancelProcess implements SubscriptionStrategyService {
    @Autowired
    private ISubscriptionService subscriptionService;
    @Autowired
    private ISubscribedUserService subscribedUserService;
    @Autowired
    private IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;
    @Autowired
    private ITransactionHistoryService transactionHistoryService;

    /**
     * <b>Cancellation process will be processed by following steps:</b>
     * <ul>
     *     <li>Check if the subscription is already cancelled; if so, throw an error.</li>
     *     <li>Check if the subscription is a FREE subscription; if so, throw an error.</li>
     *     <li>Cancel the subscription from the Authorize.net platform.</li>
     *     <li>
     *         Fetch active transaction history, update the transaction history,
     *         and add a FREE subscription into the transaction history.
     *     </li>
     *     <li>
     *         Update subscribed user's endDate with trial period end date if the cancellation is processed in between
     *         trial period if not then the endDate should be subscription next cycle date.
     *     </li>
     * </ul>
     */
    @Override
    public Message<String> processSubscription(SubscriptionRequest requestDTO, SubscribedUser subscribedUser, Subscription nextSubscription, User user, Coupon coupon) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Cancelling the subscription of a user {}", user.getId());

        // Coupon based subscription cancellation
        if (Objects.nonNull(subscribedUser.getCoupon())) {
            return cancelCouponBasedSubscription(subscribedUser);
        }

        if (subscribedUser.getSubscription().getPlanType() == PlanType.FREE) {
            throw new BadRequestException("Free plan cannot be canceled.");
        }
        if (subscribedUser.getEndDate() != null) {
            TransactionHistory transactionHistory = transactionHistoryService.findByLatestUserAndSubscriptionStatus(
                    user.getId(), SubscriptionStatus.PENDING);
            if (transactionHistory != null) {

                transactionHistory.setResponseText("Subscription canceled before activation.");
                transactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
                transactionHistory.setStatus(GenericStatus.INACTIVE);
                paymentAdditionalSubscriptionService.cancelPaymentSubscription(transactionHistory.getAuthSubscriptionId());
                transactionHistoryService.save(transactionHistory);
            }
            throw new BadRequestException("This " + subscribedUser.getSubscription().getPlanType().name() + " plan already cancelled, but you can use it till " +
                    SimpleDateFormat.getDateInstance().format(subscribedUser.getEndDate()));
        }


        if (subscribedUser.getPaymentSubscriptionId() == "0") {
            throw new BadRequestException("Free plan cannot be canceled.");
        }
        Boolean isErrorSubs = false;
        //if error not in old subscription so that it can run
        if (!subscribedUser.getSubscribedId().equalsIgnoreCase("0")) {
            // Delete to Payment subscription from Payment platform
            paymentAdditionalSubscriptionService.cancelPaymentSubscription(subscribedUser.getPaymentSubscriptionId());
        } else {
            isErrorSubs = true;
        }
        // Fetch the latest transaction history by ACTIVE status and payment subscription ID
        TransactionHistory transactionHistory = transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(
                subscribedUser.getSubscribedId(), GenericStatus.ACTIVE);
        if (!isErrorSubs) {
            if (transactionHistory == null) {
                transactionHistory = transactionHistoryService.findByLatestUserAndSubscriptionStatus(
                        user.getId(), SubscriptionStatus.PENDING);
                transactionHistory.setResponseText("Subscription canceled before activation.");
                transactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
                if (transactionHistory != null) {
                    paymentAdditionalSubscriptionService.cancelPaymentSubscription(transactionHistory.getAuthSubscriptionId());
                }
            } else {
//                transactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
            }
        } else {
            transactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
            transactionHistory.setStatus(GenericStatus.INACTIVE);
        }
        updateTransactionHistory(transactionHistory);

        Subscription subscription = subscriptionService.findBySubscriptionId(1L).getData();
        saveTransactionHistoryForFreePlan(subscription, transactionHistory, user);

        LocalDate nextBillingCycleDate = transactionHistory.getSubscriptionNextCycle().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (subscribedUser.getSubscription().getPlanType() == PlanType.STANDARD) {
            subscribedUser.setEndDate(transactionHistory.getTrialEndDate().toInstant().isBefore(Instant.now()) ?
                    transactionHistory.getTrialEndDate() : transactionHistory.getSubscriptionNextCycle());
        } else {
            subscribedUser.setEndDate(transactionHistory.getSubscriptionNextCycle());
        }
        subscribedUserService.save(subscribedUser);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Subscription will be cancelled at " + nextBillingCycleDate + ", so till then you can use the system with current subscription")
                .setData("Subscription will be cancelled at " + nextBillingCycleDate + ", so till then you can use the system with current subscription");
    }

    @Transactional
    @Modifying
    private Message<String> cancelCouponBasedSubscription(SubscribedUser subscribedUser) throws EntityNotFoundException, InternalServerException {
        log.info("Cancelling coupon based subscription...");

        // Delete to Payment subscription from Payment platform
        paymentAdditionalSubscriptionService.cancelPaymentSubscription(subscribedUser.getPaymentSubscriptionId());

        // Fetch latest transaction
        var trans = transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(subscribedUser.getUser().getId(), GenericStatus.ACTIVE);
        trans.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
        trans.setStatus(GenericStatus.INACTIVE);
        trans.setUpdatedDate(new Date());
        transactionHistoryService.save(trans);

        // Create new transaction history
        Subscription freeSub = subscriptionService.findBySubscriptionId(1L).getData();

        var newTrans = TransactionHistory.builder()
                .authSubscriptionId("FREE")
                .status(GenericStatus.ACTIVE)
                .creationAt(new Date())
                .oldTransactionId(trans.getId())
                .responseCode("200")
                .responseText("FREE")
                .subscriptionAmount(0.0)
                .subscription(freeSub)
                .user(subscribedUser.getUser())
                .subscriptionStatus(SubscriptionStatus.SUCCESS)
                .paymentStatus(PaymentStatus.PAID)
                .trialEndDate(new Date())
                .settledDate(LocalDateTime.now())
                .build();
        transactionHistoryService.save(newTrans);

        // Update the subscription to FREE plan
        subscribedUser.setSubscription(freeSub);
        subscribedUser.setCoupon(null);
        subscribedUser.setCouponValidTill(null);
        subscribedUser.setSubscribedId(null);
        subscribedUser.setPaymentSubscriptionId(null);
        subscribedUser.setStartDate(new Date());

        subscribedUserService.save(subscribedUser);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Coupon based subscription cancelled successfully.")
                .setData("Coupon based subscription cancelled successfully.");
    }

    private void updateTransactionHistory(TransactionHistory transactionHistory) {

//        transactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
        transactionHistory.setStatus(GenericStatus.INACTIVE);
        transactionHistoryService.save(transactionHistory);
    }

    private void saveTransactionHistoryForFreePlan(Subscription subscription, TransactionHistory oldTransactionHistory, User user) {
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .status(GenericStatus.ACTIVE)
                .subscriptionStatus(SubscriptionStatus.SUCCESS)
                .responseText("Subscription cancellation process completed.")
                .authSubscriptionId("FREE") // As authSubscriptionId cannot be null so it cannot be left null
                .creationAt(new Date())
                .subscriptionAmount(subscription.getPrice())
                .subscription(subscription)
                .oldTransactionId(oldTransactionHistory.getId())
                .user(user)
                .build();
        transactionHistoryService.save(transactionHistory);
    }

    /**
     * Fetch all subscriptions which has endDate and update the respective data in the database.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateDatabaseWithRespectToEndDateOfSubscription() throws EntityNotFoundException {
        log.info("Subscription info updator scheduler is running...");

        Subscription freeSubscription = subscriptionService.findBySubscriptionId(1L).getData();

        // Fetch all subscribedUser with endDate null
        List<SubscribedUser> subscribedUsers = subscribedUserService.findAllSubscribedUserWhichAreCancelled();
        if (CollectionUtils.isEmpty(subscribedUsers))
            return;

        // update the respective data
        for (SubscribedUser subscribedUser : subscribedUsers) {
            // Fetch that transaction history which is cancelled
            TransactionHistory transactionHistory = transactionHistoryService
                    .findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus(
                            subscribedUser.getSubscribedId(), GenericStatus.INACTIVE, SubscriptionStatus.CONTINUE);

            transactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
            transactionHistoryService.save(transactionHistory);

            // Fetch the free subscription's transaction history
            TransactionHistory freeTransactionHistory = transactionHistoryService
                    .findByLatestTransactionHistoryByUserIdAndStatus(
                            subscribedUser.getUser().getId(), GenericStatus.ACTIVE);
            freeTransactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
            transactionHistoryService.save(freeTransactionHistory);

            subscribedUser.setSubscription(freeSubscription);
            subscribedUser.setSubscribedId(null);
            subscribedUser.setStartDate(new Date());
            subscribedUser.setPaymentSubscriptionId(null);
            subscribedUser.setEndDate(null);
            try {
                subscribedUserService.save(subscribedUser);
            } catch (InternalServerException e) {
                log.error("Error on subscription updater scheduler: " + e.getLocalizedMessage());
            }
        }
    }
}
