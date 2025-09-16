package com.vinncorp.fast_learner.integration.contact_us;


import com.vinncorp.fast_learner.integration.util.JsonUtil;
import com.vinncorp.fast_learner.models.contact_us.ContactUs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc
public class ContactUsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Submit Contact Us Form Successfully")
    public void submitContactUsForm_whenValidRequest_thenReturnsSuccess() throws Exception {
        ContactUs request = new ContactUs();
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("+1234567890");
        request.setDescription("This is a test message.");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contact-us/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Your request has been submitted."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("Your request has been submitted."));
    }

    @Test
    @DisplayName("Submit Contact Us Form with Invalid Data")
    public void submitContactUsForm_whenInvalidRequest_thenReturnsBadRequest() throws Exception {
        ContactUs request = new ContactUs();
        // Omitting fields to simulate invalid request

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/contact-us/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


}