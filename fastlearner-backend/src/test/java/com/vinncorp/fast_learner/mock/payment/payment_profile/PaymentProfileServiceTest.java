package com.vinncorp.fast_learner.mock.payment.payment_profile;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.mock.payment.additional_service.PaymentAdditionalSubscriptionTestData;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.response.customer_profile.CreateCustomerProfileResponse;
import com.vinncorp.fast_learner.response.customer_profile.DeleteCustomerProfileResponse;
import com.vinncorp.fast_learner.response.customer_profile.GetCustomerProfileResponse;
import com.vinncorp.fast_learner.response.customer_profile.UpdateCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.message.MessagesType;
import com.vinncorp.fast_learner.response.subscription.*;
import com.vinncorp.fast_learner.services.payment.payment_profile.PaymentProfileService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.response.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class PaymentProfileServiceTest {

    @Mock
    private SubscribedUserService subscribedUserService;

    @Mock
    private GenericRestClient restClient;

    @InjectMocks
    private PaymentProfileService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Create payment customer profile when provided invalid data")
    void createPaymentCustomerProfile_Error_Response_Null() throws InternalServerException {
        String customerProfileId = "12345";
        PaymentProfileDetailRequest detail = PaymentProfileTestData.paymentProfileDetailRequest();
        String email = "john.doe@example.com";

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new InternalServerException("Something went wrong with Payment api"));


        assertThrows(InternalServerException.class, () -> {
            service.createCustomerPaymentProfile(customerProfileId, detail, email);
        });
    }

    @Test
    @DisplayName("Should create Payment customer profile successfully")
    void testCreateCustomerProfile_Success() throws InternalServerException {
        var request = PaymentProfileTestData.paymentProfileDetailRequest();

        var response = new CreateCustomerPaymentProfileResponse();
        var messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any())).thenReturn(response);

        var result = service.createCustomerPaymentProfile("123", request, "john@example.com");

        assertNotNull(result);
        assertEquals(MessageTypeEnum.OK, result.getMessages().getResultCode());
    }

    @Test
    @DisplayName("Should throw exception when response is null")
    void testPaymentCreateCustomerProfile_NullResponse() throws InternalServerException {
        when(restClient.makeRequest(any(), any(), any(), any())).thenReturn(null);

        var ex = assertThrows(InternalServerException.class, () ->
                service.createCustomerPaymentProfile("123", new PaymentProfileDetailRequest(), "john@example.com"));

        assertEquals("Something went wrong with Payment api", ex.getMessage());
    }

    @Test
    @DisplayName("Should not throw if duplicate profile code returned")
    void testPaymentCreateCustomerProfile_DuplicateCode() throws InternalServerException {
        var response = new CreateCustomerPaymentProfileResponse();

        Message msg = new Message(); // Not MessagesType.Message
        msg.setCode("E00039");
        msg.setText("Duplicate profile");

        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.ERROR);
        messages.setMessage(List.of(msg));

        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any())).thenReturn(response);

        var result = service.createCustomerPaymentProfile("123", new PaymentProfileDetailRequest(), "john@example.com");

        assertNotNull(result);
        assertEquals("E00039", result.getMessages().getMessage().get(0).getCode());
    }


    @Test
    @DisplayName("Should not throw if duplicate profile code returned")
    void testCreateCustomerProfile_DuplicateCode() throws InternalServerException {
        var response = new CreateCustomerPaymentProfileResponse();
        Message msg = new Message();
        msg.setCode("E00039");
        msg.setText("Duplicate profile");

        var messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.ERROR);
        messages.setMessage(List.of(msg));
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any())).thenReturn(response);

        var result = service.createCustomerPaymentProfile("123", new PaymentProfileDetailRequest(), "john@example.com");

        assertNotNull(result);
        assertEquals("E00039", result.getMessages().getMessage().get(0).getCode());
    }

    @Test
    @DisplayName("Should create Payment customer profile successfully")
    void testCreatePaymentCustomerProfile_Success() throws InternalServerException {
        PaymentProfileDetailRequest request = PaymentProfileTestData.paymentProfileDetailRequest();

        CreateCustomerPaymentProfileResponse response = new CreateCustomerPaymentProfileResponse();
        Message message = new Message();
        message.setCode("I00001"); // any non-error code
        message.setText("Successful");

        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        messages.setMessage(List.of(message));

        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenReturn(response);

        CreateCustomerPaymentProfileResponse result = service.createCustomerPaymentProfile("123", request, "john@example.com");

        assertNotNull(result);
        assertEquals(MessageTypeEnum.OK, result.getMessages().getResultCode());
        assertEquals("I00001", result.getMessages().getMessage().get(0).getCode());
        assertEquals("Successful", result.getMessages().getMessage().get(0).getText());
    }

    @Test
    @DisplayName("Should throw InternalServerException when Payment API connection fails")
    void testUpdatePaymentCustomerProfile_NullResponse() throws InternalServerException {
        // Execute and assert
        PaymentProfileDetailRequest detail = new PaymentProfileDetailRequest();
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.updateCustomerPaymentProfile("profileId", "paymentProfileId", detail, "john.doe@example.com");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException for error response from Payment API")
    void testUpdatePaymentCustomerProfile_ErrorResponse() throws InternalServerException {
        // Mocking the request and response objects

        // Execute and assert
        PaymentProfileDetailRequest detail = new PaymentProfileDetailRequest();
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.updateCustomerPaymentProfile("profileId", "paymentProfileId", detail, "john.doe@example.com");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testUpdatePaymentCustomerProfile_ExceptionDuringExecution() throws InternalServerException {

        // Execute and assert
        PaymentProfileDetailRequest detail = new PaymentProfileDetailRequest();
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.updateCustomerPaymentProfile("profileId", "paymentProfileId", detail, "john.doe@example.com");
        });
        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }


    @Test
    @DisplayName("Should throw InternalServerException for error response from Payment API")
    void testUpdatePaymentCustomerProfile_ErrorFromAPI() throws InternalServerException {
        PaymentProfileDetailRequest detail = new PaymentProfileDetailRequest();

        UpdateCustomerPaymentProfileResponse response = new UpdateCustomerPaymentProfileResponse();
        Message msg = new Message();
        msg.setCode("E00027");
        msg.setText("Invalid card number");

        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.ERROR);
        messages.setMessage(List.of(msg));
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), eq(UpdateCustomerPaymentProfileResponse.class)))
                .thenReturn(response);

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.updateCustomerPaymentProfile("profileId", "paymentProfileId", detail, "john.doe@example.com");
        });

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when PaymentProfileDetailRequest is null")
    void testUpdatePaymentCustomerProfile_NullDetail() {
        assertThrows(InternalServerException.class, () -> {
            service.updateCustomerPaymentProfile("profileId", "paymentProfileId", null, "john.doe@example.com");
        });
    }

    @Test
    @DisplayName("Should throw InternalServerException when Payment API returns error result")
    void testUpdateAPaymentCustomerProfile_PaymentErrorResult() throws InternalServerException {
        PaymentProfileDetailRequest detail = new PaymentProfileDetailRequest();
        detail.setFirstName("Jane");
        detail.setLastName("Doe");
        detail.setCardNumber("4111111111111111");
        detail.setExpiryMonth("01");
        detail.setExpiryYear("2030");
        detail.setCvv("321");

        UpdateCustomerPaymentProfileResponse response = new UpdateCustomerPaymentProfileResponse();
        Message msg = new Message();
        msg.setCode("I00001");
        msg.setText("Successful");

        var messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.ERROR);
        messages.setMessage(List.of(msg));
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), eq(UpdateCustomerPaymentProfileResponse.class)))
                .thenReturn(response);

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                service.updateCustomerPaymentProfile("profileId", "paymentProfileId", detail, "jane.doe@example.com")
        );

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when Payment API connection fails")
    void testGetPaymentCustomerPaymentProfile_NullResponse() throws InternalServerException {

        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfile("profileId", "paymentProfileId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException for error response from Payment API")
    void testGetPaymentCustomerPaymentProfile_ErrorResponse() throws InternalServerException {
        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfile("profileId", "paymentProfileId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testGetPaymentCustomerPaymentProfile_ExceptionDuringExecution() throws InternalServerException {

        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfile("profileId", "paymentProfileId");
        });
        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when Payment API connection fails")
    void testGetPaymentCustomerPaymentProfileList_NullResponse() throws InternalServerException {
        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfileList("profileId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully return customer payment profile when response is OK")
    void testGetPaymentCustomerPaymentProfile_SuccessfulResponse() throws InternalServerException {
        // Arrange
        Message message = new Message();
        message.setCode("I00001");
        message.setText("Successful");

        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        messages.setMessage(List.of(message));

        GetCustomerPaymentProfileResponse mockResponse = new GetCustomerPaymentProfileResponse();
        mockResponse.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenReturn(mockResponse);

        // Act
        GetCustomerPaymentProfileResponse response = service.getCustomerPaymentProfile("profileId", "paymentProfileId");

        // Assert
        assertNotNull(response);
        assertEquals(MessageTypeEnum.OK, response.getMessages().getResultCode());
        assertEquals("Successful", response.getMessages().getMessage().get(0).getText());
    }

    @Test
    @DisplayName("Should throw InternalServerException for error response from Payment API")
    void testGetPaymentCustomerPaymentProfileList_ErrorResponse() throws InternalServerException {
        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfileList("profileId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testGetPaymentCustomerPaymentProfileList_ExceptionDuringExecution() throws InternalServerException {
        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfileList("profileId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when Payment returns error result code")
    void testGetPaymentCustomerPaymentProfile_ErrorResponseCode() throws InternalServerException {
        // Arrange
        Message message = new Message();
        message.setCode("E00027");
        message.setText("Invalid profile ID");

        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.ERROR);
        messages.setMessage(List.of(message));

        GetCustomerPaymentProfileResponse mockResponse = new GetCustomerPaymentProfileResponse();
        mockResponse.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenReturn(mockResponse);

        // Act & Assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfile("profileId", "paymentProfileId");
        });

        assertEquals("Payment Subscription: Invalid profile ID", exception.getMessage());
    }


    @Test
    @DisplayName("Should throw InternalServerException when exception is thrown during request execution")
    void testGetPaymentCustomerPaymentProfile_ExceptionThrown() throws InternalServerException {
        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new InternalServerException("Something went wrong with Payment api"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfile("profileId", "paymentProfileId");
        });

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }


    @Test
    @DisplayName("Should throw InternalServerException when Payment API connection fails")
    void testDeletePaymentCustomerPaymentProfile_NullResponse() throws InternalServerException {
        SubscribedUser subscribedUser = new SubscribedUser();
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.deleteCustomerPaymentProfile("profileId", "paymentProfileId", subscribedUser);
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException for error response from Payment API")
    void testDeletePaymentCustomerPaymentProfile_ErrorResponse() throws InternalServerException {
        // Execute and assert
        SubscribedUser subscribedUser = new SubscribedUser();
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.deleteCustomerPaymentProfile("profileId", "paymentProfileId", subscribedUser);
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testDeletePaymentCustomerPaymentProfile_ExceptionDuringExecution() throws InternalServerException {
        // Execute and assert
        SubscribedUser subscribedUser = new SubscribedUser();
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.deleteCustomerPaymentProfile("profileId", "paymentProfileId", subscribedUser);
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when API connection fails")
    void testGetTransactionDetail_NullResponse() throws InternalServerException {
        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getTransactionDetail("transactionId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException for error response from API")
    void testGetTransactionDetail_ErrorResponse() throws InternalServerException {
        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getTransactionDetail("transactionId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testGetTransactionDetail_ExceptionDuringExecution() throws InternalServerException {
        // Execute and assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getTransactionDetail("transactionId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when API connection fails")
    void testGetSubscription_NullResponse() throws InternalServerException {
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getSubscription("subscriptionId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException for error response from API")
    void testGetSubscription_ErrorResponse() throws InternalServerException {
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getSubscription("subscriptionId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testGetSubscription_ExceptionDuringExecution() throws InternalServerException {
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.getSubscription("subscriptionId");
        });

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException(new InternalServerException("Something went wrong with Payment api")));

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when API returns error")
    void testCreateCustomerProfile_ErrorResponse() throws InternalServerException {
        SubscriptionRequest requestDTO = PaymentAdditionalSubscriptionTestData.subscriptionRequest();

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new InternalServerException("Failed to create customer profile:  ERROR"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.createCustomerProfile("test@example.com", requestDTO);
        });


        assertEquals("Failed to create customer profile:  ERROR", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when API response is null")
    void testCreateCustomerProfile_NullResponse() throws InternalServerException {
        SubscriptionRequest requestDTO = PaymentAdditionalSubscriptionTestData.subscriptionRequest();

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new InternalServerException("Failed to create customer profile:  ERROR"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.createCustomerProfile("test@example.com", requestDTO);
        });

        assertEquals("Failed to create customer profile:  ERROR", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testCreateCustomerProfile_ExceptionDuringExecution() throws InternalServerException {
        SubscriptionRequest requestDTO = PaymentAdditionalSubscriptionTestData.subscriptionRequest();

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new InternalServerException("Failed to create customer profile:  ERROR"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.createCustomerProfile("test@example.com", requestDTO);
        });

        assertEquals("Failed to create customer profile:  ERROR", exception.getMessage());
    }

    @Test
    @DisplayName("Should log a warning when the customer profile is not deleted")
    void testDeleteCustomerProfileById_NotDeleted() throws InternalServerException, BadRequestException {
        service.deleteCustomerProfileById("12345");
    }

    @Test
    @DisplayName("Should do nothing if the API response is null")
    void testDeleteCustomerProfileById_NullResponse() throws InternalServerException, BadRequestException {
        service.deleteCustomerProfileById("12345");
    }

    @Test
    @DisplayName("Should handle exceptions thrown during API execution")
    void testDeleteCustomerProfileById_ExceptionDuringExecution() {
        assertDoesNotThrow(() -> service.deleteCustomerProfileById("12345"));
    }

    @Test
    @DisplayName("Should reject invalid CVV format")
    void testCreateCustomerProfile_InvalidCVV() throws InternalServerException {
        PaymentProfileDetailRequest detail = new PaymentProfileDetailRequest();
        detail.setCvv("12");

        when(restClient.makeRequest(any(), any(), any(), any()))
                .thenThrow(new InternalServerException("Invalid CVV format"));

        assertThrows(InternalServerException.class, () -> {
            service.createCustomerPaymentProfile("profileId", detail, "email@example.com");
        });
    }

    @Test
    @DisplayName("Should return null when customer profile response is OK but no payment profiles")
    void testCreateCustomerProfile_NonEmptyPaymentList() throws InternalServerException {
        SubscriptionRequest requestDTO = PaymentAdditionalSubscriptionTestData.subscriptionRequest();

        CreateCustomerProfileResponse response = new CreateCustomerProfileResponse();
        response.setCustomerProfileId("cust123");

        ArrayOfNumericString emptyList = new ArrayOfNumericString();
        emptyList.setNumericString(List.of("38923343"));  // empty list
        response.setCustomerPaymentProfileIdList(emptyList);
        ArrayOfNumericString emptyShipping = new ArrayOfNumericString();
        emptyShipping.setNumericString(List.of());
        response.setCustomerShippingAddressIdList(emptyShipping);

        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any())).thenReturn(response);

        CustomerAndPaymentId result = service.createCustomerProfile("test@example.com", requestDTO);

        assertNotNull(result);
        assertEquals("cust123", result.getCustomerId());
    }

    @Test
    @DisplayName("Should throw InternalServerException when result code is ERROR while deleting customer profile")
    void testDeleteCustomerProfileById_ErrorCode() throws InternalServerException {
        DeleteCustomerProfileResponse response = new DeleteCustomerProfileResponse();
        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.ERROR);
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), any())).thenReturn(response);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                service.deleteCustomerProfileById("12345")
        );

        assertTrue(exception.getMessage().contains("Failed to delete customer profile:"));
    }

    @Test
    @DisplayName("Should update Payment customer profile successfully")
    void testUpdatePaymentCustomerProfile_Success() throws InternalServerException {
        PaymentProfileDetailRequest detail = new PaymentProfileDetailRequest();
        detail.setFirstName("John");
        detail.setLastName("Doe");
        detail.setCardNumber("4111111111111111");
        detail.setExpiryMonth("01");
        detail.setExpiryYear("30");
        detail.setCvv("123");

        // Mock getPaymentCustomerPaymentProfile
        GetCustomerPaymentProfileResponse profileResponse = new GetCustomerPaymentProfileResponse();
        MessagesType msg = new MessagesType();
        msg.setResultCode(MessageTypeEnum.OK);
        msg.setMessage(List.of(new Message()));
        profileResponse.setMessages(msg);

        when(restClient.makeRequest(contains("get"), eq(HttpMethod.GET), isNull(), eq(GetCustomerPaymentProfileResponse.class)))
                .thenReturn(profileResponse);

        UpdateCustomerPaymentProfileResponse updateResponse = new UpdateCustomerPaymentProfileResponse();
        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        messages.setMessage(List.of(new Message()));
        updateResponse.setMessages(messages);

        when(restClient.makeRequest(contains("update"), eq(HttpMethod.PUT), any(), eq(UpdateCustomerPaymentProfileResponse.class)))
                .thenReturn(updateResponse);

        assertDoesNotThrow(() -> service.updateCustomerPaymentProfile("cust123", "pay456", detail, "john@example.com"));
    }

    @Test
    @DisplayName("Should get transaction detail successfully")
    void testGetTransactionDetail_Success() throws InternalServerException {
        GetTransactionDetailsResponse response = new GetTransactionDetailsResponse();
        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        Message message = new Message();
        message.setCode("I00001");
        message.setText("Success");
        messages.setMessage(List.of(message));
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), eq(GetTransactionDetailsResponse.class)))
                .thenReturn(response);

        var result = service.getTransactionDetail("txn123");
        assertNotNull(result);
        assertEquals(MessageTypeEnum.OK, result.getMessages().getResultCode());
    }
    @Test
    @DisplayName("Should get subscription detail successfully")
    void testGetSubscription_Success() throws InternalServerException {
        GetSubscriptionResponse response = new GetSubscriptionResponse();
        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        Message message = new Message();
        message.setCode("I00001");
        message.setText("Success");
        messages.setMessage(List.of(message));
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), eq(GetSubscriptionResponse.class)))
                .thenReturn(response);

        var result = service.getSubscription("sub123");
        assertNotNull(result);
        assertEquals(MessageTypeEnum.OK, result.getMessages().getResultCode());
    }

    @Test
    @DisplayName("Should return customer profile list successfully")
    void testGetCustomerPaymentProfileList_Success() throws InternalServerException {
        GetCustomerProfileResponse response = new GetCustomerProfileResponse();
        MessagesType messages = new MessagesType();
        messages.setResultCode(MessageTypeEnum.OK);
        Message message = new Message();
        message.setCode("I00001");
        message.setText("Retrieved");
        messages.setMessage(List.of(message));
        response.setMessages(messages);

        when(restClient.makeRequest(any(), any(), any(), eq(GetCustomerProfileResponse.class)))
                .thenReturn(response);

        var result = service.getCustomerPaymentProfileList("cust123");
        assertNotNull(result);
        assertEquals(MessageTypeEnum.OK, result.getMessages().getResultCode());
    }


}

