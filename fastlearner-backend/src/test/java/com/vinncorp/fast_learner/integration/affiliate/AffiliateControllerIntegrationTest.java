package com.vinncorp.fast_learner.integration.affiliate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.request.affiliate.CreateAffiliateReq;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.RequestEntity.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AffiliateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Test getting affiliates with valid instructorId and pagination")
    public void testGetAffiliateByInstructorSuccess() throws Exception {
        Long validInstructorId = 12L;
        int pageNo = 0;
        int pageSize = 5;

        mockMvc.perform(get("/api/v1/affiliate/fetch")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("instructorId", validInstructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    @DisplayName("Test getting affiliates with invalid instructorId")
    public void testGetAffiliateByInstructorInvalidInstructorId() throws Exception {
        Long invalidInstructorId = 1L;
        int pageNo = 0;
        int pageSize = 5;

        mockMvc.perform(get("/api/v1/affiliate/fetch")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("instructorId", invalidInstructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test getting affiliates with invalid pageSize (less than minimum)")
    public void testGetAffiliateByInstructorInvalidPageSize() throws Exception {
        Long validInstructorId = 1L;
        int pageNo = 0;
        int invalidPageSize = 0;

        mockMvc.perform(get("/api/v1/affiliate/fetch")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("instructorId", validInstructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(invalidPageSize)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("500"))
                .andExpect(jsonPath("$.message", containsString("Request processing failed: java.lang.IllegalArgumentException: Page size must not be less than one")));
    }

    @Test
    @DisplayName("Test getting affiliates when instructor has no affiliates")
    public void testGetAffiliateByInstructorNoAffiliates() throws Exception {
        Long instructorIdWithNoAffiliates = 999L; // Use a specific instructor ID that has no affiliates
        int pageNo = 0;
        int pageSize = 5;

        mockMvc.perform(get("/api/v1/affiliate/fetch")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("instructorId", instructorIdWithNoAffiliates.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.data", nullValue()));
    }


    //delete affiliate

    @Test
    @DisplayName("Test deleting an affiliate successfully")
    public void testDeleteAffiliateSuccess() throws Exception {
        Long validAffiliateId = 1L; // Replace with an actual valid ID for the test

        mockMvc.perform(delete("/api/v1/affiliate/delete")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("affiliateId", validAffiliateId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Affiliate deleted successfully"))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    @DisplayName("Test deleting an affiliate with an invalid ID")
    public void testDeleteAffiliateInvalidId() throws Exception {
        Long invalidAffiliateId = 9999L; // Use an ID that does not exist

        mockMvc.perform(delete("/api/v1/affiliate/delete")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("affiliateId", invalidAffiliateId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Affiliate not found")));
    }

    @Test
    @DisplayName("Test deleting an affiliate without providing an ID")
    public void testDeleteAffiliateMissingId() throws Exception {
        mockMvc.perform(delete("/api/v1/affiliate/delete")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString("Required parameter 'affiliateId' is not present")));
    }

    @Test
    @DisplayName("Test deleting an affiliate with unauthorized access")
    public void testDeleteAffiliateUnauthorized() throws Exception {
        Long validAffiliateId = 1L;

        mockMvc.perform(delete("/api/v1/affiliate/delete")
                        .param("affiliateId", validAffiliateId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", containsString("JWT Token is invalid")));
    }

    //update integration

//    @Test
//    @DisplayName("Test updating an affiliate with invalid request")
//    public void testUpdateAffiliateSuccess() throws Exception {
//        // Arrange
//        CreateAffiliateReq request = new CreateAffiliateReq();
//        request.setEmail("test@example.com");
//        request.setName("John Doe");
//        request.setNickName("JD");
//        request.setInstructorId(123L);
//        request.setReward(50.0);
//
//        // Convert the request object to JSON
//        String requestJson = objectMapper.writeValueAsString(request);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/affiliate/update")
//                        .header("Authorization", "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.status").value("404"));
//    }
//
//    @Test
//    @DisplayName("Test updating an affiliate with invalid instructor ID")
//    public void testUpdateAffiliateWithInvalidInstructorId() throws Exception {
//        // Arrange
//        CreateAffiliateReq request = new CreateAffiliateReq();
//        request.setEmail("instructor1@mailinator.com");
//        request.setName("Bob Johnson");
//        request.setNickName("BJ");
//        request.setInstructorId(-1L); // Invalid instructor ID
//        request.setReward(50.0);
//
//        // Convert to JSON
//        String requestJson = objectMapper.writeValueAsString(request);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/affiliate/update")
//                        .header("Authorization", "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").value("affiliate not found with ID: -1")); // Example error message
//    }
//
//    @Test
//    @DisplayName("Test updating an affiliate with null optional fields")
//    public void testUpdateAffiliateWithNullOptionalFields() throws Exception {
//        // Arrange
//        CreateAffiliateReq request = new CreateAffiliateReq();
//        request.setEmail("instructor1@mailinator.com");
//        request.setName(null); // Set name to null
//        request.setNickName(null); // Set nickname to null
//        request.setInstructorId(456L); // Valid instructor ID
//        request.setReward(null); // Set reward to null
//
//        // Convert to JSON
//        String requestJson = objectMapper.writeValueAsString(request);
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/affiliate/update")
//                        .header("Authorization", "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value("404"));
//    }

}
