package com.vinncorp.fast_learner.integration.quiz;

import com.vinncorp.fast_learner.integration.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizIntegrationTest {
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
    @DisplayName("Should successfully validate the correct answer")
    public void testValidateAnswer_CorrectAnswer() throws Exception {
        Long questionId = 11L; // Ensure this question ID exists in the test database
        Long answerId = 25L;

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .param("answerId", answerId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Answer is correct."))
                .andExpect(jsonPath("$.data.isCorrect").value(true));

    }

    @Test
    @DisplayName("Should successfully validate the incorrect answer")
    public void testValidateAnswer_IncorrectAnswer() throws Exception {
        Long questionId = 11L;
        Long answerId = 26L; // Ensure this answer is marked as incorrect

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .param("answerId", answerId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Answer is incorrect."))
                .andExpect(jsonPath("$.data.isCorrect").value(false));
    }

    @Test
    @DisplayName("Should return 404 when the question ID does not exist")
    public void testValidateAnswer_QuestionNotFound() throws Exception {
        Long questionId = 999L;
        Long answerId = 25L;

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .param("answerId", answerId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No data found for provided question and answer id."));
    }

    @Test
    @DisplayName("Should return 404 when the answer ID does not exist")
    public void testValidateAnswer_AnswerNotFound() throws Exception {
        Long questionId = 11L;
        Long answerId = 999L;

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .param("answerId", answerId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Question doesn't have any answer by provided id."));
    }

    @Test
    @DisplayName("Should return 400 when answer ID is missing")
    public void testValidateAnswer_MissingAnswerId() throws Exception {
        Long questionId = 11L;

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when unauthorized access is attempted")
    public void testValidateAnswer_UnauthorizedAccess() throws Exception {
        Long questionId = 11L;
        Long answerId = 25L;

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .param("answerId", answerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Adjust based on actual response message
    }

    @Test
    @DisplayName("Should return 404 when question ID is invalid")
    public void testValidateAnswer_InvalidQuestionId() throws Exception {
        Long questionId = -1L;
        Long answerId = 25L;

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .param("answerId", answerId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Adjust based on actual validation message
    }

    @Test
    @DisplayName("Should return 404 when answer ID is invalid")
    public void testValidateAnswer_InvalidAnswerId() throws Exception {
        Long questionId = 11L;
        Long answerId = -1L;

        mockMvc.perform(post("/api/v1/quiz/validate-answer")
                        .param("questionId", questionId.toString())
                        .param("answerId", answerId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Adjust based on actual validation message
    }
}
