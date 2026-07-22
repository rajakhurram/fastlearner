package com.vinncorp.fast_learner.services.subscription;

import com.vinncorp.fast_learner.dtos.payout.PaidUser;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;
import com.vinncorp.fast_learner.repositories.subscription.SubscribedUserRepository;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionValidationsRepository;
import com.vinncorp.fast_learner.response.subscription.CurrentSubscriptionResponse;
import com.vinncorp.fast_learner.services.authorize_net.additional_service.IAuthNetAdditionalSubscriptionService;
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
import org.springframework.transaction.annotation.Propagation;
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

    private final IAuthNetAdditionalSubscriptionService authNetAdditionalSubscriptionService;
    private final IUserService userService;

    private final ITransactionHistoryService transactionHistoryService;
    private final SubscriptionContextService subscriptionContextService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final ISubscriptionPermissionService subscriptionPermissionService;
    private final SubscriptionValidationsRepository subscriptionValidationsRepository;

    public SubscribedUserService(SubscribedUserRepository repo, @Lazy ISubscriptionService subscriptionService,
                                 @Lazy ISubscribedUserProfileService subscribedUserProfileService, IAuthNetAdditionalSubscriptionService
                                         authNetAdditionalSubscriptionService, @Lazy IUserService userService,
                                 ITransactionHistoryService transactionHistoryService,
                                 @Lazy SubscriptionContextService subscriptionContextService, @Lazy SubscriptionLogRepository subscriptionLogRepo,
                                 ISubscriptionPermissionService subscriptionPermissionService, SubscriptionValidationsRepository subscriptionValidationsRepository) {
        this.repo = repo;
        this.subscriptionService = subscriptionService;
        this.subscribedUserProfileService = subscribedUserProfileService;
        this.authNetAdditionalSubscriptionService = authNetAdditionalSubscriptionService;
        this.userService = userService;
        this.transactionHistoryService = transactionHistoryService;
        this.subscriptionContextService = subscriptionContextService;
        this.subscriptionLogRepo = subscriptionLogRepo;
        this.subscriptionPermissionService = subscriptionPermissionService;
        this.subscriptionValidationsRepository = subscriptionValidationsRepository;
    }

    @Override
    @Transactional
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
    public Message<String> completeAuthorizeNetSubscription(String authorizeNetSubscriptionId, String email) throws EntityNotFoundException, InternalServerException {
        log.info("Complete authorize net subscription for user " + email);

        SubscribedUser subscribedUser = repo.findByUserEmail(email).orElseThrow(() -> new EntityNotFoundException("User doesn't subscribed any plan"));

        subscribedUser.setSubscribedId(authorizeNetSubscriptionId);
        subscribedUser.setAuthorizeNetSubscriptionId(authorizeNetSubscriptionId);
        subscribedUser.setPaymentStatus(PaymentStatus.PAID);

        // update user
        updateUser(email);

        log.info("Authorize.Net Subscription is completed.");
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Authorize.Net Subscription is completed.")
                .setData("Authorize.Net Subscription is completed.");
    }


    @Transactional(rollbackFor = {EntityNotFoundException.class, BadRequestException.class})
    @Override
    public Message<String> cancelSubscription(String email) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Cancel subscription for user: " + email);
        SubscribedUser subscribedUser = findByUser(email);
        if (subscribedUser.getPaypalSubscriptionId() == null && subscribedUser.getAuthorizeNetSubscriptionId() == null) {
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

    public SubscribedUser findBySubscribedUser(String email){
        log.info("Fetching subscribed user detail.");
        SubscribedUser subscribedUser = repo.findByUser_Email(email);

        if (subscribedUser == null) {
            log.warn("Subscribed user not found for email: {}", email);
            return null;
        }

        log.info("Subscribed user found with email: {}", subscribedUser.getUser().getEmail());
        return subscribedUser;
    }

    @Override
    public Message<CurrentSubscriptionResponse> getCurrentSubscription(String email) throws EntityNotFoundException {
        log.info("Fetching current subscription for user: " + email);
        SubscribedUser subscribedUser = findByUser(email);


        List<String> permissions = new ArrayList<>();

        if (subscribedUser != null) {
            int noOfPagesUsed = subscribedUser.getUser().getNoOfPagesUsed()==null ? 0 : subscribedUser.getUser().getNoOfPagesUsed();

            SubscriptionValidations subscriptionValidations = subscriptionValidationsRepository
                    .findByValidationNameAndSubscriptionAndIsActive(
                            SubscriptionValidation.PDF_PAGE_COUNT.name(),
                            subscribedUser.getSubscription(),
                            true
                    );

            if (subscriptionValidations != null && subscriptionValidations.getValue() != null) {
                long allowedPages = subscriptionValidations.getValue();

                if (noOfPagesUsed < allowedPages) {
                    permissions.add(PermissionName.AFFILIATE.name());
                }
            } else {
                permissions.add(PermissionName.AFFILIATE.name());
            }
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
                subscribedUser.getAuthorizeNetSubscriptionId() == null;

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
        } else if (isStandard) {
            planMessage = "Your plan will be automatically renewed after each month. It will be charged as one payment of $"
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
                                (Objects.isNull(subscribedUser.getPaypalSubscriptionId()) && Objects.isNull(subscribedUser.getAuthorizeNetSubscriptionId())) ?
                                        "Free" : "" + subscribedUser.getSubscription().getPrice()
                )
                .freeTrialMessage(
                        subscribedUser.getCoupon() != null
                                ? "Valid till " + DateUtils.showDate(subscribedUser.getCouponValidTill())
                                : (subscribedUser.getPaypalSubscriptionId() == null &&
                                subscribedUser.getAuthorizeNetSubscriptionId() == null)
                                ? "Free forever"
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

    public Message<String> fetchCurrentSubscriptionPlanType(String name) throws EntityNotFoundException {
        SubscribedUser user = this.findByUser(name);
        Message<String> message = new Message<>();
        message.setStatus(HttpStatus.OK.value());
        message.setCode(HttpStatus.OK.toString());
        message.setMessage("Fetching current subscription plan type successfully.");
        message.setData(user.getSubscription().getPlanType().name());
        return message;
    }

}
