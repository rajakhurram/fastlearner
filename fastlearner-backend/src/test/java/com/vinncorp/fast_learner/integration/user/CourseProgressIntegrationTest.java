package com.vinncorp.fast_learner.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.request.user.CreateUserCourseProgressRequest;
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
public class CourseProgressIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }
    @Test
    @DisplayName("Should successfully mark a topic as complete")
    public void testMarkComplete_Success() throws Exception {
        Long validTopicId = 29L;

        CreateUserCourseProgressRequest request = new CreateUserCourseProgressRequest();
        request.setTopicId(validTopicId);
        request.setIsCompleted(true);
        request.setSeekTime(300L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-course-progress/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Topic is marked completed."));
    }

    @Test
    @DisplayName("Should successfully unmark a topic")
    public void testUnmarkComplete_Success() throws Exception {
        Long validTopicId = 29L;

        CreateUserCourseProgressRequest request = new CreateUserCourseProgressRequest();
        request.setTopicId(validTopicId);
        request.setIsCompleted(false);
        request.setSeekTime(150L); // Intermediate seek time

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-course-progress/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unmarked the topic."));
    }


    @Test
    @DisplayName("Should return NotFound if topic ID is invalid")
    public void testMarkComplete_TopicNotFound() throws Exception {
        Long invalidTopicId = 9999L; // Non-existent topic ID

        CreateUserCourseProgressRequest request = new CreateUserCourseProgressRequest();
        request.setTopicId(invalidTopicId);
        request.setIsCompleted(true);
        request.setSeekTime(300L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-course-progress/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound());
    }



    @Test
    @DisplayName("Should return Forbidden if JWT token is expired")
    public void testMarkComplete_ExpiredJwtToken() throws Exception {
        String expiredJwtToken = "expiredToken"; // Replace with an actual expired token
        Long validTopicId = 29L;

        CreateUserCourseProgressRequest request = new CreateUserCourseProgressRequest();
        request.setTopicId(validTopicId);
        request.setIsCompleted(true);
        request.setSeekTime(300L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user-course-progress/")
                        .header("Authorization", "Bearer " + expiredJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("Should successfully fetch course progress")
    public void testFetchCourseProgress_Success() throws Exception {
        Long validCourseId = 29L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user-course-progress/" + validCourseId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully fetch course progress"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }


    @Test
    @DisplayName("Should return NotFound if course ID is invalid")
    public void testFetchCourseProgress_CourseNotFound() throws Exception {
        Long invalidCourseId = 9999L; // Non-existent course ID

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user-ourse-progress/" + invalidCourseId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return Forbidden if authorization header is missing")
    public void testFetchCourseProgress_MissingAuthHeader() throws Exception {
        Long validCourseId = 29L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/course-progress/" + validCourseId)
                        .contentType(MediaType.APPLICATION_JSON)) // Missing Authorization header
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully fetch all active students")
    public void testGetAllActiveStudentsByCourseIdOrInstructorId_Success() throws Exception {
        Long validCourseId = 29L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user-course-progress/active-students")
                        .param("courseId", validCourseId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched all active students successfully."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").isNotEmpty());
    }



    @Test
    @DisplayName("Should return Forbidden if authorization header is missing")
    public void testGetAllActiveStudentsByCourseIdOrInstructorId_MissingAuthHeader() throws Exception {
        Long validCourseId = 29L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user-course-progress/active-students")
                        .param("courseId", validCourseId.toString())
                        .contentType(MediaType.APPLICATION_JSON)) // Missing Authorization header
                .andExpect(status().isForbidden());
    }



    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
}}
