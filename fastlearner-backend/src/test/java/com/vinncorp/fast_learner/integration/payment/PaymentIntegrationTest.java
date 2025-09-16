package com.vinncorp.fast_learner.integration.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.dtos.payment.BillingHistoryRequest;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.integration.util.JsonUtil;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.repositories.subscription.SubscribedUserProfileRepository;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscribedUserService subscribedUserService;

    @Autowired
    private SubscribedUserProfileRepository subscribedUserProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String JWT_TOKEN;
    private SubscriptionRequest subscriptionRequest;

    @BeforeEach
    void init() throws Exception {
        JWT_TOKEN = TokenUtils.getToken(mockMvc);

        PaymentProfileDetailRequest paymentProfileDetailRequest = PaymentProfileDetailRequest.builder()
                .firstName("Qasim")
                .lastName("Ali")
                .expiryMonth("11")
                .expiryYear("30")
                .cardNumber("4111111111111111")
                .cvv("900")
                .build();

        subscriptionRequest = SubscriptionRequest.builder()
                .subscriptionId(2L)
                .paymentDetail(paymentProfileDetailRequest)
                .build();
    }

    @DisplayName("Test: Paid Subscription - when provided valid data (Success)")
    @Test
    @Order(1)
    public void createStandardSubscription_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.CREATE_SUBSCRIPTION)
                        .content(JsonUtil.asJsonString(subscriptionRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.name()))
                .andExpect(jsonPath("$.message").value("Subscription: Standard Plan has been subscribed by instructor1@mailinator.com"))
                .andExpect(jsonPath("$.data").value("You have successfully subscribed this plan"));
    }

    @DisplayName("Test: Get Saved Payment Profile - when provided valid data (Success)")
    @Test
    @Order(2)
    public void getSavedUserPaymentProfile_whenValidData() throws Exception {

        mockMvc.perform(get(APIUrls.PAYMENT_GATEWAY + APIUrls.GET_SAVED_PAYMENT_PROFILE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.toString()))
                .andExpect(jsonPath("$.message").value("Successfully get the default payment profile."))
                .andExpect(jsonPath("$.data").exists());
    }

    @DisplayName("Test: Add Payment Profile - when provided valid data (Success)")
    @Test
    @Order(3)
    public void addUserPaymentProfile_whenProvidedValidData() throws Exception {
        PaymentProfileDetailRequest paymentProfileDetailRequest = PaymentProfileDetailRequest.builder()
                .firstName("Salman")
                .lastName("Ali")
                .expiryMonth("12")
                .expiryYear("30")
                .cardNumber("5424000000000015")
                .cvv("900")
                .isSave(false)
                .build();

        mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.ADD_PAYMENT_PROFILE)
                        .content(JsonUtil.asJsonString(paymentProfileDetailRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.toString()))
                .andExpect(jsonPath("$.message").value("Successfully added customer payment profile."))
                .andExpect(jsonPath("$.data").exists());
    }

    @DisplayName("Test: Add Payment Profile - when profile is duplicated")
    @Test
    @Order(4)
    public void addUserPaymentProfile_whenProvidedInvalidData() throws Exception {
        PaymentProfileDetailRequest paymentProfileDetailRequest = PaymentProfileDetailRequest.builder()
                .firstName("Salman")
                .lastName("Ali")
                .expiryMonth("12")
                .expiryYear("30")
                .cardNumber("5424000000000015")
                .cvv("900")
                .isSave(false)
                .build();

        mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.ADD_PAYMENT_PROFILE)
                        .content(JsonUtil.asJsonString(paymentProfileDetailRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.message").value("Payment profile: A duplicate customer payment profile already exists."));
    }

    @DisplayName("Test: Update Payment Profile - when provided valid data (Success)")
    @Test
    @Order(5)
    public void updateUserPaymentProfile_whenProvidedValidData() throws Exception {
        SubscribedUser user = subscribedUserService.findByUser("instructor1@mailinator.com");
        SubscribedUserProfile profile = subscribedUserProfileRepository.findByIsDefaultAndSubscribedUser(false, user)
                .orElseThrow(() -> new RuntimeException("No subscribed user profile found."));

        PaymentProfileDetailRequest paymentProfileDetailRequest = PaymentProfileDetailRequest.builder()
                .id(profile.getId())
                .firstName("Qasim")
                .lastName("Ali")
                .expiryMonth("12")
                .expiryYear("32")
                .cardNumber("4111111111111111")
                .cvv("900")
                .isSave(false)
                .build();

        mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.UPDATE_PAYMENT_PROFILE)
                        .content(JsonUtil.asJsonString(paymentProfileDetailRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.toString()))
                .andExpect(jsonPath("$.message").value("Successfully update customer payment profile."))
                .andExpect(jsonPath("$.data").exists());
    }

    @DisplayName("Test: Update Payment Profile - when no user payment profile exists")
    @Test
    @Order(6)
    public void updateUserPaymentProfile_whenProvidedInvalidData() throws Exception {
        PaymentProfileDetailRequest paymentProfileDetailRequest = PaymentProfileDetailRequest.builder()
                .id(123123L)
                .firstName("Qasim")
                .lastName("Ali")
                .expiryMonth("12")
                .expiryYear("32")
                .cardNumber("4111111111111111")
                .cvv("900")
                .isSave(false)
                .build();

        ResultActions result = mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.UPDATE_PAYMENT_PROFILE)
                        .content(JsonUtil.asJsonString(paymentProfileDetailRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.toString()))
                .andExpect(jsonPath("$.message").exists());

        MvcResult mvcResult = result.andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        String messageResponse = jsonNode.get("message").toString();

        assertThat(messageResponse).contains("Payment profile not exists with this id ");
    }

    @DisplayName("Test: Delete Payment Profile - when provided valid data (Success)")
    @Test
    @Order(7)
    public void deleteUserPaymentProfile_whenProvidedValidData() throws Exception {
        SubscribedUser user = subscribedUserService.findByUser("instructor1@mailinator.com");
        SubscribedUserProfile profile = subscribedUserProfileRepository.findByIsDefaultAndSubscribedUser(false, user)
                .orElseThrow(() -> new RuntimeException("No subscribed user profile found."));

        mockMvc.perform(get(APIUrls.PAYMENT_GATEWAY + "/payment-profile/" + profile.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.toString()))
                .andExpect(jsonPath("$.message").value("Payment profile delete successfully."));
    }

    @DisplayName("Test: Delete Payment Profile - when provided invalid data")
    @Test
    @Order(8)
    public void deleteUserPaymentProfile_whenProvidedInvalidData() throws Exception {
        mockMvc.perform(get(APIUrls.PAYMENT_GATEWAY + "/payment-profile/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.toString()))
                .andExpect(jsonPath("$.message").value("Payment profile not exists with this id 999"));
    }

    @DisplayName("Test: Get All Payment Profile - when provided valid data (Success)")
    @Test
    @Order(9)
    public void getAllPaymentProfile_whenValidData() throws Exception {

        mockMvc.perform(get(APIUrls.PAYMENT_GATEWAY + APIUrls.GET_ALL_PAYMENT_PROFILE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.toString()))
                .andExpect(jsonPath("$.message").value("Successfully get all the profiles."))
                .andExpect(jsonPath("$.data").exists());
    }

    @DisplayName("Test: Free Subscription - when provided valid data (Success)")
    @Test
    @Order(10)
    public void createFreeSubscription_shouldReturnSuccess() throws Exception {
        SubscriptionRequest subscriptionRequest = SubscriptionRequest.builder()
                .subscriptionId(1L)
                .paymentDetail(null)
                .build();

        mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.CREATE_SUBSCRIPTION)
                        .content(JsonUtil.asJsonString(subscriptionRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.name()))
                .andExpect(jsonPath("$.data").value("You have successfully subscribed the free plan"));

    }

    @DisplayName("Test: Get Saved Payment Profile - when no payment profile set to default")
    @Test
    @Order(11)
    public void getSavedUserPaymentProfile_whenNoPaymentProfilePresentInDb() throws Exception {

        mockMvc.perform(get(APIUrls.PAYMENT_GATEWAY + APIUrls.GET_SAVED_PAYMENT_PROFILE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.toString()))
                .andExpect(jsonPath("$.message").value("No profile is set to default"));
    }

    @DisplayName("Test: Subscription - when provided subscription id which is already subscribed")
    @Test
    @Order(12)
    public void createSubscription_whenProvidedSameSubscriptionId_shouldReturnBadRequest() throws Exception {
        SubscriptionRequest subscriptionRequest = SubscriptionRequest.builder()
                .subscriptionId(1L)
                .paymentDetail(null)
                .build();

        mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.CREATE_SUBSCRIPTION)
                        .content(JsonUtil.asJsonString(subscriptionRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.message").value("You already subscribed to this plan"));
    }

    @DisplayName("Test: Get All Payment Profile - when no customer profile present")
    @Test
    @Order(13)
    public void getSavedUserPaymentProfile_whenNoCustomerProfilePresent() throws Exception {

        mockMvc.perform(get(APIUrls.PAYMENT_GATEWAY + APIUrls.GET_ALL_PAYMENT_PROFILE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.toString()))
                .andExpect(jsonPath("$.message").value("No customer profile id found for the user."));
    }

    @Test
    @DisplayName("Get Billing History - Customer Profile ID Not Exists")
    @Order(14)
    void getBillingHistory_whenCustomerProfileIdNotExist_shouldReturnBadRequest() throws Exception {
        BillingHistoryRequest request = new BillingHistoryRequest();
        request.setPageNo(1);
        request.setPageSize(5);

        mockMvc.perform(post(APIUrls.PAYMENT_GATEWAY + APIUrls.GET_BILLING_HISTORY)
                .content(JsonUtil.asJsonString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Customer profile id not exists"));
    }
}
