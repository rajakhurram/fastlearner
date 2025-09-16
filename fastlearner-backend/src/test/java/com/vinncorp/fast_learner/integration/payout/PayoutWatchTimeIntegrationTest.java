package com.vinncorp.fast_learner.integration.payout;


import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PayoutWatchTimeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private String JWT_TOKEN;

    @BeforeEach
    public void setup() throws Exception {
        JWT_TOKEN = TokenUtils.getToken(mockMvc);
    }

    @Test
    public void testCreateWatchTime_whenProvidedCourseOrUserNotFound() throws Exception {
        mockMvc.perform(post(APIUrls.PAYOUT_WATCH_TIME + APIUrls.CREATE_PAYOUT_WATCH_TIME)
                        .header(HttpHeaders.ORIGIN, "fastlearner.ai")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .param("courseId", "22222")
                        .param("watchTime", "222")
                        .param("email", TokenUtils.EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())));
    }

    @Test
    public void testCreateWatchTime_whenFreeOrPremiumCourseIsGiven() throws Exception {
        mockMvc.perform(post(APIUrls.PAYOUT_WATCH_TIME + APIUrls.CREATE_PAYOUT_WATCH_TIME)
                        .header(HttpHeaders.ORIGIN, "fastlearner.ai")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .param("courseId", "15")
                        .param("watchTime", "222")
                        .param("email", TokenUtils.EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));
    }

    /**
     * When valid course and user provided then I have to check if the course is not a premium or free course and then
     * i have to fetch user's current subscription if he has sub 2 then i have to add watch time into timeSpendForSub2
     * field else if he has sub 3 then i will added it into timeSpendForSub3.
     *
     * */
    @Test
    public void testCreateWatchTime_whenValidEmailWatchTimeAndStandardCourseIsGiven() throws Exception {
        mockMvc.perform(post(APIUrls.PAYOUT_WATCH_TIME + APIUrls.CREATE_PAYOUT_WATCH_TIME)
                        .header(HttpHeaders.ORIGIN, "fastlearner.ai")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .param("courseId", "54")
                        .param("watchTime", "500")
                        .param("email", "samikhan1@yopmail.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())));
    }
}
