package com.vinncorp.fast_learner.services.payment.payment_profile;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.request.customer_profile.CreateCustProfileReq;
import com.vinncorp.fast_learner.request.payment_gateway.customer.CreateCustPaymentProfileRequest;
import com.vinncorp.fast_learner.request.payment_gateway.customer.DeleteCustPaymentProfileRequest;
import com.vinncorp.fast_learner.request.payment_gateway.customer.UpdateCustPaymentProfileReq;
import com.vinncorp.fast_learner.response.customer_profile.DeleteCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.response.customer_profile.UpdateCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.response.customer_profile.*;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.subscription.CreateCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.util.Constants.LogMessage;
import com.vinncorp.fast_learner.response.subscription.GetCustomerPaymentProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.vinncorp.fast_learner.response.subscription.GetTransactionDetailsResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProfileService implements IPaymentProfileService {

    private final GenericRestClient restClient;

    @Override
    public CreateCustomerPaymentProfileResponse createCustomerPaymentProfile(String customerProfileId, PaymentProfileDetailRequest detail, String email) throws InternalServerException {
        log.info("Creating the payment customer profile with id " + customerProfileId);

        CreateCustPaymentProfileRequest createCustProfileRequest = new CreateCustPaymentProfileRequest();
        createCustProfileRequest.setCustomerProfileId(customerProfileId);
        createCustProfileRequest.setEmail(email);
        createCustProfileRequest.setFirstName(detail.getFirstName());
        createCustProfileRequest.setLastName(detail.getLastName());
        createCustProfileRequest.setCardNo(detail.getCardNumber());
        createCustProfileRequest.setCardExpiry(detail.getExpiryMonth() + detail.getExpiryYear());
        createCustProfileRequest.setCvv(detail.getCvv());


        CreateCustomerPaymentProfileResponse response = restClient.makeRequest(
                "/api/v1/customer-payment-profile/save", HttpMethod.POST, createCustProfileRequest, CreateCustomerPaymentProfileResponse.class);

        if (Objects.isNull(response)) {
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            if (!Objects.equals(response.getMessages().getMessage().get(0).getCode(), LogMessage.DUPLICATE_PROFILE_CODE)) {
                log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
                throw new InternalServerException("Payment Subscription: " + response.getMessages().getMessage().get(0).getText());
            }
        }
        return response;
    }

    @Override
    public void updateCustomerPaymentProfile(String customerProfileId, String customerPaymentProfileId, PaymentProfileDetailRequest detail, String email) throws InternalServerException {
        log.info("Updating the payment customer payment profile with id  " + customerPaymentProfileId);

        GetCustomerPaymentProfileResponse customerPaymentProfileResponse = getCustomerPaymentProfile(customerProfileId, customerPaymentProfileId);

        UpdateCustPaymentProfileReq updateCustomerProfileReq = new UpdateCustPaymentProfileReq();
        updateCustomerProfileReq.setEmail(email);
        updateCustomerProfileReq.setFirstName(detail.getFirstName());
        updateCustomerProfileReq.setLastName(detail.getLastName());
        updateCustomerProfileReq.setCardNumber(detail.getCardNumber());
        updateCustomerProfileReq.setExpiryDate(convertDateToPaymentDateFormat(detail.getExpiryMonth() + "/" + detail.getExpiryYear()));
        updateCustomerProfileReq.setDefaultPaymentProfile(false);
        updateCustomerProfileReq.setCustomerPaymentProfileId(customerPaymentProfileId);
        updateCustomerProfileReq.setCustomerProfileId(customerProfileId);
        updateCustomerProfileReq.setCvv(detail.getCvv());

        UpdateCustomerPaymentProfileResponse response = restClient.makeRequest(
                "/api/v1/customer-payment-profile/update", HttpMethod.PUT, updateCustomerProfileReq, UpdateCustomerPaymentProfileResponse.class);


        if (Objects.isNull(response)) {
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Payment Subscription: " + response.getMessages().getMessage().get(0).getText());
        }
    }

    @Override
    public GetCustomerPaymentProfileResponse getCustomerPaymentProfile(String customerProfileId, String customerPaymentProfileId) throws InternalServerException {
        log.info("Fetching the customer payment profile from payment with id " + customerPaymentProfileId);

        GetCustomerPaymentProfileResponse response = restClient.makeRequest(
                "/api/v1/customer-payment-profile/get?customerProfileId=" + customerProfileId + "&customerPaymentProfileId=" + customerPaymentProfileId,
                HttpMethod.GET, null, GetCustomerPaymentProfileResponse.class
        );

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
    public GetCustomerProfileResponse getCustomerPaymentProfileList(String customerProfileId) throws InternalServerException {
        log.info("Fetching the customer payment profile list from payment with id " + customerProfileId);

        GetCustomerProfileResponse response = restClient.makeRequest(
                "/api/v1/customer-profile/get/list?customerProfileId=" + customerProfileId, HttpMethod.GET, null, GetCustomerProfileResponse.class);

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
    public void deleteCustomerPaymentProfile(String customerProfileId, String customerPaymentProfileId, SubscribedUser subscribedUser) throws InternalServerException {
        log.info("Deleting the customer payment profile list from payment with id " + customerPaymentProfileId);

        DeleteCustPaymentProfileRequest deleteCustPaymentProfileRequest = new DeleteCustPaymentProfileRequest();
        deleteCustPaymentProfileRequest.setCustomerProfileId(customerProfileId);
        deleteCustPaymentProfileRequest.setCustomerPaymentProfileId(customerPaymentProfileId);

        DeleteCustomerPaymentProfileResponse response = restClient.makeRequest(
                "/api/v1/customer-payment-profile/delete?customerPaymentProfileId=" + customerPaymentProfileId + "&customerProfileId=" + customerProfileId, HttpMethod.DELETE, null, DeleteCustomerPaymentProfileResponse.class);

        if (Objects.isNull(response)) {
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + response.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Payment Subscription: " + response.getMessages().getMessage().get(0).getText());
        }
    }

    @Override
    public GetTransactionDetailsResponse getTransactionDetail(String transactionId) throws InternalServerException {
        log.info("Fetching the transaction detail from payment api with id " + transactionId);

        GetTransactionDetailsResponse getResponse = restClient.makeRequest(
                "/api/v1/transaction/detail?transactionId=" + transactionId, HttpMethod.GET, null, GetTransactionDetailsResponse.class);

        if (Objects.isNull(getResponse)) {
            log.error("ERROR: Connection not established with Payment api");
            throw new InternalServerException("Something went wrong with Payment api");
        } else if (getResponse.getMessages().getResultCode() != MessageTypeEnum.OK) {
            log.error("ERROR: " + getResponse.getMessages().getMessage().get(0).getText());
            throw new InternalServerException("Payment Subscription: " + getResponse.getMessages().getMessage().get(0).getText());
        }

        return getResponse;
    }

    @Override
    public GetSubscriptionResponse getSubscription(String subscriptionId) throws InternalServerException {
        log.info("Fetching the subscription detail from payment api with id " + subscriptionId);

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

    private static String convertDateToPaymentDateFormat(String inputDate) {
        LocalDate date = LocalDate.parse(inputDate + "/01", DateTimeFormatter.ofPattern("MM/yy/dd"));
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    @Override
    public CustomerAndPaymentId createCustomerProfile(String email, SubscriptionRequest requestDTO) throws InternalServerException {
        log.info("Creating customer profile.");

        CreateCustProfileReq createCustProfileReq = new CreateCustProfileReq();
        createCustProfileReq.setEmail(email);
        createCustProfileReq.setCvv(requestDTO.getPaymentDetail().getCvv());
        createCustProfileReq.setCardNo(requestDTO.getPaymentDetail().getCardNumber());
        createCustProfileReq.setCardExpiry(requestDTO.getPaymentDetail().getExpiryMonth() +
                requestDTO.getPaymentDetail().getExpiryYear());
        createCustProfileReq.setFirstname(requestDTO.getPaymentDetail().getFirstName());
        createCustProfileReq.setLastname(requestDTO.getPaymentDetail().getLastName());

        CreateCustomerProfileResponse response = restClient.makeRequest(
                "/api/v1/customer-profile/create", HttpMethod.POST, createCustProfileReq, CreateCustomerProfileResponse.class);

        // Parse the response to determine results
        if (response != null) {
            // If API Response is OK, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                if (!response.getCustomerPaymentProfileIdList().getNumericString().isEmpty()) {
                    return CustomerAndPaymentId.builder()
                            .customerId(response.getCustomerProfileId())
                            .paymentId(response.getCustomerPaymentProfileIdList().getNumericString().get(0))
                            .addressId(response.getCustomerShippingAddressIdList().getNumericString().isEmpty() ? null :
                                    response.getCustomerShippingAddressIdList().getNumericString().get(0))
                            .build();
                }
            } else {
                throw new InternalServerException("Failed to create customer profile:  " + response.getMessages().getMessage().get(0).getText());
            }
        } else {
            log.info("Failed to get response");
            throw new InternalServerException("Error while creating customer profile.");
        }
        throw new InternalServerException("Something went wrong please try again later.");
    }

    @Override
    public void deleteCustomerProfileById(String profileId) throws BadRequestException {
        log.info("Deleting a customer profile id from payment server...");

        try {
            DeleteCustomerProfileResponse response = restClient.makeRequest(
                    "/api/v1/customer-profile/delete?customerProfileId=" + profileId, HttpMethod.DELETE,
                    null, DeleteCustomerProfileResponse.class);

            if (response != null) {
                if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                    log.info("Successfully deleted customer with ID: " + profileId);
                    return;
                } else {
                    log.warn("Customer with ID: " + profileId + "doesn't deleted from payment server.");
                    throw new InternalServerException("Failed to delete customer profile:  " + response.getMessages().getResultCode());
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("ERROR: " + e.getLocalizedMessage());
        }
    }
}
