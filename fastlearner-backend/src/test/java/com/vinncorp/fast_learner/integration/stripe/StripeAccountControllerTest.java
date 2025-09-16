package com.vinncorp.fast_learner.integration.stripe;

import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StripeAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String JWT_TOKEN;

    @BeforeEach
    void init() throws Exception {
        if (JWT_TOKEN == null) {
            JWT_TOKEN = TokenUtils.getToken(mockMvc);  // Assuming TokenUtils is available for getting the token
        }
    }

    @DisplayName("Test: Fetch Connected Account Details - Success")
    @Test
    public void fetchConnectedAccountDetails_whenValidInput_thenReturnsAccountDetails() throws Exception {
        mockMvc.perform(get(APIUrls.STRIPE_ACCOUNT + APIUrls.STRIPE_ACCOUNT_DETAILS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Account details fetched successfully."));
    }


    @DisplayName("Test: Fetch Payment Withdrawal History - Success")
    @Test
    public void fetchConnectedAccountHistory_whenValidInput_thenReturnsHistory() throws Exception {

        mockMvc.perform(get(APIUrls.STRIPE_ACCOUNT + APIUrls.STRIPE_ACCOUNT_HISTORY)
                        .param("pageNo", "0")
                        .param("pageSize", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Payment withdrawn history successfully fetched."));
    }
}
