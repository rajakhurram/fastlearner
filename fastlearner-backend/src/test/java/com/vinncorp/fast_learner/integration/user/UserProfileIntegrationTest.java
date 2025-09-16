package com.vinncorp.fast_learner.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.dtos.user.UserProfileDto;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private IUserProfileService service;

    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Should successfully fetch user profile")
    public void testGetUserProfile_Success() throws Exception {
        String profileUrl = "john";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user-profile/")
                        .param("profileUrl", profileUrl == null ? "" : profileUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return User profile not found if user profile is not found by provided profile url")
    public void testGetUserProfile_ProfileNotFoundById() throws Exception {
        String profileUrl = "invalid-profile-url";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user-profile/")
                        .param("profileUrl", profileUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User profile not found"));
    }

    @Test
    @DisplayName("Should successfully update user profile")
    public void testUpdateUserProfile_Success() throws Exception {
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setFullName("Qasim");
        userProfileDto.setEmail("example@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-profile/update")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userProfileDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User profile updated successfully"));
    }


    @Test
    @DisplayName("Should successfully update user profile with optional fields")
    public void testUpdateUserProfile_Success_OptionalFields() throws Exception {
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setFullName("Alice Smith");
        userProfileDto.setEmail("alice.smith@example.com");
        userProfileDto.setExperience("2 years");
        userProfileDto.setQualification("Masters");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-profile/update")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userProfileDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User profile updated successfully"));
    }


    @Test
    @DisplayName("Should return 403 Forbidden for invalid JWT token")
    public void testUpdateUserProfile_InvalidToken() throws Exception {
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setFullName("Qasim");
        userProfileDto.setEmail("example@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-profile/update")
                        .header("Authorization", "Bearer " + "invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userProfileDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when required fields are missing")
    public void testUpdateUserProfile_MissingRequiredFields() throws Exception {
        UserProfileDto userProfileDto = new UserProfileDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-profile/update")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userProfileDto)))
                .andExpect(status().isBadRequest());
    }



    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
