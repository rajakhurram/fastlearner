package com.vinncorp.fast_learner.services.payment.additional_service;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionLog;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.request.payment_gateway.subscription.CreateSubsRequest;
import com.vinncorp.fast_learner.request.subscription.*;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.subscription.*;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.date.DateUtils;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class PaymentAdditionalSubscriptionService implements IPaymentAdditionalSubscriptionService {

    private final IPaymentProfileService paymentProfileService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final ITransactionHistoryService transactionHistoryService;
    private final GenericRestClient restClient;

    public PaymentAdditionalSubscriptionService(@Lazy IPaymentProfileService paymentProfileService,
                                                SubscriptionLogRepository subscriptionLogRepo,
                                                ITransactionHistoryService transactionHistoryService, GenericRestClient restClient) {
        this.paymentProfileService = paymentProfileService;
        this.subscriptionLogRepo = subscriptionLogRepo;
        this.transactionHistoryService = transactionHistoryService;
        this.restClient = restClient;
    }

    @Override
    public CreateSubscriptionResponse createCouponBased(Subscription subscription, SubscriptionRequest requestDTO,
                                                        String customerProfileId, String customerPaymentProfileId,
                                                        String email, Long userId, Date trialEndDate,
                                                        TransactionHistory transactionHistory, Coupon coupon)
            throws InternalServerException, BadRequestException {
        log.info("Creating new subscription for user: {}", userId);

        log.info("Configuration initialized");
        CreateSubsRequest createSubsRequest = new CreateSubsRequest();
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");
        // Set up payment schedule
        log.info("Payment schedule set with duration: {} month(s)", subscription.getDuration());

        XMLGregorianCalendar startDate;

        try {
            LocalDateTime currentDate = LocalDateTime.now();

            currentDate = currentDate.plusMonths(coupon.getDurationInMonth());

            // Set startDate from adjusted currentDate
            GregorianCalendar gCal = GregorianCalendar.from(LocalDateTime.now().atZone(ZoneId.systemDefault()));
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);

            // Set trial end date
            Date calculatedTrialEndDate = Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant());
            transactionHistory.setSubscriptionNextCycle(calculatedTrialEndDate);
            transactionHistory.setTrialEndDate(new Date());

            log.info("Subscription trial set. Calculating next billing cycle...");

        } catch (Exception e) {
            log.error("Failed to calculate subscription start date: {}", e.getMessage(), e);
            throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
        }

        //set date & payment occurrence
        createSubsRequest.setStartDate(startDate);
        createSubsRequest.setTotalOccurrences((short) 9999);
        // If current user already had a subscription
        if (coupon != null)
            createSubsRequest.setTrialOccurrences((short) coupon.getDurationInMonth());

        createSubsRequest.setSubscriptionTypeName(subscription.getName());
        createSubsRequest.setAmount(subscription.getPrice());

        log.info("Setting trial amount by calculating the discount percentage of the coupon.");
        if (coupon != null && coupon.getDiscount() != 100)
            createSubsRequest.setTrialAmount(calculateTrialAmount(subscription, coupon));
        else
            createSubsRequest.setTrialAmount(BigDecimal.valueOf(0.0));

        String addressId = null;
        if (Objects.isNull(customerProfileId) && Objects.isNull(customerPaymentProfileId)) {
            log.info("Creating subscription with new profile");
            var resp = paymentProfileService.createCustomerProfile(email, requestDTO);

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
            customerProfileId = resp.getCustomerId();
            customerPaymentProfileId = resp.getPaymentId();
            addressId = resp.getAddressId();
            log.info("Customer Profile ID: {}, Customer Payment Profile ID: {}, Address ID: {}", customerProfileId, customerPaymentProfileId, addressId);
        }

        createSubsRequest.setCustomerProfileId(customerProfileId);
        createSubsRequest.setCustomerPaymentProfileId(customerPaymentProfileId);

        log.info("Customer profile set for subscription");
        log.info("CustomerProfileIdType: Customer ID: {}, Payment ID: {}", customerProfileId, customerPaymentProfileId);

        return getArbCreateSubscriptionResponse(requestDTO, customerProfileId, transactionHistory, createSubsRequest);
    }

    @Override
    public CreateSubscriptionResponse createFirstTime(Subscription subscription, SubscriptionRequest requestDTO,
                                                      String customerProfileId, String customerPaymentProfileId,
                                                      String email, Long userId, Date trialEndDate,
                                                      TransactionHistory transactionHistory, Coupon coupon)
            throws InternalServerException, BadRequestException {
        log.info("Creating new subscription for user: {}", userId);

        CreateSubsRequest createSubsRequest = new CreateSubsRequest();
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");

        log.info("Payment schedule set with duration: {} month(s)", subscription.getDuration());

        XMLGregorianCalendar startDate;

        try {
            LocalDateTime currentDate = LocalDateTime.now();

            boolean isStandardPlan = subscription.getPlanType() == PlanType.STANDARD;
            boolean isFreePlan = subscription.getPlanType() == PlanType.FREE;
            boolean isFullDiscount = coupon != null && coupon.getDiscount() == 100;

            if (isFullDiscount) {
                currentDate = currentDate.plusMonths(coupon.getDurationInMonth());
            } else {
                if (isStandardPlan) {
                    log.info("First-time STANDARD plan subscription in production: adding 2-week trial period");
                    currentDate = currentDate.plusWeeks(2);
                }
            }

            // Set startDate from adjusted currentDate
            GregorianCalendar gCal = GregorianCalendar.from(LocalDateTime.now().atZone(ZoneId.systemDefault()));
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);

            // Set trial end date
            Date calculatedTrialEndDate = Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant());
            transactionHistory.setTrialEndDate(calculatedTrialEndDate);

            log.info("Subscription trial set. Calculating next billing cycle...");

            // Set subscription next cycle
            if (!isFreePlan) {
                Date nextCycle = DateUtils.addMonthsToDate(calculatedTrialEndDate, subscription.getDuration());
                transactionHistory.setSubscriptionNextCycle(nextCycle);
            }

        } catch (Exception e) {
            log.error("Failed to calculate subscription start date: {}", e.getMessage(), e);
            throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
        }
        createSubsRequest.setStartDate(startDate);
        createSubsRequest.setTotalOccurrences((short) 9999);

        // If current user already had a subscription
        if (subscription.getPlanType() == PlanType.STANDARD) {
            log.info("First time subscription, setting trial occurrences to 1");
            createSubsRequest.setTrialOccurrences((short) 1);
        } else {
            log.info("Existing subscription found, setting trial occurrences to 0");
            createSubsRequest.setTrialOccurrences((short) 0);
        }

        if (coupon != null)
            createSubsRequest.setTrialOccurrences((short) coupon.getDurationInMonth());

        createSubsRequest.setSubscriptionTypeName(subscription.getName());
        createSubsRequest.setAmount(subscription.getPrice());

        if (coupon != null && coupon.getDiscount() != 100)
            createSubsRequest.setTrialAmount(calculateTrialAmount(subscription, coupon));
        else
            createSubsRequest.setTrialAmount(BigDecimal.valueOf(0.0));

        String addressId = null;
        if (Objects.isNull(customerProfileId) && Objects.isNull(customerPaymentProfileId)) {
            log.info("Creating subscription with new profile");
            var resp = paymentProfileService.createCustomerProfile(email, requestDTO);

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
            customerProfileId = resp.getCustomerId();
            customerPaymentProfileId = resp.getPaymentId();
            addressId = resp.getAddressId();
            log.info("Customer Profile ID: {}, Customer Payment Profile ID: {}, Address ID: {}", customerProfileId, customerPaymentProfileId, addressId);
        }

        createSubsRequest.setCustomerProfileId(customerProfileId);
        createSubsRequest.setCustomerPaymentProfileId(customerPaymentProfileId);
        log.info("Customer profile set for subscription");
        log.info("CustomerProfileIdType: Customer ID: {}, Payment ID: {}", customerProfileId, customerPaymentProfileId);

        // Make the API Request
        return getArbCreateSubscriptionResponse(requestDTO, customerProfileId, transactionHistory, createSubsRequest);
    }

    @NotNull
    private CreateSubscriptionResponse getArbCreateSubscriptionResponse(SubscriptionRequest requestDTO,
                                                                        String customerProfileId, TransactionHistory transactionHistory, CreateSubsRequest createSubsRequest)
            throws BadRequestException, InternalServerException {

        CreateSubscriptionResponse response = restClient.makeRequest(
                "/api/v1/subscription/create", HttpMethod.POST, createSubsRequest, CreateSubscriptionResponse.class);

        log.info("API response received: {}", response);
        if (response != null) {
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                transactionHistory.setAuthSubscriptionId(response.getSubscriptionId());
                transactionHistory.setCustomerPaymentProfileId(response.getProfile().getCustomerPaymentProfileId());
                Boolean isCardVerify = validateCardVerification(
                        response.getProfile().getCustomerProfileId(),
                        response.getProfile().getCustomerPaymentProfileId()
                );

                if (!isCardVerify) {
                    log.error("Card verification failed: Invalid expiry date or CVV for customerProfileId: {}, paymentProfileId: {}",
                            response.getProfile().getCustomerProfileId(), response.getProfile().getCustomerPaymentProfileId());
                    throw new BadRequestException("Card verification failed: Invalid expiry date or CVV");
                }
            } else {
                transactionHistory.setAuthSubscriptionId("0");
                transactionHistory.setStatus(GenericStatus.INACTIVE);
            }
            transactionHistory.setResponseCode(String.valueOf(response.getMessages().getResultCode()));
            transactionHistory.setResponseText(response.getMessages().getMessage().get(0).getText());

        } else {
            transactionHistory.setAuthSubscriptionId("0");
        }

        transactionHistory = transactionHistoryService.save(transactionHistory);
        if (transactionHistory != null) {
            log.info("Transaction history save");
        } else {
            log.info("Transaction history not save");
        }


        if (Objects.isNull(response)) {
            if (Objects.isNull(requestDTO.getPaymentDetail().getId()))
                paymentProfileService.deleteCustomerProfileById(customerProfileId);
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            if (Objects.isNull(requestDTO.getPaymentDetail().getId()))
                paymentProfileService.deleteCustomerProfileById(customerProfileId);
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new BadRequestException("Payment Subscription: " + response.getMessages().getMessage().get(0).getText());
        }
        log.info("Subscription created successfully with ID: {}", response.getSubscriptionId());
        return response;
    }

    @NotNull
    private static BigDecimal calculateTrialAmount(Subscription subscription, Coupon coupon) {
        double discountedAmount = (coupon.getDiscount() / 100.0) * subscription.getPrice();
        double actualDiscountedAmountAsPerOccurrences = (subscription.getPrice() - discountedAmount);
        return BigDecimal.valueOf(actualDiscountedAmountAsPerOccurrences);
    }

    //This method is used for card cvv and expiry validation
    @Override
    public Boolean validateCardVerification(String customerProfileId, String customerPaymentProfileId) {
        log.info("Starting validation for CustomerProfileId: {}, PaymentProfileId: {}", customerProfileId, customerPaymentProfileId);

        try {
            com.vinncorp.fast_learner.util.Message response = restClient.makeRequest(
                    "/api/v1/subscription/card-verification?customerProfileId=" + customerProfileId + "&customerPaymentProfileId=" + customerPaymentProfileId,
                    HttpMethod.POST, null, com.vinncorp.fast_learner.util.Message.class);

            if (response != null && response.getStatus() == 200) {
                String successMessage = response.getMessage();
                log.info("Validation successful: {}", successMessage);
                return true;
            } else {
                String errorMessage = response != null
                        ? response.getMessage()
                        : "No response from API";
                log.error("Validation failed: {}", errorMessage);
                return false;
            }

        } catch (Exception ex) {
            log.error("Exception occurred during validation: {}", ex.getMessage(), ex);
            return false;
        }
    }


    @Override
    public CreateSubscriptionResponse upgradeFromFreePlan(Subscription subscription, SubscriptionRequest requestDTO,
                                                          String customerProfileId, String customerPaymentProfileId,
                                                          String email, Long userId, Date trialEndDate,
                                                          TransactionHistory transactionHistory, Double balanceAmount)
            throws InternalServerException, BadRequestException {
        log.info("Creating new subscription for user: {}", userId);

        SubscriptionLog subscriptionLog = subscriptionLogRepo.findTopByUserIdOrderByCreatedAtDesc(userId);

        CreateSubsRequest createSubsRequest = new CreateSubsRequest();
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");

        XMLGregorianCalendar startDate;

        //create starting date with 2 weeks trial period
        try {
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            LocalDateTime currentDate = LocalDateTime.now();
            if (subscription.getPlanType() == PlanType.STANDARD && Objects.isNull(subscriptionLog)) {
                log.info("First time subscription taken, including 2 week trial period");
                currentDate = currentDate.plusWeeks(2);
            }
            startDate.setDay(currentDate.getDayOfMonth());
            startDate.setMonth(currentDate.getMonthValue());
            startDate.setYear(currentDate.getYear());
            transactionHistory.setTrialEndDate(Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant()));

            log.info("Subscription trial will be zero because user already has a subscription");

            Date nextCycle = new Date();
            if (subscription.getDuration() == 1) {
                nextCycle = DateUtils.addMonthsToDate(trialEndDate == null ? Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant()) : trialEndDate, 1);
            } else if (subscription.getDuration() == 12) {
                if (trialEndDate != null) {
                    nextCycle = DateUtils.addMonthsToDate(trialEndDate, 12);
                }
            }
            transactionHistory.setSubscriptionNextCycle(nextCycle);

        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
            throw new InternalServerException("ERROR" + e.getLocalizedMessage());
        }
        createSubsRequest.setStartDate(startDate);
        createSubsRequest.setTotalOccurrences((short) 9999);
        //set date & payment occurrence

        // If current user already had a subscription
        if (subscription.getPlanType() == PlanType.STANDARD && Objects.isNull(subscriptionLog)) {
            log.info("First time subscription, setting trial occurrences to 1");
            createSubsRequest.setTrialOccurrences((short) 1);
        } else {
            log.info("Existing subscription found, setting trial occurrences to 0");
            createSubsRequest.setTrialOccurrences((short) 0);
        }


        SubscriptionTypeReq subscriptionType = new SubscriptionTypeReq();

        createSubsRequest.setSubscriptionTypeName(subscription.getName());

        createSubsRequest.setTrialAmount(BigDecimal.valueOf(0.0));
        log.info("Subscription type set: {}", subscriptionType);

        String addressId = null;
        if (Objects.isNull(customerProfileId) && Objects.isNull(customerPaymentProfileId)) {
            log.info("Creating subscription with new profile");
            var resp = paymentProfileService.createCustomerProfile(email, requestDTO);

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
            customerProfileId = resp.getCustomerId();
            customerPaymentProfileId = resp.getPaymentId();
            addressId = resp.getAddressId();
            log.info("Customer Profile ID: {}, Customer Payment Profile ID: {}, Address ID: {}", customerProfileId, customerPaymentProfileId, addressId);
        }
        createSubsRequest.setCustomerProfileId(customerProfileId);
        createSubsRequest.setCustomerPaymentProfileId(customerPaymentProfileId);

        log.info("Customer profile set for subscription");
        log.info("CustomerProfileIdType: Customer ID: {}, Payment ID: {}", customerProfileId, customerPaymentProfileId);

        // Make the API Request
        return getArbCreateSubscriptionResponse(requestDTO, customerProfileId, transactionHistory, createSubsRequest);
    }

    @Override
    public void cancelPaymentSubscription(String subscriptionId) throws InternalServerException {
        log.info("Cancel existing subscription");

        CancelSubscriptionResponse response = restClient.makeRequest(
                "/api/v1/subscription/cancel?subscriptionId=" + subscriptionId, HttpMethod.DELETE,
                null, CancelSubscriptionResponse.class);

        if (Objects.isNull(response)) {
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Payment Subscription: " + response.getMessages().getMessage().get(0).getText());
        }
    }

    @Override
    public List<TransactionSummaryType> getTransactionPageListByCustomerProfileId(String customerProfileId, String customerPaymentProfileId, int pageNo, int pageSize) throws InternalServerException, EntityNotFoundException {
        log.info("Fetch all the transactions by profile id " + customerProfileId);

        GetTransactionListResponse getResponse = restClient.makeRequest(
                "/api/v1/transaction/detail/list?customerProfileId=" +
                        customerProfileId + "&customerPaymentProfileId=" +
                        customerPaymentProfileId, HttpMethod.GET, null, GetTransactionListResponse.class);


        if (Objects.isNull(getResponse)) {
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (getResponse.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + getResponse.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Payment Subscription: " + getResponse.getMessages().getMessage().get(0).getText());
        } else if (Objects.isNull(getResponse.getTransactions())) {
            log.error("ERROR: " + getResponse.getMessages().getMessage().get(0).getText());
            throw new EntityNotFoundException("No Transaction found");
        }

        return getResponse.getTransactions().getTransaction();

    }

    @Override
    public GetSubscriptionResponse getSubscriptionById(String subscriptionId) throws InternalServerException {
        log.info("Get subscription by subscription id " + subscriptionId);

        GetSubscriptionResponse response = restClient.makeRequest(
                "/api/v1/subscription/get?subscriptionId=" + subscriptionId, HttpMethod.GET, null, GetSubscriptionResponse.class);

        if (Objects.isNull(response)) {
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Payment Subscription: " + response.getMessages().getMessage().get(0).getText());
        }

        return response;
    }

    @Override
    public CustomerAndPaymentId updatePaymentSubscription(String email, String subscriptionId, String customerProfileId, String customerPaymentProfileId) throws InternalServerException {
        log.info("Updating existing subscription: " + subscriptionId + " for " + email);

        UpdateSubscriptionRequest apiRequest = new UpdateSubscriptionRequest();
        apiRequest.setSubscriptionId(subscriptionId);
        apiRequest.setCustomerProfileId(customerProfileId);
        apiRequest.setCustomerPaymentProfileId(customerPaymentProfileId);

        UpdateSubscriptionResponse response = restClient.makeRequest(
                "/api/v1/subscription/update", HttpMethod.POST, apiRequest, UpdateSubscriptionResponse.class);

        if (response != null) {
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                return CustomerAndPaymentId.builder()
                        .customerId(customerProfileId)
                        .paymentId(customerPaymentProfileId)
                        .addressId(null)
                        .build();
            } else {
                throw new InternalServerException("Failed to update Subscription:  " + response.getMessages().getMessage());
            }
        }
        throw new InternalServerException("Payment api error or the server is down.");
    }
}
