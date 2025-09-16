package com.vinncorp.fast_learner.integration.favouriteCourseCntroller;

import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.repositories.favourite_course.FavouriteCourseRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FavouriteCourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FavouriteCourseRepository favouriteCourseRepository;
    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);

    }
    @Test
    public void testCreateFavouriteCourse() throws Exception {

       // String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg"; // Replace with a valid JWT token for "testuser@example.com"
        Long courseId = 81L;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/favourite-course/")
                        .param("courseId", String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateFavouriteCourseWithInvalidToken() throws Exception {
        String invalidToken = "invalidToken";
        Long courseId = 45L;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/favourite-course/")
                        .param("courseId", String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("INVALID_CREDENTIALS"));
    }

    @Test
    public void testCreateFavouriteCourseWithNonExistentCourse() throws Exception {
      //  String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";
        Long nonExistentCourseId = 999L;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/favourite-course/")
                        .param("courseId", String.valueOf(nonExistentCourseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateFavouriteCourseWithoutCourseId() throws Exception {
      //  String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/favourite-course/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetFavouriteCourses() throws Exception {
       // String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";

        mockMvc.perform(get("/api/v1/favourite-course/")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Favourite courses fetched successfully."))
                .andExpect(jsonPath("$.data.favouriteCourses").isArray())
                .andExpect(jsonPath("$.data.pageNo").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    public void testGetFavouriteCoursesUnauthorized() throws Exception {
        String invalidToken = "invalidToken";

        mockMvc.perform(get("/api/v1/favourite-course/")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // Expecting HTTP 401 Unauthorized
    }

    @Test
    public void testGetFavouriteCoursesInvalidPageNo() throws Exception {
       // String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";

        mockMvc.perform(get("/api/v1/favourite-course/")
                        .param("pageNo", "-1")  // Invalid page number
                        .param("pageSize", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetFavouriteCoursesNoResults() throws Exception {
       // String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzcxOTk1NywiaWF0IjoxNzIzNzA3OTU3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.VWHqx3g77uR7AFXWJmen0-czQwd98sL9asulaS_bCXIwLzKgO-5Kfi9_or2wbSrarFahnPtlP-zf9CccL_dYcg";

        mockMvc.perform(get("/api/v1/favourite-course/")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .param("title", "NonExistingCourse")  // Assuming no course with this title
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // Expecting HTTP 404 Not Found
                .andExpect(jsonPath("$.data").isEmpty());  // Check that the 'data' field is empty or null
    }


}
