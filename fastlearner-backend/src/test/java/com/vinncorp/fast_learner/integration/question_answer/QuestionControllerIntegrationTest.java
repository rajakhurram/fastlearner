package com.vinncorp.fast_learner.integration.question_answer;

import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.request.question_answer.QuestionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class QuestionControllerIntegrationTest {



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
    void createQuestionSuccessfully() throws Exception {
        QuestionRequest request = new QuestionRequest();
        // Use the correct method name based on your QuestionRequest class
        request.setText("What is the capital of France?");
        request.setCourseId(29L);  // Assuming you have courseId
        request.setTopicId(85L);   // Assuming you have topicId

        mockMvc.perform(post("/api/v1/question/")
                        .header("Authorization", "Bearer " + jwtToken)  // Include your JWT token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Question is created successfully."));
    }

    @Test
    void createQuestionBadRequest() throws Exception {
        QuestionRequest request = new QuestionRequest();
        // Missing required fields to trigger BadRequestException

        mockMvc.perform(post("/api/v1/question/")
                        .header("Authorization", "Bearer " + jwtToken) // Add JWT token here
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createQuestionEntityNotFound() throws Exception {
        QuestionRequest request = new QuestionRequest();

        mockMvc.perform(post("/api/v1/question/")
                        .header("Authorization", "Bearer " + jwtToken) // Add JWT token here
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createQuestionInternalServerError() throws Exception {
        QuestionRequest request = new QuestionRequest();

        mockMvc.perform(post("/api/v1/question/")
                        .header("Authorization", "Bearer " + jwtToken) // Add JWT token here
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully fetch all questions with pagination")
    public void testFetchAllQuestionsWithPagination_Success() throws Exception {
        Long validCourseId = 29L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/question/")
                        .param("courseId", validCourseId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched all questions successfully."));
    }

    @Test
    @DisplayName("Should return 400 if user not enrolled in course")
    public void testFetchAllQuestionsWithPagination_NotEnrolled() throws Exception {
        Long invalidCourseId = 999L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/question/")
                        .param("courseId", invalidCourseId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enrolled in the course first."));
    }

    @Test
    @DisplayName("Should return 400 if no questions are present")
    public void testFetchAllQuestionsWithPagination_NoQuestions() throws Exception {
        Long validCourseId = 229L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/question/")
                        .param("courseId", validCourseId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
