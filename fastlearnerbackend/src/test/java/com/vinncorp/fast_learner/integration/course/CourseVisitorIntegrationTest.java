package com.vinncorp.fast_learner.integration.course;

import com.vinncorp.fast_learner.integration.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseVisitorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Should successfully fetch all course visitors for a given course ID")
    public void testFetchAllCourseVisitors_Success() throws Exception {
        Long courseId = 29L;

        mockMvc.perform(get("/api/v1/course-visitor/")
                        .param("courseId", courseId.toString())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched all visitors successfully."));
    }
    @Test
    @DisplayName("Should successfully fetch all course visitors when courseId is not provided")
    public void testFetchAllCourseVisitors_NoCourseId() throws Exception {

        mockMvc.perform(get("/api/v1/course-visitor/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


}
