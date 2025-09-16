package com.vinncorp.fast_learner.integration.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.test_util.Constants;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import com.vinncorp.fast_learner.util.enums.PlanType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@SpringBootTest
@AutoConfigureMockMvc
public class SubscriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;
    @Autowired
    private ObjectMapper objectMapper;

    private SubscriptionRequest couponRequest;
    private SubscribedUser subscribedUser;

    private static final String SUBSCRIPTION_API = APIUrls.SUBSCRIPTION_API;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);

        Subscription subscription = new Subscription();
        subscription.setId(2L);
        subscription.setPlanType(PlanType.STANDARD);

        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("instructor@mailinator.com");
        user.setStripeAccountId("acct_1Example12345");
        user.setAge(30);
        user.setPassword("securePassword123"); // Assuming you're manually setting this for test or setup purposes

        Role role = new Role();
        role.setId(1L); // assuming role ID is known
        role.setType("ROLE_USER");
        user.setRole(role);

        user.setProvider(AuthProvider.LOCAL); // or AuthProvider.GOOGLE, etc.
        user.setCreationDate(new Date());
        user.setLoginTimestamp(new Date());
        user.setSubscribed(true);
        user.setSalesRaise(1.5);
        user.setActive(true);
        subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setCoupon(null);
        subscribedUser.setSubscription(subscription);
        subscribedUser.setUser(user);


    }
    @Test
    void testCreateSubscription_withCoupon_shouldReturnSuccess() throws Exception {
        SubscriptionRequest couponRequest = new SubscriptionRequest();
        couponRequest.setCoupon("WELCOME50");


        couponRequest.setImmediatelyApply(true);
        // Perform request
        mockMvc.perform(post(PAYMENT_GATEWAY + CREATE_SUBSCRIPTION)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponRequest)))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Successfully fetch all active subscriptions")
    public void fetchAllSubscription_whenSubscriptionsExist_thenReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SUBSCRIPTION_API + GET_ALL_SUBSCRIPTION)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("All subscription is fetched successfully."));
    }

    @Test
    @DisplayName("Successfully fetch subscription by ID")
    public void fetchSubscriptionById_whenSubscriptionExists_thenReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SUBSCRIPTION_API + GET_SUBSCRIPTION_BY_ID.replace("{subscriptionId}", Constants.SUBSCRIPTION_ID.toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Subscription is fetched successfully."));
    }

    @Test
    @DisplayName("Successfully fetch current subscription for user")
    public void getCurrentSubscription_whenSubscriptionExists_thenReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SUBSCRIPTION_API + GET_CURRENT_SUBSCRIPTION)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Fetching current subscriptions successfully."));
    }

}
