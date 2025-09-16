package com.vinncorp.fast_learner.integration.newsletter_subscription;

import com.vinncorp.fast_learner.repositories.newsletter_subscription.NewsletterSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsletterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsletterSubscriptionRepository repo;

    @Test
    public void testSubscribeNewsletterSuccess() throws Exception {
        // Generate a unique email address by appending a random integer
        String uniqueSuffix = String.valueOf(new Random().nextInt(10000));
        String email = "instructor122+" + uniqueSuffix + "@mailinator.com";

        mockMvc.perform(post("/api/v1/newsletter-subscription/subscribe")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Successfully subscribed the newsletter.")));
    }

    @Test
    public void testSubscribeNewsletterAlreadySubscribed() throws Exception {
        String email = "instructor1@mailinator.com";

        mockMvc.perform(post("/api/v1/newsletter-subscription/subscribe")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Newsletter already subscribed by this email: " + email)));
    }



}
