package com.vinncorp.fast_learner.services.subscription;

import com.vinncorp.fast_learner.dtos.payout.PaidUser;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.repositories.subscription.SubscribedUserRepository;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.response.subscription.CurrentSubscriptionResponse;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.SubscriptionContextService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.subscription_permission.ISubscriptionPermissionService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.date.DateUtils;
import com.vinncorp.fast_learner.util.enums.*;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SubscribedUserService implements ISubscribedUserService {

    private final SubscribedUserRepository repo;
    private final ISubscriptionService subscriptionService;
    private final ISubscribedUserProfileService subscribedUserProfileService;

    private final IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;
    private final IUserService userService;

    private final ITransactionHistoryService transactionHistoryService;
    private final SubscriptionContextService subscriptionContextService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final ISubscriptionPermissionService subscriptionPermissionService;

    public SubscribedUserService(SubscribedUserRepository repo, @Lazy ISubscriptionService subscriptionService,
                                 @Lazy ISubscribedUserProfileService subscribedUserProfileService, IPaymentAdditionalSubscriptionService
                                         paymentAdditionalSubscriptionService, @Lazy IUserService userService,
                                 ITransactionHistoryService transactionHistoryService,
                                 @Lazy SubscriptionContextService subscriptionContextService, @Lazy SubscriptionLogRepository subscriptionLogRepo, ISubscriptionPermissionService subscriptionPermissionService) {
        this.repo = repo;
        this.subscriptionService = subscriptionService;
        this.subscribedUserProfileService = subscribedUserProfileService;
        this.paymentAdditionalSubscriptionService = paymentAdditionalSubscriptionService;
        this.userService = userService;
        this.transactionHistoryService = transactionHistoryService;
        this.subscriptionContextService = subscriptionContextService;
        this.subscriptionLogRepo = subscriptionLogRepo;
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public SubscribedUser save(SubscribedUser subscribedUser) throws InternalServerException {
        log.info("Saving subscription with user.");
        try {
            return repo.save(subscribedUser);
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("SubscribedUser " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<String> completeSubscription(String paypalSubscriptionId, String email)
            throws EntityNotFoundException, InternalServerException {
        log.info("Complete subscription for user: " + email);
        SubscribedUser subscribedUser = repo.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User doesn't subscribed any plan."));

        subscribedUser.setPaypalSubscriptionId(paypalSubscriptionId);
        subscribedUser.setPaymentStatus(PaymentStatus.PAID);
        subscribedUser.setActive(true);
        save(subscribedUser);

        // update user
        updateUser(email);

        log.info("Subscription is completed.");
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Subscription is completed.")
                .setData("Subscription is completed.");
    }

    private void updateUser(String email) throws EntityNotFoundException, InternalServerException {
        log.info("Update user " + email + " when subscribed");
        // update user
        User user = userService.findByEmail(email);
        user.setSubscribed(true);
        userService.save(user);
    }

    @Override
    public Message<String> completePaymentSubscription(String paymentSubscriptionId, String email) throws EntityNotFoundException, InternalServerException {
        log.info("Complete Payment subscription for user " + email);

        SubscribedUser subscribedUser = repo.findByUserEmail(email).orElseThrow(() -> new EntityNotFoundException("User doesn't subscribed any plan"));

        subscribedUser.setSubscribedId(paymentSubscriptionId);
        subscribedUser.setPaymentSubscriptionId(paymentSubscriptionId);
        subscribedUser.setPaymentStatus(PaymentStatus.PAID);

        // update user
        updateUser(email);

        log.info("Payment Subscription is completed.");
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Payment Subscription is completed.")
                .setData("Payment Subscription is completed.");
    }


    @Transactional(rollbackFor = {EntityNotFoundException.class, BadRequestException.class})
    @Override
    public Message<String> cancelSubscription(String email) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Cancel subscription for user: " + email);
        SubscribedUser subscribedUser = findByUser(email);
        if (subscribedUser.getPaypalSubscriptionId() == null && subscribedUser.getPaymentSubscriptionId() == null) {
            throw new BadRequestException("Free plan cannot be canceled.");
        }

        subscriptionContextService.setProcess(SubscriptionProcessType.CANCEL);
        return subscriptionContextService.process(null, subscribedUser, null, subscribedUser.getUser(), null);
    }


    @Override
    public SubscribedUser findByUser(String email) throws EntityNotFoundException {
        log.info("Fetching subscribed user.");
        return repo.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Subscribed user is not found."));
    }

    @Override
    public Message<CurrentSubscriptionResponse> getCurrentSubscription(String email) throws EntityNotFoundException {
        log.info("Fetching current subscription for user: " + email);
        SubscribedUser subscribedUser = findByUser(email);

        List<String> permissions = new ArrayList<>();

        if (subscribedUser != null) {
            // if user subscribe a free plan than permission list is empty
            if (subscribedUser.getSubscription().getId() != 1) {
                List<SubscriptionPermission> subscriptionPermissions =
                        subscriptionPermissionService.findBySubscriptionAndIsActive(
                                subscribedUser.getSubscription().getId());

                if (subscriptionPermissions != null && !subscriptionPermissions.isEmpty()) {
                    for (SubscriptionPermission subscriptionPermission : subscriptionPermissions) {
                        permissions.add(String.valueOf(subscriptionPermission.getPermission().getName()));
                    }
                }
            }
        }

        Subscription subscription = subscribedUser.getSubscription();
        PlanType planType = subscription != null ? subscription.getPlanType() : null;

        boolean isCouponBased = subscribedUser.getCoupon() != null;
        boolean isFreePlan = subscribedUser.getPaypalSubscriptionId() == null &&
                subscribedUser.getPaymentSubscriptionId() == null;

        boolean isStandard = planType == PlanType.STANDARD;
        boolean isPremiumOrUltimate = planType == PlanType.PREMIUM || planType == PlanType.ULTIMATE;

        boolean hasPurchased = false;
        if (isStandard) {
            hasPurchased = subscriptionLogRepo.existsByUserId(subscribedUser.getUser().getId());
        }

        String planMessage;
        if (isCouponBased) {
            planMessage = "Your plan is coupon-based subscription which is valid till " +
                    DateUtils.showDate(subscribedUser.getCouponValidTill()) + ".";
        } else if (isFreePlan) {
            planMessage = "It's a free plan, no billing cycle occurred.";
        } else if ((isStandard && hasPurchased) || isPremiumOrUltimate) {
            planMessage = "Your plan will be automatically renewed after each month. It will be charged as one payment of $"
                    + subscription.getPrice() + " USD after " + subscription.getDuration() + " month.";
        } else if (isStandard && !hasPurchased) {
            planMessage = "Your plan will be automatically renewed after each month excluding free trial period. It will be charged as one payment of $"
                    + subscription.getPrice() + " USD after " + subscription.getDuration() + " month.";
        } else {
            planMessage = "Your subscription details are unavailable.";
        }

        CurrentSubscriptionResponse subscriptionResponse = CurrentSubscriptionResponse.builder()
                .planName(subscribedUser.getSubscription().getName())
                .planMessage(planMessage)
                .planPrice(
                        subscribedUser.getCoupon() != null ?
                                "Coupon Based" :
                                (Objects.isNull(subscribedUser.getPaypalSubscriptionId()) && Objects.isNull(subscribedUser.getPaymentSubscriptionId())) ?
                                        "Free" : "" + subscribedUser.getSubscription().getPrice()
                )
                .freeTrialMessage(
                        subscribedUser.getCoupon() != null
                                ? "Valid till " + DateUtils.showDate(subscribedUser.getCouponValidTill())
                                : (subscribedUser.getPaypalSubscriptionId() == null &&
                                subscribedUser.getPaymentSubscriptionId() == null)
                                ? "Free forever"
                                : (subscribedUser.getSubscription() != null &&
                                subscribedUser.getSubscription().getPlanType() == PlanType.STANDARD)
                                ? "2 weeks free trial"
                                : null
                )

                .isSubscribed(true)
                .isCouponBasedSubscription(Objects.nonNull(subscribedUser.getCoupon()))
                .planType(subscribedUser.getSubscription().getPlanType().name())
                .subscriptionId(subscribedUser.getSubscription().getId())
                .permissions(permissions)
                .build();
        return new Message<CurrentSubscriptionResponse>()
                .setMessage("Fetching current subscriptions successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(subscriptionResponse);
    }

    @Override
    public SubscribedUser findBySubscribedId(String subscribedId) {
        log.info("Fetching subscribed user by subscribed id.");
        return repo.findBySubscribedId(subscribedId);
    }

    @Override
    public SubscribedUser findByCustomerProfileId(String customerProfileId) {
        log.info("Fetching subscribed user by customer profile id.");
        return repo.findByCustomerProfileId(customerProfileId).orElse(null);
    }

    @Override
    public List<PaidUser> fetchAllPaidSubscriptionAfterTrialPeriod() {
        log.info("Fetching all paid subscribed users.");
        List<Tuple> paidUsers = repo.fetchAllPaidUsers();
        return PaidUser.map(paidUsers);
    }

    @Override
    public SubscribedUser fetchByCustomerProfileId(String customerProfileId) throws EntityNotFoundException {
        log.info("Fetching subscribed user by the customer profile id.");
        return repo.findByCustomerProfileId(customerProfileId)
                .orElseThrow(() -> new EntityNotFoundException("No data found."));
    }

    @Override
    public List<SubscribedUser> findAllSubscribedUserWhichAreCancelled() {
        log.info("Fetching all cancelled subscribed user...");
        return repo.findAllByEndDateIsNotNull();
    }

    @Override
    public List<SubscribedUser> fetchAllCouponBasedSubscriptions() throws EntityNotFoundException {
        log.info("Fetching all coupon based subscribed users...");
        List<SubscribedUser> subscribedUserList = repo.findAllByCouponIsNotNull();
        if(subscribedUserList.isEmpty())
            throw new EntityNotFoundException("No data found.");
        return subscribedUserList;
    }
}
