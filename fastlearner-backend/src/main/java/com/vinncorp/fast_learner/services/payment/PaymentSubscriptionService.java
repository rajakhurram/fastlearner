package com.vinncorp.fast_learner.services.payment;

import com.vinncorp.fast_learner.dtos.payment.BillingHistoryRequest;
import com.vinncorp.fast_learner.dtos.payment.BillingHistoryResponse;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.notification.IInstructorPerformanceInsightService;
import com.vinncorp.fast_learner.services.subscription.process.CreateProces;
import com.vinncorp.fast_learner.services.subscription.process.ICouponBasedSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.SubscriptionContextService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.*;
import com.vinncorp.fast_learner.util.exception.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentSubscriptionService implements IPaymentSubscriptionService {
    private final IUserService userService;
    private final ISubscriptionService subscriptionService;
    private final ISubscribedUserService subscribedUserService;
    private final ISubscribedUserProfileService subscribedUserProfileService;
    private final IPaymentProfileService paymentProfileService;
    private final IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final IInstructorPerformanceInsightService instructorPerformanceInsightService;
    private final ICouponBasedSubscriptionService couponBasedSubscriptionService;
    private final ITransactionHistoryService transactionHistoryService;
    private final SubscriptionContextService subscriptionContextService;
    private final CreateProces createProces;

    @Transactional(rollbackFor = {InternalServerException.class, BadRequestException.class})
    @Override
    public Message<String> create(SubscriptionRequest subscriptionRequest, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Creating payment subscription for user " + email);

        User user = userService.findByEmail(email.toLowerCase());
        SubscribedUser subscribedUser = ExceptionUtils.safelyFetch(() -> subscribedUserService.findByUser(email));

        // Cancel if user has coupon based subscription
        if(subscribedUser != null && subscribedUser.getCoupon() != null) {
            subscriptionContextService.setProcess(SubscriptionProcessType.CANCEL);
            subscriptionContextService.process(null, subscribedUser, null, user, null);
        }

        // If buying subscription by using coupon
        if (Objects.nonNull(subscriptionRequest.getCoupon()) && !subscriptionRequest.getCoupon().isBlank()) // Via coupon
            return couponBasedSubscriptionService.processCouponBasedSubscription(subscriptionRequest, subscribedUser, user);
        else if (Objects.nonNull(subscribedUser) && Objects.nonNull(subscribedUser.getCoupon())) // From coupon based to paid
            return couponBasedSubscriptionService.processCouponBasedSubscription(subscriptionRequest, subscribedUser, user);

        Subscription nextSubscription = subscriptionService.findBySubscriptionId(subscriptionRequest.getSubscriptionId()).getData();

        ExceptionUtils.safelyFetch(() -> validationOnChangingPlan(subscribedUser));
        subscriptionRequest.setPrevAuthSubscriptionId(subscribedUser==null?null:subscribedUser.getPaymentSubscriptionId());

        subscriptionContextService.setProcess(validateAndDefineWhichProcessToRun(subscribedUser, nextSubscription));

        // previousSubscription,
        return subscriptionContextService.process(subscriptionRequest, subscribedUser, nextSubscription, user, null);
    }

    public boolean validationOnChangingPlan(SubscribedUser subscribedUser) throws EntityNotFoundException, BadRequestException {
        log.info("Started validation for changing plan for user with ID: {}", subscribedUser.getUser().getId());

        log.info("Retrieving latest transaction history for subscription ID: {} with ACTIVE status",
                subscribedUser.getPaymentSubscriptionId());

        TransactionHistory transactionHistory = transactionHistoryService.
                findByLatestTransactionHistoryBySubsIdAndStatus(subscribedUser.getPaymentSubscriptionId(), GenericStatus.ACTIVE);

        if (transactionHistory == null) {
            log.info("No active transaction history found for subscription ID: {}", subscribedUser.getPaymentSubscriptionId());
            return true;
        } else {
            log.info("Active transaction history found for subscription ID: {}", subscribedUser.getPaymentSubscriptionId());
            log.info("Transaction created at: {}", transactionHistory.getCreationAt());

            // Check if the transaction creation date is today
            if (transactionHistory.getCreationAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(LocalDate.now())) {
                log.info("The transaction was created today, plan change is not allowed.");
                throw new BadRequestException("The transaction was created today, plan change is not allowed.");
            } else {
                // Plan change is allowed
                log.info("The transaction was created on a different day, plan change is allowed.");
                return true;
            }
        }
    }

    private SubscriptionProcessType validateAndDefineWhichProcessToRun(SubscribedUser subscribedUser, Subscription nextSubscription) throws BadRequestException, EntityNotFoundException {
        log.info("Validating the subscription request data and also fetching the type of process...");

        if (Objects.isNull(subscribedUser))
            return SubscriptionProcessType.CREATE;

        boolean isChangePlanAvailable = this.validationOnChangingPlan(subscribedUser);
        if (!isChangePlanAvailable) {
            log.error("ERROR: Plan change request denied. User with ID: {} already has an active PayPal subscription. Changing plans multiple times is not allowed.",
                    subscribedUser.getUser().getId());
            throw new BadRequestException("Plan change is not allowed at this time. You have already changed your plan recently.");
        }

        Subscription currentSub = subscribedUser.getSubscription();

        if (Objects.nonNull(subscribedUser.getPaypalSubscriptionId())) {
            log.error("ERROR: user already has paypal subscription");
            throw new BadRequestException("You already have a paypal subscription");
        } else if (Objects.equals(currentSub.getId(), nextSubscription.getId())) {
            log.error("ERROR: user already subscribed to this plan");
            throw new BadRequestException("You already subscribed to this plan");
        }

        PlanType currentPlanType = currentSub.getPlanType();
        PlanType nextPlanType = nextSubscription.getPlanType();

        // Check if the subscription plan is for UPGRADING
        if (currentPlanType.getValue() < nextPlanType.getValue())
            return SubscriptionProcessType.UPGRADE;

        // Check if the subscription plan is for DOWNGRADING
        if (currentPlanType.getValue() > nextPlanType.getValue())
            return SubscriptionProcessType.DOWNGRADE;

        // If the plan is same then its mean that only the Yearly to Monthly or Monthly to Yearly of a plan is changing
        // i.e. STANDARD (Monthly) to STANDARD (Yearly) or STANDARD (Yearly) to STANDARD (Monthly)
        if (currentPlanType == nextPlanType && currentSub.getDuration() != nextSubscription.getDuration())
            return SubscriptionProcessType.UPDATE;

        throw new BadRequestException("Subscription process cannot be define.");
    }

    @Override
    public Message<List<BillingHistoryResponse>> getBillingHistory(BillingHistoryRequest request, String email) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Fetching the billing history of user " + email);

        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);

        if (Objects.isNull(subscribedUser.getPaymentSubscriptionId())) {
            log.error("ERROR: Customer profile id not exist for user " + email);
            throw new BadRequestException("Customer profile id not exists");
        }

        if (subscribedUser.getPaymentSubscriptionId().equals("0000000") && subscribedUser.getCustomerProfileId().equals("000000000")) {
            return new Message<List<BillingHistoryResponse>>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString())
                    .setMessage("No billing history found for the premium user.")
                    .setData(null);
        }

        GetSubscriptionResponse transactions = paymentAdditionalSubscriptionService.getSubscriptionById(subscribedUser.getPaymentSubscriptionId());

        log.info("Subscription: billing history fetched");
        return new Message<List<BillingHistoryResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Billing history has been fetched successfully")
                .setData(BillingHistoryResponse.mapToBillingHistoryList(transactions, subscribedUser));
    }

    @Override
    public Message<String> updateSubscription(Long subscribedUserProfileId, String email) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Updating the subscription's payment profile.");
        SubscribedUserProfile subscribedUserProfile = subscribedUserProfileService.getSubscribedUserProfileById(subscribedUserProfileId);
        if (subscribedUserProfile.getIsDefault()) {
            throw new BadRequestException("The provided payment profile is already active for current subscription.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (Objects.isNull(subscribedUser.getCustomerProfileId()) || Objects.isNull(subscribedUser.getPaymentSubscriptionId()))
            throw new BadRequestException("Current subscription doesn't have a authorized subscription.");

        // Update the current subscription
        var customerProfileData = paymentAdditionalSubscriptionService.updatePaymentSubscription(email, subscribedUser.getPaymentSubscriptionId(),
                subscribedUser.getCustomerProfileId(), subscribedUserProfile.getCustomerPaymentProfileId());

        // Update the db now
        subscribedUserProfileService.markAllProfileSetAsNotDefaultById(subscribedUser.getId());
        subscribedUserProfile.setIsDefault(true);
        subscribedUserProfileService.save(subscribedUserProfile);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully updated the subscription on provided card.")
                .setData("Successfully updated the subscription on provided card.");
    }

    @Override
    public Message<String> freeSignUpSubscription(Long subscriptionId, String name) throws EntityNotFoundException, InternalServerException {
        User user = userService.findByEmail(name.toLowerCase());
        Subscription nextSubscription = subscriptionService.findBySubscriptionId(subscriptionId).getData();
        return createProces.createFreeSubscription(nextSubscription, user);
    }
}
