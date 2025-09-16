package com.vinncorp.fast_learner.services.payment.additional_service;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.subscription.RemainingBalance;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionLog;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.request.payment_gateway.subscription.CreateSubsRequest;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.subscription.CreateSubscriptionResponse;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.date.DateUtils;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpgradePaymentSubscriptionService {

    private final ISubscribedUserService subscribedUserService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final ITransactionHistoryService transactionHistoryService;
    private final GenericRestClient restClient;


    /**
     * Upgrading the subscription plan from a FREE to any PAID subscription.
     *
     * */
    public Message<String> upgradeFromFreePlan(Subscription subscription, SubscribedUserProfile subscribedUserProfile,
                                               User user, TransactionHistory transactionHistory, SubscribedUser from, SubscribedUser to)
            throws InternalServerException{
        log.info("Upgrading subscription from a FREE plan to a PAID plan for user: {}", user.getId());

        log.info("Configuration initialized");

        CreateSubsRequest createSubsRequest=new CreateSubsRequest();
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");


        XMLGregorianCalendar startDate = calculateStartDate(subscription, user.getId(), null,createSubsRequest);

        Date nextBillingCycleDate = calculateNextCycle(subscription, startDate, transactionHistory.getTrialEndDate());
        RemainingBalance remainingBalance =new RemainingBalance(0.0,RemainingBalance.TRIAL_PERIOD.DAY);

        createSubsRequest.setStartDate(startDate);
        createSubsRequest.setTotalOccurrences((short) 9999);
        this.createSubscriptionType(subscription, subscribedUserProfile,  remainingBalance,createSubsRequest);

        CreateSubscriptionResponse response = restClient.makeRequest(
                "/api/v1/subscription/create" , HttpMethod.POST, createSubsRequest, CreateSubscriptionResponse.class);


        log.info("API response received: {}", response);

        Message<String> result = processApiResponse(transactionHistory, response);

        transactionHistory.setTrialEndDate(Date.from(startDate.toGregorianCalendar().toZonedDateTime().toInstant()));
        transactionHistory.setSubscriptionNextCycle(nextBillingCycleDate);
        transactionHistory.setCreationAt(new Date());
        if (transactionHistory.getAuthSubscriptionId().equalsIgnoreCase("0")){
            throw new InternalServerException("PLease try again Later due connection error.");
        }
        transactionHistoryService.save(transactionHistory);

        SubscriptionLog subscriptionLog = new SubscriptionLog();
        subscriptionLog.setPrevAuthSubscriptionId(from.getPaymentSubscriptionId());
        subscriptionLog.setPrevSubscriptionId(from.getSubscription().getId());
        subscriptionLog.setCurrentAuthSubscriptionId(transactionHistory.getAuthSubscriptionId());
        subscriptionLog.setCurrentSubscriptionId(transactionHistory.getSubscription().getId());
        subscriptionLog.setCustomerProfileId(subscribedUserProfile.getCustomerPaymentId());
        subscriptionLog.setPaymentProfileId(subscribedUserProfile.getCustomerPaymentProfileId());
        subscriptionLog.setCreatedAt(new Date());
        subscriptionLog.setUserId(to.getUser().getId());
        subscriptionLogRepo.save(subscriptionLog);

        to.setSubscription(subscription);
        to.setPaymentSubscriptionId(transactionHistory.getAuthSubscriptionId());
        to.setSubscribedId(transactionHistory.getAuthSubscriptionId());
        to.setCustomerProfileId(subscribedUserProfile.getCustomerPaymentId());
        subscribedUserService.save(to);

        return result;
    }

    private XMLGregorianCalendar calculateStartDate(Subscription subscription, Long userId, RemainingBalance remainingBalance
            ,CreateSubsRequest createSubsRequest)
            throws InternalServerException {
        var subscriptionLog = subscriptionLogRepo.findTopByUserIdOrderByCreatedAtDesc(userId);
        XMLGregorianCalendar startDate = null;
        try {
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } catch (Exception e) {
            log.error("Error calculating start date: {}", e.getMessage());
            throw new InternalServerException("Error calculating start date: " + e.getLocalizedMessage());
        }
        LocalDateTime currentDate = LocalDateTime.now();

        createSubsRequest.setTrialOccurrences((short) (0));
        // Add trial period only if the subscription is for standard plan
        if (subscription.getPlanType() == PlanType.STANDARD && Objects.isNull(subscriptionLog)) {
            log.info("First time subscription taken, including 2 week trial period");
            currentDate = currentDate.plusWeeks(2);
            createSubsRequest.setTrialOccurrences((short) (1));
        }

        if (remainingBalance != null) {
            if (remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.DAY) {
                currentDate.plusDays(remainingBalance.getNoOfTrialDays());
            } else if (remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.MONTHLY) {
                currentDate.plusMonths(1);
            } else if (remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.YEARLY) {
                currentDate.plusYears(1);
            }
        }

        startDate.setYear(currentDate.getYear());
        startDate.setMonth(currentDate.getMonthValue());
        startDate.setDay(currentDate.getDayOfMonth());

        return startDate;
    }

    private Date calculateNextCycle(Subscription subscription, XMLGregorianCalendar startDate, Date trialEndDate) {
        Date baseDate = trialEndDate != null ? trialEndDate : Date.from(startDate.toGregorianCalendar().toZonedDateTime().toInstant());
        return subscription.getDuration() == 1
                ? DateUtils.addMonthsToDate(baseDate, 1)
                : DateUtils.addMonthsToDate(baseDate, 12);
    }

    private void createSubscriptionType(Subscription subscription, SubscribedUserProfile subscribedUserProfile,
                                                       RemainingBalance remainingBalance,CreateSubsRequest createSubsRequest)
    {
        createSubsRequest.setAmount(subscription.getPrice());
        createSubsRequest.setSubscriptionTypeName(subscription.getName());

        if (remainingBalance != null) {
            if (remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.DAY) {
                createSubsRequest.setTrialAmount(BigDecimal.ZERO);
            } else if (remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.MONTHLY) {
                createSubsRequest.setTrialAmount(BigDecimal.valueOf(remainingBalance.getRemainingBalance()));
            } else if (remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.YEARLY) {
                createSubsRequest.setTrialAmount(BigDecimal.valueOf(remainingBalance.getRemainingBalance()));
            }

        }
        createSubsRequest.setCustomerProfileId(subscribedUserProfile.getCustomerPaymentId());
        createSubsRequest.setCustomerPaymentProfileId(subscribedUserProfile.getCustomerPaymentProfileId());

        log.info("Subscription type created: {}", createSubsRequest);
    }

    private Message<String> processApiResponse(TransactionHistory transactionHistory, CreateSubscriptionResponse response)
            throws InternalServerException{
        if (response != null) {
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                transactionHistory.setAuthSubscriptionId(response.getSubscriptionId());
                transactionHistory.setCustomerPaymentProfileId(response.getProfile().getCustomerPaymentProfileId());
            } else {
                transactionHistory.setAuthSubscriptionId("0");
                transactionHistory.setStatus(GenericStatus.INACTIVE);
            }
            transactionHistory.setResponseCode(String.valueOf(response.getMessages().getResultCode()));
            transactionHistory.setResponseText(response.getMessages().getMessage().get(0).getText());

             return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Successfully upgraded the subscription.")
                    .setData("Successfully upgraded the subscription.");
        } else {
            log.error("Connection not established with Payment API");
            throw new InternalServerException("Error communicating with Payment API");
        }
    }

    public Message<String> upgradeFromPaidPlan(Subscription subscription, SubscribedUserProfile subscribedUserProfile,
                                               User user, TransactionHistory transactionHistory, SubscribedUser previousSubscribedUser,
                                               SubscribedUser subscribedUser, RemainingBalance remainingBalance) throws InternalServerException, EntityNotFoundException {
        log.info("Upgrading subscription from a FREE plan to a PAID plan for user: {}", user.getId());

        CreateSubsRequest createSubsRequest=new CreateSubsRequest();
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");

        XMLGregorianCalendar startDate = calculateStartDate(subscription, user.getId(), remainingBalance,createSubsRequest);
        //set date & payment occurrence
        createSubsRequest.setStartDate(startDate);
        createSubsRequest.setTotalOccurrences((short) 9999);

        Date nextBillingCycleDate = calculateNextCycle(subscription, startDate, transactionHistory.getTrialEndDate());

                this.createSubscriptionType(subscription, subscribedUserProfile,remainingBalance,createSubsRequest);

        CreateSubscriptionResponse response = restClient.makeRequest(
                "/api/v1/subscription/create" , HttpMethod.POST, createSubsRequest, CreateSubscriptionResponse.class);


        log.info("API response received: {}", response);

        Message<String> result = processApiResponse(transactionHistory, response);

        LocalDate localDate = startDate.toGregorianCalendar().toZonedDateTime().toLocalDate();
        if (remainingBalance != null) {
            if(remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.DAY)
                localDate.minusDays(remainingBalance.getNoOfTrialDays());
            else if(remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.MONTHLY)
                localDate.minusMonths(1);
            else if(remainingBalance.getTrialPeriod() == RemainingBalance.TRIAL_PERIOD.YEARLY)
                localDate.minusDays(1);
        }

        transactionHistory.setTrialEndDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        transactionHistory.setSubscriptionNextCycle(nextBillingCycleDate);
        transactionHistoryService.save(transactionHistory);

        if (transactionHistory.getAuthSubscriptionId().equalsIgnoreCase("0")){
           handleSubscriptionError(transactionHistory);
        }
        SubscriptionLog subscriptionLog = new SubscriptionLog();
        subscriptionLog.setPrevAuthSubscriptionId(previousSubscribedUser.getPaymentSubscriptionId());
        subscriptionLog.setPrevSubscriptionId(previousSubscribedUser.getSubscription().getId());
        subscriptionLog.setCurrentAuthSubscriptionId(transactionHistory.getAuthSubscriptionId());
        subscriptionLog.setCurrentSubscriptionId(transactionHistory.getSubscription().getId());
        subscriptionLog.setCustomerProfileId(subscribedUserProfile.getCustomerPaymentId());
        subscriptionLog.setPaymentProfileId(subscribedUserProfile.getCustomerPaymentProfileId());
        subscriptionLog.setCreatedAt(new Date());
        subscriptionLog.setUserId(subscribedUser.getUser().getId());
        subscriptionLogRepo.save(subscriptionLog);

        subscribedUser.setEndDate(null);
        subscribedUser.setSubscription(subscription);
        subscribedUser.setPaymentSubscriptionId(transactionHistory.getAuthSubscriptionId());
        subscribedUser.setSubscribedId(transactionHistory.getAuthSubscriptionId());
        subscribedUser.setCustomerProfileId(subscribedUserProfile.getCustomerPaymentId());
        subscribedUserService.save(subscribedUser);

        return result;
    }

    private void handleSubscriptionError(TransactionHistory transactionHistory) throws EntityNotFoundException, InternalServerException {
        transactionHistory=transactionHistoryService.findById(transactionHistory.getOldTransactionId());
        transactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
        transactionHistory.setStatus(GenericStatus.ACTIVE);
        transactionHistoryService.save(transactionHistory);
        throw new InternalServerException("PLease try again Later due connection error.");
    }
}
