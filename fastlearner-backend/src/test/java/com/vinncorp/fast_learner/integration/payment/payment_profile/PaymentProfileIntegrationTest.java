package com.vinncorp.fast_learner.integration.payment.payment_profile;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.customer_profile.GetCustomerProfileResponse;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import com.vinncorp.fast_learner.response.subscription.GetTransactionDetailsResponse;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentProfileIntegrationTest {

    @Autowired
    private IPaymentProfileService service;

    @Test
    @DisplayName("Test getPaymentCustomerPaymentProfile when provided valid data.")
    public void testGetPaymentCustomerPaymentProfile_whenProvidedValidData() throws InternalServerException {
        String customerProfileId = "522287685";
        String customerPaymentProfileId = "534324498";
        var m = service.getCustomerPaymentProfile(customerProfileId, customerPaymentProfileId);

        assertThat(m).isNotNull();
    }

    @Test
    @DisplayName("Test getTransactionDetail when provided valid data.")
    public void testGetTransactionDetail_whenProvidedValidData() throws InternalServerException {
        String transactionId = "120063506252";
        var m = service.getTransactionDetail(transactionId);

        assertThat(m).isNotNull();
        assertThat(m.getTransaction().getTransId()).isEqualTo(transactionId);
    }

    @Test
    @DisplayName("Test createCustomerProfile when provided valid data.")
    public void createCustomerProfile_whenProvidedValidData() throws InternalServerException, BadRequestException {
        // Create a valid SubscriptionRequest with real or sandbox test card info
        SubscriptionRequest request = new SubscriptionRequest();
        PaymentProfileDetailRequest paymentDetail = new PaymentProfileDetailRequest();
        paymentDetail.setCardNumber("4111111111111111"); // use sandbox test card
        paymentDetail.setCvv("123");
        paymentDetail.setExpiryMonth("12");
        paymentDetail.setExpiryYear("2025");
        paymentDetail.setFirstName("Jane");
        paymentDetail.setLastName("Doe");
        request.setPaymentDetail(paymentDetail);

        String email = "test@integration.com";

        CustomerAndPaymentId result = service.createCustomerProfile(email, request);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isNotNull();
        assertThat(result.getPaymentId()).isNotNull();

        service.deleteCustomerProfileById(result.getCustomerId());
    }//

    @Test
    @DisplayName("Test createCustomerProfile when provided invalid data.")
    void testCreateCustomerProfile_EndToEndFailure_InvalidCard() {
        SubscriptionRequest request = new SubscriptionRequest();
        PaymentProfileDetailRequest paymentDetail = new PaymentProfileDetailRequest();
        paymentDetail.setCardNumber("1234567890123456"); // Invalid card number
        paymentDetail.setCvv("999");
        paymentDetail.setExpiryMonth("01");
        paymentDetail.setExpiryYear("2023");
        paymentDetail.setFirstName("Fake");
        paymentDetail.setLastName("User");
        request.setPaymentDetail(paymentDetail);

        String email = "fakeuser@example.com";

        Exception exception = assertThrows(InternalServerException.class, () -> {
            service.createCustomerProfile(email, request);
        });

        assertTrue(exception.getMessage().contains("Error while creating customer profile.")
                || exception.getMessage().contains("Something went wrong"));
    }

    @Test
    @DisplayName("Test deleteCustomerProfileById when provided valid data.")
    void testDeleteCustomerProfileById_Success() throws Exception {
        // First create a profile (successfully)
        SubscriptionRequest request = new SubscriptionRequest();
        PaymentProfileDetailRequest paymentDetail = new PaymentProfileDetailRequest();
        paymentDetail.setCardNumber("4111111111111111"); // Valid sandbox test card
        paymentDetail.setCvv("123");
        paymentDetail.setExpiryMonth("12");
        paymentDetail.setExpiryYear("2026");
        paymentDetail.setFirstName("Delete");
        paymentDetail.setLastName("Test");
        request.setPaymentDetail(paymentDetail);

        String email = "deletetest@example.com";
        CustomerAndPaymentId result = service.createCustomerProfile(email, request);
        assertNotNull(result);
        String customerId = result.getCustomerId();

        // Now delete the customer profile
        assertDoesNotThrow(() -> service.deleteCustomerProfileById(customerId));
    }

    @Test
    @DisplayName("Test deleteCustomerProfileById with invalid data.")
    void testDeleteCustomerProfileById_Failure_InvalidId() {
        String invalidId = "99999999"; // Assumed non-existent or invalid ID

        Exception exception = assertThrows(BadRequestException.class, () -> {
            service.deleteCustomerProfileById(invalidId);
        });

        assertTrue(exception.getMessage().contains("Customer profile not found"));
    }

    @Test
    @DisplayName("Test getCustomerPaymentProfileList when provided valid data.")
    void testGetCustomerPaymentProfileList_Success() throws Exception {
        // First create a customer to ensure the ID exists
        SubscriptionRequest request = new SubscriptionRequest();
        PaymentProfileDetailRequest paymentDetail = new PaymentProfileDetailRequest();
        paymentDetail.setCardNumber("4111111111111111");
        paymentDetail.setCvv("123");
        paymentDetail.setExpiryMonth("12");
        paymentDetail.setExpiryYear("2026");
        paymentDetail.setFirstName("John");
        paymentDetail.setLastName("Doe");
        request.setPaymentDetail(paymentDetail);

        String email = "testfetchprofile@example.com";
        CustomerAndPaymentId createdProfile = service.createCustomerProfile(email, request);
        String customerId = createdProfile.getCustomerId();

        // Now fetch the customer profile list
        GetCustomerProfileResponse response = service.getCustomerPaymentProfileList(customerId);

        assertNotNull(response);
        assertEquals(MessageTypeEnum.OK, response.getMessages().getResultCode());
        assertEquals(customerId, response.getProfile().getCustomerProfileId());
        assertFalse(response.getProfile().getPaymentProfiles().isEmpty());

        service.deleteCustomerProfileById(customerId);
    }

    @Test
    @DisplayName("Test getCustomerPaymentProfileList when provided invalid data.")
    void testGetCustomerPaymentProfileList_Failure_InvalidCustomerId() {
        String invalidCustomerId = "99999999"; // Assumed invalid

        Exception exception = assertThrows(InternalServerException.class, () -> {
            service.getCustomerPaymentProfileList(invalidCustomerId);
        });

        assertTrue(exception.getMessage().contains("Payment Subscription"));
    }

    @Test
    @DisplayName("Test getTransactionDetail when provided valid data.")
    void testGetTransactionDetail_Success() throws Exception {
        String transactionId = "120065085418"; // Sandbox already exited transaction id

        // Step 3: Fetch transaction detail
        GetTransactionDetailsResponse response = service.getTransactionDetail(transactionId);

        assertNotNull(response);
        assertEquals(MessageTypeEnum.OK, response.getMessages().getResultCode());
        assertEquals(transactionId, response.getTransaction().getTransId());
    }

    @Test
    @DisplayName("Test getTransactionDetail when provided invalid data.")
    void testGetTransactionDetail_Failure_InvalidId() {
        String invalidTxnId = "0000000000"; // Invalid/non-existent ID

        Exception ex = assertThrows(InternalServerException.class, () -> {
            service.getTransactionDetail(invalidTxnId);
        });

        assertTrue(ex.getMessage().contains("Payment Subscription"));
    }

    @Test
    @DisplayName("Test getSubscription when provided valid data.")
    void testGetSubscription_whenProvidedValidData() throws InternalServerException {
        String subscriptionId = "9602445"; // Sandbox already existed subscription id
        GetSubscriptionResponse response = service.getSubscription(subscriptionId);
        assertNotNull(response);
        assertEquals(MessageTypeEnum.OK, response.getMessages().getResultCode());
    }

    @Test
    @DisplayName("Test getSubscription when provided invalid data.")
    void testGetSubscription_whenProvidedInValidData() throws InternalServerException {
        String subscriptionId = "43894789743"; // Invalid subscription id
        Exception exception = assertThrows(InternalServerException.class, () -> {
            service.getSubscription(subscriptionId);
        });

        assertTrue(exception.getMessage().contains("Payment Subscription"));
    }
}
