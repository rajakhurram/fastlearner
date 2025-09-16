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
public class DowngradePaymentSubscriptionService {

    private final ISubscribedUserService subscribedUserService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final ITransactionHistoryService transactionHistoryService;
    private final GenericRestClient restClient;


    public Message<String> downgrade(Subscription subscription, SubscribedUserProfile subscribedUserProfile,
                                               User user, TransactionHistory transactionHistory, SubscribedUser from, SubscribedUser to, Date previousSubsCycleDate)
            throws InternalServerException {
        log.info("Downgrading the subscription...");
        CreateSubsRequest createSubsRequest=new CreateSubsRequest();
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");

        XMLGregorianCalendar startDate = calculateStartDate(previousSubsCycleDate,createSubsRequest);

        Date nextBillingCycleDate = calculateNextCycle(subscription, startDate, previousSubsCycleDate);

         this.createSubscriptionType(subscription, subscribedUserProfile, createSubsRequest);

        CreateSubscriptionResponse response = restClient.makeRequest(
                "/api/v1/subscription/create" , HttpMethod.POST, createSubsRequest, CreateSubscriptionResponse.class);


        log.info("API response received: {}", response);

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

        return result;
    }

    private XMLGregorianCalendar calculateStartDate(Date previousSubsCycleDate,CreateSubsRequest createSubsRequest)
            throws InternalServerException {
        XMLGregorianCalendar startDate = null;
        try {
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } catch (Exception e) {
            log.error("Error calculating start date: {}", e.getMessage());
            throw new InternalServerException("Error calculating start date: " + e.getLocalizedMessage());
        }
        LocalDateTime currentDate = previousSubsCycleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        createSubsRequest.setTrialOccurrences((short) (0));

        startDate.setYear(currentDate.getYear());
        startDate.setMonth(currentDate.getMonthValue());
        startDate.setDay(currentDate.getDayOfMonth());

        createSubsRequest.setStartDate(startDate);
        createSubsRequest.setTotalOccurrences((short) 9999);

        return startDate;
    }

    private Date calculateNextCycle(Subscription subscription, XMLGregorianCalendar startDate, Date trialEndDate) {
        Date baseDate = trialEndDate != null ? trialEndDate : Date.from(startDate.toGregorianCalendar().toZonedDateTime().toInstant());
        return subscription.getDuration() == 1
                ? DateUtils.addMonthsToDate(baseDate, 1)
                : DateUtils.addMonthsToDate(baseDate, 12);
    }

    private void createSubscriptionType(Subscription subscription, SubscribedUserProfile subscribedUserProfile,
                                                       CreateSubsRequest createSubsRequest) {

        createSubsRequest.setSubscriptionTypeName(subscription.getName());

        createSubsRequest.setAmount(subscription.getPrice());
        createSubsRequest.setTrialAmount(BigDecimal.ZERO);

        createSubsRequest.setCustomerProfileId(subscribedUserProfile.getCustomerPaymentId());
        createSubsRequest.setCustomerPaymentProfileId(subscribedUserProfile.getCustomerPaymentProfileId());

        log.info("Subscription request type created: {}", createSubsRequest);
    }


    // TODO manage the transaction history for future process
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
                    .setMessage("Successfully downgraded the subscription.")
                    .setData("Successfully downgraded the subscription.");
        } else {
            log.error("Connection not established with Payment API");
            throw new InternalServerException("Error communicating with Payment API");
        }
    }
}
