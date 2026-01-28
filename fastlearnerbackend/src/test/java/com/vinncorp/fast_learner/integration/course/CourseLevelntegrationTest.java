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
public class CourseLevelntegrationTest {


    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }


    @Test
    @DisplayName("Should successfully fetch all active course levels")
    public void testFetchAllCourseLevel_Success() throws Exception {
        // Ensure the database has active course levels

        mockMvc.perform(get("/api/v1/course-level/")
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course level fetched successfully."))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data[0].name").isNotEmpty()); // Adjust based on actual response fields
    }

    @Test
    @DisplayName("Should fetch course levels with pagination")
    public void testFetchAllCourseLevel_Pagination() throws Exception {
        // Ensure the database has active course levels

        mockMvc.perform(get("/api/v1/course-level/?page=0&size=5")
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }



    @Test
    @DisplayName("Should return 404 when course level is not found by ID")
    public void testFindById_NotFound() throws Exception {
        Long invalidCourseLevelId = 999L; // Ensure this ID does not exist in your database

        mockMvc.perform(get("/api/v1/course-level/{id}", invalidCourseLevelId)
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
