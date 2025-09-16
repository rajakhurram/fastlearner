package com.vinncorp.fast_learner.services.payment.webhook;

import com.vinncorp.fast_learner.dtos.payment.webhook.PaymentWebhookRequest;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.Payment.WebhookLog;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.Payment.WebhookLogRepository;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.services.payment.payment_profile.PaymentProfileService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.date.DateUtils;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import com.vinncorp.fast_learner.response.subscription.GetTransactionDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService implements IPaymentWebhookService {

    private final ISubscriptionService subscriptionService;
    private final ISubscribedUserService subscribedUserService;
    private final WebhookLogRepository webhookLogRepo;
    private final ITransactionHistoryService transactionHistoryService;
    private final PaymentProfileService paymentProfileService;

    // On cancelled, expired or suspended subscription we should have to change the subscription of the user to the
    // free plan and cancel the subscription.
    @Override
    public void subscriptionTermination(PaymentWebhookRequest request) throws EntityNotFoundException, InternalServerException {
        log.info("Subscription termination process....");
        var webhookLog = WebhookLog.builder().content(request.toString()).build();
        webhookLogRepo.save(webhookLog);

        Subscription subscription = subscriptionService.findBySubscriptionId(1L).getData();
        SubscribedUser subscribedUser = subscribedUserService.findBySubscribedId("" + request.getPayload().getId());
        if (subscribedUser != null && subscribedUser.getEndDate() == null) {


            TransactionHistory transactionHistory = transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(
                    subscribedUser.getSubscribedId(), GenericStatus.ACTIVE);

            updateTransactionHistory(transactionHistory);

            saveTransactionHistoryForFreePlan(subscription, transactionHistory, subscribedUser.getUser());

            subscribedUser.setEndDate(transactionHistory.getTrialEndDate().toInstant().isBefore(Instant.now()) ?
                    transactionHistory.getTrialEndDate() : transactionHistory.getSubscriptionNextCycle());

            subscribedUser.setSubscription(subscription);
            subscribedUser.setPaymentStatus(PaymentStatus.PAID);
            subscribedUser.setPaypalSubscriptionId(null);
            subscribedUser.setSubscribedId(null);
            subscribedUser.setPaymentSubscriptionId(null);
            subscribedUser.setStartDate(new Date());
            subscribedUserService.save(subscribedUser);
        }
    }

    /**
     * This method is used to save the transaction history for a free plan.
     * It will save the transaction history with the given subscription and user.
     * The status of the transaction history will be set to GenericStatus.ACTIVE and the subscription status to SubscriptionStatus.SUCCESS.
     * The authSubscriptionId will be set to "FREE" as it cannot be null.
     * The creationAt will be set to the current time.
     * The subscriptionAmount will be set to the price of the given subscription.
     * The subscription will be set to the given subscription.
     * The oldTransactionId will be set to the id of the old transaction history.
     * The user will be set to the given user.
     * @param subscription the subscription to be saved.
     * @param oldTransactionHistory the old transaction history.
     * @param user the user.
     */
    private void saveTransactionHistoryForFreePlan(Subscription subscription, TransactionHistory oldTransactionHistory, User user) {
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .status(GenericStatus.ACTIVE)
                .subscriptionStatus(SubscriptionStatus.SUCCESS)
                .authSubscriptionId("FREE") // As authSubscriptionId cannot be null so it cannot be left null
                .creationAt(new Date())
                .subscriptionAmount(subscription.getPrice())
                .subscription(subscription)
                .oldTransactionId(oldTransactionHistory.getId())
                .user(user)
                .build();
        transactionHistoryService.save(transactionHistory);
    }

    private void updateTransactionHistory(TransactionHistory transactionHistory) {
        transactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
        transactionHistory.setStatus(GenericStatus.INACTIVE);
        transactionHistoryService.save(transactionHistory);
    }

    @Override
    public void logging(String request) {
        log.info("Payment event calling...");
        var webhookLog = WebhookLog.builder().content(request).build();
        webhookLog.setCreationDate(new Date());
        webhookLogRepo.save(webhookLog);
        log.info("Payment event processed.");
    }

    @Override
    public void paymentSubscription(Map<String, Object> webhookPayload) throws InternalServerException {
        log.info("Payment method run started.");

        // Retrieve transaction details
//        String transactionId = (String) webhookPayload.get("transactionId");
//        log.info("Fetching transaction details for transactionId: {}", transactionId);
        String transactionId = null;
        // Extract the "payload" map from webhookPayload
        Map<String, Object> payload = (Map<String, Object>) webhookPayload.get("payload");

        if (payload != null) {
            transactionId = (String) payload.get("id");  // "id" contains the transaction ID
            log.info("Fetching transaction details for transactionId: {}", transactionId);
        } else {
            log.error("Payload is missing in webhook data.");
        }

        GetTransactionDetailsResponse getTransactionDetailsResponse =
                paymentProfileService.getTransactionDetail(transactionId);
        log.info("Transaction details response received: {}", getTransactionDetailsResponse);

        if (getTransactionDetailsResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {

            // Transaction successful
            String subscriptionId = String.valueOf(getTransactionDetailsResponse.getTransaction().getSubscription().getId());
            log.info("Transaction successful. SubscriptionId: {}", subscriptionId);

            SubscribedUser subscribedUser = subscribedUserService.findBySubscribedId(subscriptionId);
            if(subscribedUser.getCoupon() != null && subscribedUser.getSubscription().getPrice() == getTransactionDetailsResponse.getTransaction().getAuthAmount().doubleValue()){
                subscribedUser.setCoupon(null);
                subscribedUser.setCouponValidTill(null);
            }

            XMLGregorianCalendar submitTimeLocal = getTransactionDetailsResponse.getTransaction().getSubmitTimeLocal();
            LocalDateTime dateTime = submitTimeLocal.toGregorianCalendar()
                    .toZonedDateTime()
                    .toLocalDateTime();
            log.info("Transaction submit time (local): {}", dateTime);

            try {
                TransactionHistory transactionHistory = transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(
                        subscriptionId, GenericStatus.ACTIVE);
                log.info("Retrieved transaction history for subscriptionId: {}", subscriptionId);

                if (transactionHistory != null) {
                    log.info("Transaction history found for subscriptionId: {}. Payment status: {}", subscriptionId, transactionHistory.getPaymentStatus());

                    if (transactionHistory.getPaymentStatus() != null && transactionHistory.getPaymentStatus().equals(PaymentStatus.PAID)) {
                        // Handle paid transaction scenario
                        log.info("Transaction already paid. Updating status to INACTIVE and creating a new transaction history entry.");

                        transactionHistory.setStatus(GenericStatus.INACTIVE);
                        transactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
                        transactionHistory.setUpdatedDate(new Date());
                        transactionHistory = transactionHistoryService.save(transactionHistory);
                        log.info("Updated transaction history to INACTIVE. Creating new transaction history entry.");

                        TransactionHistory transactionHistoryNew = new TransactionHistory();
                        transactionHistoryNew.setCustomerPaymentProfileId(transactionHistory.getCustomerPaymentProfileId());
                        transactionHistoryNew.setSubscription(transactionHistory.getSubscription());
                        transactionHistoryNew.setCreationAt(new Date());
                        transactionHistoryNew.setAuthSubscriptionId(transactionHistory.getAuthSubscriptionId());
                        transactionHistoryNew.setSubscriptionAmount(transactionHistory.getSubscriptionAmount());

                        transactionHistoryNew.setTrialEndDate(transactionHistory.getSubscriptionNextCycle());
                        XMLGregorianCalendar startDate = convertDateToXMLGregorianCalendar(transactionHistory.getSubscriptionNextCycle());
                        Date nextBillingCycleDate = calculateNextCycle(transactionHistory.getSubscription(), startDate,
                                null);
                        transactionHistoryNew.setSubscriptionNextCycle(nextBillingCycleDate);
                        transactionHistoryNew.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
                        transactionHistoryNew.setResponseCode(getTransactionDetailsResponse.getMessages() == null ? null :
                                getTransactionDetailsResponse.getMessages().getResultCode().getValue());
                        transactionHistoryNew.setResponseText(getTransactionDetailsResponse.getMessages() == null ? null :
                                getTransactionDetailsResponse.getMessages().getMessage().get(0).getText());
                        transactionHistoryNew.setUser(transactionHistory.getUser());
                        transactionHistoryNew.setOldTransactionId(transactionHistory.getId());
                        transactionHistoryNew.setPaymentStatus(PaymentStatus.PAID);
                        transactionHistoryNew.setStatus(GenericStatus.ACTIVE);
                        transactionHistoryNew.setSettledDate(dateTime);
//                        transactionHistoryNew.setSubscriptionNextCycle(DateUtils.addMonthsToLocalDate(dateTime, transactionHistory.getSubscription().getDuration()));
                        transactionHistoryNew.setExternalTransactionId(getTransactionDetailsResponse.getTransaction().getTransId());
                        transactionHistoryNew.setSubscriptionAmount(getTransactionDetailsResponse.getTransaction().getAuthAmount().doubleValue());
                        transactionHistoryNew.setExternalTransactionId(transactionId);

                        transactionHistoryService.save(transactionHistoryNew);
                        log.info("Created new transaction history entry for subscriptionId: {}.", subscriptionId);
                    } else {
                        // Handle scenario for continued subscription
                        log.info("Transaction not paid yet. Updating transaction history to CONTINUE with status PAID.");

                        transactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
                        transactionHistory.setPaymentStatus(PaymentStatus.PAID);
                        transactionHistory.setSettledDate(dateTime);
                        transactionHistory.setResponseCode(getTransactionDetailsResponse.getMessages() == null ? null :
                                getTransactionDetailsResponse.getMessages().getResultCode().getValue());
                        transactionHistory.setResponseText(getTransactionDetailsResponse.getMessages() == null ? null :
                                getTransactionDetailsResponse.getMessages().getMessage().get(0).getText());
                        transactionHistory.setExternalTransactionId(getTransactionDetailsResponse.getTransaction().getTransId());
                        transactionHistory.setUpdatedDate(new Date());
                        transactionHistory.setSubscriptionAmount(getTransactionDetailsResponse.getTransaction().getAuthAmount().doubleValue());

                        transactionHistoryService.save(transactionHistory);
                        log.info("Updated transaction history for subscriptionId: {}. Payment status: PAID.", subscriptionId);
                    }
                } else {
                    log.warn("No transaction history found for subscriptionId: {}", subscriptionId);
                }

            } catch (EntityNotFoundException e) {
                log.error("Transaction ID not found for subscriptionId: {}. Error: {}", subscriptionId, e.getMessage());
                throw new RuntimeException(e);
            }

        } else {
            log.warn("Transaction failed. ResultCode: {}", getTransactionDetailsResponse.getMessages().getResultCode().getValue());
        }

        log.info("Payment method run completed.");
    }

    private Date calculateNextCycle(Subscription subscription, XMLGregorianCalendar startDate, Date trialEndDate) {
        Date baseDate = trialEndDate != null ? trialEndDate : Date.from(startDate.toGregorianCalendar().toZonedDateTime().toInstant());
        return subscription.getDuration() == 1
                ? DateUtils.addMonthsToDate(baseDate, 1)
                : DateUtils.addMonthsToDate(baseDate, 12);
    }

    public static XMLGregorianCalendar convertDateToXMLGregorianCalendar(Date date) {
        if (date == null) {
            return null;
        }
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
