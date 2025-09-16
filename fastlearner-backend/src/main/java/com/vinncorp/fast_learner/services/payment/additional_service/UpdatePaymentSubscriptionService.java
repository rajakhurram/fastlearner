package com.vinncorp.fast_learner.services.payment.additional_service;

import com.vinncorp.fast_learner.config.GenericRestClient;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentSubscriptionService {

    private final ISubscribedUserService subscribedUserService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final ITransactionHistoryService transactionHistoryService;
    private final GenericRestClient restClient;

    /**
     * Upgrading the subscription plan from a FREE to any PAID subscription.
     *
     * */
    public Message<String> update(Subscription subscription, SubscribedUserProfile subscribedUserProfile,
                                               User user, TransactionHistory transactionHistory, SubscribedUser from,
                                  SubscribedUser to, Date previousTrialEndDate, Date prevBillingCycleDate)
            throws InternalServerException {
        log.info("Upgrading subscription from a FREE plan to a PAID plan for user: {}", user.getId());


        CreateSubsRequest createSubsRequest = new CreateSubsRequest();
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");

        XMLGregorianCalendar startDate = calculateStartDate(createSubsRequest, previousTrialEndDate, prevBillingCycleDate);

        Date nextBillingCycleDate = calculateNextCycle(subscription, startDate);

        createSubsRequest.setAmount(subscription.getPrice());
        createSubsRequest.setTrialAmount(BigDecimal.ZERO);
        createSubsRequest.setCustomerProfileId(subscribedUserProfile.getCustomerPaymentId());
        createSubsRequest.setCustomerPaymentProfileId(subscribedUserProfile.getCustomerPaymentProfileId());
        createSubsRequest.setSubscriptionTypeName(subscription.getPlanType().name());

        CreateSubscriptionResponse response = restClient.makeRequest("/api/v1/subscription/create", HttpMethod.POST, createSubsRequest, CreateSubscriptionResponse.class);

        Message<String> result = processApiResponse(transactionHistory, response);

        transactionHistory.setTrialEndDate(Date.from(startDate.toGregorianCalendar().toZonedDateTime().toInstant()));
        transactionHistory.setSubscriptionNextCycle(nextBillingCycleDate);
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

    private XMLGregorianCalendar calculateStartDate(CreateSubsRequest schedule, Date prevTrialEndDate, Date prevBillingCycleDate)
            throws InternalServerException {
        XMLGregorianCalendar startDate = null;
        try {
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } catch (Exception e) {
            log.error("Error calculating start date: {}", e.getMessage());
            throw new InternalServerException("Error calculating start date: " + e.getLocalizedMessage());
        }
        LocalDateTime currentDate = LocalDateTime.now();
        schedule.setTrialOccurrences((short) (0));

        // When the user is in paid period and updating the plan then current date will be the billing cycle date of
        // previous subscription billing cycle date
        if (currentDate.isAfter(prevTrialEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())) {
            currentDate = prevTrialEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }else {
            currentDate = prevBillingCycleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        startDate.setYear(currentDate.getYear());
        startDate.setMonth(currentDate.getMonthValue());
        startDate.setDay(currentDate.getDayOfMonth());

        schedule.setStartDate(startDate);
        schedule.setTotalOccurrences((short) 9999);

        return startDate;
    }

    private Date calculateNextCycle(Subscription subscription, XMLGregorianCalendar startDate) {
        Date baseDate =Date.from(startDate.toGregorianCalendar().toZonedDateTime().toInstant());
        return subscription.getDuration() == 1
                ? DateUtils.addMonthsToDate(baseDate, 1)
                : DateUtils.addMonthsToDate(baseDate, 12);
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
                    .setMessage("Successfully updated the subscription.")
                    .setData("Successfully updated the subscription.");
        } else {
            log.error("Connection not established with Payment API");
            throw new InternalServerException("Error communicating with Payment API");
        }
    }
}
