package com.vinncorp.fast_learner.integration.entrollment;


import com.vinncorp.fast_learner.integration.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class EnrollmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);

    }
//    @Test
//    public void testEnrolled_whenValidRequest_thenReturnsSuccess() throws Exception {
//        //String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyNDA3OTQ2OCwiaWF0IjoxNzI0MDY3NDY4LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.hSE3NoWMA43I_p7i-aBmaUY-IJk8MrA0QUk5T045YU4YlPZg5FzT_Gg7kHJZy-tStWKJeCDiWw6yETAuhoxleQ";
//        Long courseId = 61L; // Use a valid course ID from your test data
//        String url = "/api/v1/enrollment/";
//
//        mockMvc.perform(MockMvcRequestBuilders.post(url)
//                        .param("courseId", String.valueOf(courseId))
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200));
//    }


    //negative
    @Test
    public void testEnrolled_whenInValidRequest_thenReturnsBadRequest() throws Exception {
        //String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzU1NTEzOCwiaWF0IjoxNzIzNTQzMTM4LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.LH5C6Yr4_hFnieUJm0XHwumlRt46KTbGcma7JAucw8_Fhiq_3lKG07f78mgZP_jwPzmIxlvpbLq0SVEVkh-HbA";
        Long courseId = 61L; // Use a valid course ID from your test data
        String url = "/api/v1/enrollment/";

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .param("courseId", String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    public void testEnrolled_whenInvalidToken_thenReturnsUnauthorized() throws Exception {
        String invalidJwtToken = "InvalidToken";
        Long courseId = 61L;
        String url = "/api/v1/enrollment/";

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .param("courseId", String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidJwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testEnrolled_whenInvalidCourseId_thenReturnsNotFound() throws Exception {
       // String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzU1NTEzOCwiaWF0IjoxNzIzNTQzMTM4LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.LH5C6Yr4_hFnieUJm0XHwumlRt46KTbGcma7JAucw8_Fhiq_3lKG07f78mgZP_jwPzmIxlvpbLq0SVEVkh-HbA";
        Long invalidCourseId = 999L; // Assume 999L is an invalid course ID
        String url = "/api/v1/enrollment/";

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .param("courseId", String.valueOf(invalidCourseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testEnrolled_whenMissingCourseId_thenReturnsBadRequest() throws Exception {
      //  String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzU1NTEzOCwiaWF0IjoxNzIzNTQzMTM4LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.LH5C6Yr4_hFnieUJm0XHwumlRt46KTbGcma7JAucw8_Fhiq_3lKG07f78mgZP_jwPzmIxlvpbLq0SVEVkh-HbA";
        String url = "/api/v1/enrollment/";

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    public void testGetAllEnrolledCoursesWithToken() throws Exception {
      //  String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyNDA3NjUwNCwiaWF0IjoxNzI0MDY0NTA0LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.-gxgDg-phGV9QE12aLvKydboWMK0TPEnkiKPJk_YawcaXieSqCP2bJ8qzLAKdEp7o8Z7yhmSo8oVgWxAHiJu4Q";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .param("sortBy", "1") // adjust params as needed
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.myCourses").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("My courses fetched successfully."));
    }

    @Test
    public void testGetAllEnrolledCoursesWithInvalidToken() throws Exception {
        String invalidToken = "Bearer invalid_token";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/")
                        .header(HttpHeaders.AUTHORIZATION, invalidToken)
                        .param("sortBy", "1")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("INVALID_CREDENTIALS"));
    }


    @Test
    public void testGetAllEnrolledCoursesWithExpiredToken() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzU1NTEzOCwiaWF0IjoxNzIzNTQzMTM4LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.LH5C6Yr4_hFnieUJm0XHwumlRt46KTbGcma7JAucw8_Fhiq_3lKG07f78mgZP_jwPzmIxlvpbLq0SVEVkh-HbA";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/")
                        .header(HttpHeaders.AUTHORIZATION, expiredToken)
                        .param("sortBy", "1")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("JWT Token is invalid"));
    }

    @Test
    public void testGetAllEnrolledCoursesWithNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/")
                        .param("sortBy", "1")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("JWT Token is invalid"));
    }
//
//    @Test
//    public void testGetAllEnrolledCoursesWithInvalidSortBy() throws Exception {
//        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/")
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                        .param("sortBy", "-1") // Invalid sortBy value
//                        .param("pageNo", "0")
//                        .param("pageSize", "10")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Sort By parameter should not be null"));
//    }

    @Test
    public void testGetAllEnrolledCoursesWithMissingParameters() throws Exception {
     //   String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());}

    @Test
    public void testGetAllEnrolledCoursesWithNonExistentPage() throws Exception {
        //String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollment/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .param("sortBy", "1")
                        .param("pageNo", "1000") // Non-existent page number
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}