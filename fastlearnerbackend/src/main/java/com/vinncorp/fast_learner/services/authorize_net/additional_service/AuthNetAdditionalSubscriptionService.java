package com.vinncorp.fast_learner.services.authorize_net.additional_service;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.authorize_net.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.authorize_net.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionLog;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionValidationsRepository;
import com.vinncorp.fast_learner.request.payment_gateway.subscription.CreateSubsRequest;
import com.vinncorp.fast_learner.request.subscription.*;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.subscription.*;
import com.vinncorp.fast_learner.services.authorize_net.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.date.DateUtils;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionValidation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
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
public class AuthNetAdditionalSubscriptionService implements IAuthNetAdditionalSubscriptionService {

    private final IPaymentProfileService paymentProfileService;
    private final SubscriptionLogRepository subscriptionLogRepo;
    private final ITransactionHistoryService transactionHistoryService;
    private final GenericRestClient restClient;
    private final SubscriptionValidationsRepository subscriptionValidationsRepository;

    public AuthNetAdditionalSubscriptionService(@Lazy IPaymentProfileService paymentProfileService,
                                                SubscriptionLogRepository subscriptionLogRepo,
                                                ITransactionHistoryService transactionHistoryService, GenericRestClient restClient,
                                                SubscriptionValidationsRepository subscriptionValidationsRepository) {
        this.paymentProfileService = paymentProfileService;
        this.subscriptionLogRepo = subscriptionLogRepo;
        this.transactionHistoryService = transactionHistoryService;
        this.restClient = restClient;
        this.subscriptionValidationsRepository = subscriptionValidationsRepository;
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
        createSubsRequest.setFirstName(requestDTO.getPaymentDetail().getFirstName());
        createSubsRequest.setLastName(requestDTO.getPaymentDetail().getLastName());
        createSubsRequest.setZipCode(requestDTO.getPaymentDetail().getZipCode());
        createSubsRequest.setCountryCode(requestDTO.getPaymentDetail().getCountryCode());
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
            transactionHistory.setTrialEndDate(Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant()));

            Date nextCycle = DateUtils.addMonthsToDate(Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant()), subscription.getDuration());
            transactionHistory.setSubscriptionNextCycle(nextCycle);

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
        createSubsRequest.setAmount(BigDecimal.valueOf(subscription.getPrice()));

        SubscriptionValidations subscriptionValidation=subscriptionValidationsRepository
                .findByValidationNameAndEmailAndIsActive(SubscriptionValidation.PROD_SUBSCRIPTION_TEST.name(),email,true);
        BigDecimal amount =null;
        if(subscriptionValidation!=null){
            amount= BigDecimal.valueOf(subscription.getId());
        }
        createSubsRequest.setAmount(amount!=null?amount:BigDecimal.valueOf(subscription.getPrice()));

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
        createSubsRequest.setFirstName(requestDTO.getPaymentDetail().getFirstName());
        createSubsRequest.setLastName(requestDTO.getPaymentDetail().getLastName());
        createSubsRequest.setZipCode(requestDTO.getPaymentDetail().getZipCode());
        createSubsRequest.setCountryCode(requestDTO.getPaymentDetail().getCountryCode());
        createSubsRequest.setLength((short) subscription.getDuration());
        createSubsRequest.setUnit("months");

        log.info("Payment schedule set with duration: {} month(s)", subscription.getDuration());

        XMLGregorianCalendar startDate;

        try {
            LocalDateTime currentDate = LocalDateTime.now();

            boolean isFullDiscount = coupon != null && coupon.getDiscount() == 100;

            if (isFullDiscount) {
                currentDate = currentDate.plusMonths(coupon.getDurationInMonth());
            }

            // Set startDate from adjusted currentDate
            GregorianCalendar gCal = GregorianCalendar.from(LocalDateTime.now().atZone(ZoneId.systemDefault()));
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);

            // Set trial end date
            Date calculatedTrialEndDate = Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant());
            transactionHistory.setTrialEndDate(calculatedTrialEndDate);

            log.info("Subscription trial set. Calculating next billing cycle...");

            Date nextCycle = DateUtils.addMonthsToDate(calculatedTrialEndDate, subscription.getDuration());
            transactionHistory.setSubscriptionNextCycle(nextCycle);

        } catch (Exception e) {
            log.error("Failed to calculate subscription start date: {}", e.getMessage(), e);
            throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
        }
        createSubsRequest.setStartDate(startDate);
        createSubsRequest.setTotalOccurrences((short) 9999);
        createSubsRequest.setTrialOccurrences((short) 0);

        if (coupon != null)
            createSubsRequest.setTrialOccurrences((short) coupon.getDurationInMonth());

        createSubsRequest.setSubscriptionTypeName(subscription.getName());
        createSubsRequest.setAmount(BigDecimal.valueOf(subscription.getPrice()));

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
                "/v1/subscription/create", HttpMethod.POST, createSubsRequest, CreateSubscriptionResponse.class);

        log.info("API response received: {}", response);
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
            log.error("ERROR: Connection not established with authorize.net api");
            throw new InternalServerException("Something went wrong with Authorize.Net api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            if (Objects.isNull(requestDTO.getPaymentDetail().getId()))
                paymentProfileService.deleteCustomerProfileById(customerProfileId);
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new BadRequestException("Authorize.Net Subscription: " + response.getMessages().getMessage().get(0).getText());
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

    @Override
    public void cancelAuthorizeNetSubscription(String subscriptionId) throws InternalServerException {
        log.info("Cancel existing subscription");

        CancelSubscriptionResponse response = restClient.makeRequest(
                "/v1/subscription/cancel?subscriptionId=" + subscriptionId, HttpMethod.DELETE,
                null, CancelSubscriptionResponse.class);

        if (Objects.isNull(response)) {
            log.error("ERROR: Connection not established with authorize.net api");
            throw new InternalServerException("Something went wrong with Authorize.Net api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Authorize.Net Subscription: " + response.getMessages().getMessage().get(0).getText());
        }
    }

    @Override
    public GetSubscriptionResponse getSubscriptionById(String subscriptionId) throws InternalServerException {
        log.info("Get subscription by subscription id " + subscriptionId);

        GetSubscriptionResponse response = restClient.makeRequest(
                "/v1/subscription/get?subscriptionId=" + subscriptionId, HttpMethod.GET, null, GetSubscriptionResponse.class);

        if (Objects.isNull(response)) {
            log.error("ERROR: Connection not established with authorize.net api");
            throw new InternalServerException("Something went wrong with Authorize.Net api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Authorize.Net Subscription: " + response.getMessages().getMessage().get(0).getText());
        }

        return response;
    }

    @Override
    public CustomerAndPaymentId updateAuthorizeNetSubscription(String email, String subscriptionId, String customerProfileId, String customerPaymentProfileId) throws InternalServerException {
        log.info("Updating existing subscription: " + subscriptionId + " for " + email);

        UpdateSubscriptionRequest apiRequest = new UpdateSubscriptionRequest();
        apiRequest.setSubscriptionId(subscriptionId);
        apiRequest.setCustomerProfileId(customerProfileId);
        apiRequest.setCustomerPaymentProfileId(customerPaymentProfileId);

        UpdateSubscriptionResponse response = restClient.makeRequest(
                "/v1/subscription/update", HttpMethod.POST, apiRequest, UpdateSubscriptionResponse.class);

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
        throw new InternalServerException("Authorize net api error or the server is down.");
    }
}
