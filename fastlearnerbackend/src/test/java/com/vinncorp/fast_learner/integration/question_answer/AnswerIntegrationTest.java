package com.vinncorp.fast_learner.integration.question_answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.request.question_answer.AnswerRequest;
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
public class AnswerIntegrationTest {

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
    @DisplayName("Should successfully create an answer")
    public void testCreateAnswer_Success() throws Exception {
        // Assuming the course, question, and subscription exist in the database
        Long validCourseId = 29L; // Ensure this course ID exists in the test database
        Long validQuestionId = 50L; // Ensure this question ID exists in the test database
        String validAnswerText = "This is a test answer.";

        AnswerRequest request = new AnswerRequest();
        request.setCourseId(validCourseId);
        request.setQuestionId(validQuestionId);
        request.setText(validAnswerText);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/answer/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Answer saved successfully."))
                .andExpect(jsonPath("$.data").value("Answer saved successfully."));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when course ID is null")
    public void testCreateAnswer_CourseIdNull() throws Exception {
        AnswerRequest request = new AnswerRequest();
        request.setCourseId(null); // Null course ID
        request.setQuestionId(1L); // Ensure this question ID exists in the test database
        request.setText("This is a test answer.");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/answer/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when user is not enrolled in the course")
    public void testCreateAnswer_NotEnrolled() throws Exception {
        Long validCourseId = 54L; // Ensure this course ID exists but the user is not enrolled
        Long validQuestionId = 69L; // Ensure this question ID exists in the test database

        AnswerRequest request = new AnswerRequest();
        request.setCourseId(validCourseId);
        request.setQuestionId(validQuestionId);
        request.setText("This is a test answer.");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/answer/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully fetch all answers with pagination")
    public void testGetAllAnswersWithPagination_Success() throws Exception {
        Long validCourseId = 29L;
        Long validQuestionId = 50L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/answer/")
                        .param("courseId", validCourseId.toString())
                        .param("questionId", validQuestionId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Should return 400 if user not enrolled in course")
    public void testGetAllAnswersWithPagination_NotEnrolled() throws Exception {
        Long invalidCourseId = 999L;
        Long validQuestionId = 50L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/answer/")
                        .param("courseId", invalidCourseId.toString())
                        .param("questionId", validQuestionId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enrolled in the course first."));
    }

    @Test
    @DisplayName("Should return 404 if no answers are present")
    public void testGetAllAnswersWithPagination_NoAnswers() throws Exception {
        Long validCourseId = 29L;
        Long invalidQuestionId = 999L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/answer/")
                        .param("courseId", validCourseId.toString())
                        .param("questionId", invalidQuestionId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No answers are present."));
    }
}
