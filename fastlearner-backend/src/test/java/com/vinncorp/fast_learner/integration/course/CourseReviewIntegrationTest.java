package com.vinncorp.fast_learner.integration.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.request.course.CreateCourseReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseReviewIntegrationTest {


    @Autowired
    private MockMvc mockMvc;@Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Should successfully create a new course review")
    public void testCreateReview_Success() throws Exception {
        CreateCourseReviewRequest request = new CreateCourseReviewRequest();
        request.setCourseId(29L); // Ensure this course ID exists in your test database
        request.setComment("Great course!");
        request.setValue(5); // Assume the rating is out of 5

        mockMvc.perform(post("/api/v1/course-review/")
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review is given successfully."))
                .andExpect(jsonPath("$.data").value("Review is given successfully."));
    }
    @Test
    @DisplayName("Should successfully update an existing course review")
    public void testUpdateReview_Success() throws Exception {
        CreateCourseReviewRequest request = new CreateCourseReviewRequest();
        request.setCourseId(29L); // Ensure this course ID exists and has an existing review
        request.setComment("Updated review!");
        request.setValue(4); // Assume the rating is out of 5

        mockMvc.perform(post("/api/v1/course-review/")
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review is given successfully."))
                .andExpect(jsonPath("$.data").value("Review is given successfully."));
    }

    @Test
    @DisplayName("Should return 400 when the user is not enrolled in the course")
    public void testCreateReview_NotEnrolled() throws Exception {
        CreateCourseReviewRequest request = new CreateCourseReviewRequest();
        request.setCourseId(77L); // Ensure this course ID exists
        request.setComment("Good course!");
        request.setValue(5);

        mockMvc.perform(post("/api/v1/course-review/")
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enroll in the course first."));
    }

    @Test
    @DisplayName("Should return 404 when the course is not found")
    public void testCreateReview_CourseNotFound() throws Exception {
        CreateCourseReviewRequest request = new CreateCourseReviewRequest();
        request.setCourseId(999L); // Ensure this course ID does not exist
        request.setComment("Good course!");
        request.setValue(5);

        mockMvc.perform(post("/api/v1/course-review")
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully fetch the course review")
    public void testFindCourseReview_Success() throws Exception {
        Long courseId = 29L;

        mockMvc.perform(get("/api/v1/course-review/{courseId}", courseId)
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course review fetched successfully."))
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.comment").isNotEmpty())
                .andExpect(jsonPath("$.data.value").isNumber())
                .andExpect(jsonPath("$.data.totalReviews").isNumber());
    }

    @Test
    @DisplayName("Should successfully fetch the course review when there are multiple reviews for the course")
    public void testFindCourseReview_WithMultipleReviews() throws Exception {
        Long courseId = 29L;

        mockMvc.perform(get("/api/v1/course-review/{courseId}", courseId)
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course review fetched successfully."))
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.comment").isNotEmpty())
                .andExpect(jsonPath("$.data.value").isNumber())
                .andExpect(jsonPath("$.data.totalReviews").isNumber());
    }

    @Test
    @DisplayName("Should return 400 when the user is not enrolled in the course")
    public void testFindCourseReview_UserNotEnrolled() throws Exception {
        Long courseId = 1L;

        mockMvc.perform(get("/api/v1/course-review/{courseId}", courseId)
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enroll in the course first."));
    }

    @Test
    @DisplayName("Should successfully fetch all reviews for a course")
    public void testGetAllReviewsOfCourses_Success() throws Exception {
        Long courseId = 29L; // Ensure this course ID exists and has reviews in the test database
        int pageNo = 0; // Page number
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course-review/")
                        .param("courseId", courseId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should successfully fetch all reviews for a course with pagination")
    public void testGetAllReviewsOfCourses_Pagination() throws Exception {
        Long courseId = 29L; // Ensure this course ID exists and has reviews in the test database
        int pageNo = 1; // Page number
        int pageSize = 5;

        mockMvc.perform(get("/api/v1/course-review/")
                        .param("courseId", courseId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Should return 404 when no reviews are found for the course")
    public void testGetAllReviewsOfCourses_NoReviewsFound() throws Exception {
        Long courseId = 999L; // Ensure this course ID does not have reviews in the test database
        int pageNo = 0; // Page number
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course-review/")
                        .param("courseId", courseId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully fetch all reviews for courses by instructor")
    public void testGetAllReviewsOfCoursesByInstructor_Success() throws Exception {

        Long courseId = 29L;
        int pageNo = 0; // Page number
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course-review/instructor")
                        .param("courseId", courseId != null ? courseId.toString() : "")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Should successfully fetch all reviews for all courses by instructor with pagination")
    public void testGetAllReviewsOfCoursesByInstructor_Pagination() throws Exception {
        Long courseId = null; // Optional
        int pageNo = 1; // Page number
        int pageSize = 5;

        mockMvc.perform(get("/api/v1/course-review/instructor")
                        .param("courseId", courseId != null ? courseId.toString() : "")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Should return 404 when no reviews are found for the instructor")
    public void testGetAllReviewsOfCoursesByInstructor_NoReviewsFound() throws Exception {
        Long courseId = 999L; // Ensure this course ID does not have reviews
        int pageNo = 0; // Page number
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course-review/instructor")
                        .param("courseId", courseId != null ? courseId.toString() : "")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when the page number is invalid")
    public void testGetAllReviewsOfCoursesByInstructor_InvalidPageNumber() throws Exception {
        Long courseId = 1L; // Optional
        int pageNo = -1; // Invalid page number
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course-review/instructor")
                        .param("courseId", courseId != null ? courseId.toString() : "")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Should return 400 when the page size is invalid")
    public void testGetAllReviewsOfCoursesByInstructor_InvalidPageSize() throws Exception {
        String email = "instructor@example.com"; // Ensure this email corresponds to an existing instructor in the test database
        Long courseId = 1L; // Optional
        int pageNo = 0; // Page number
        int pageSize = -5;

        mockMvc.perform(get("/api/v1/course-review/instructor")
                        .param("courseId", courseId != null ? courseId.toString() : "")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
//
//    @Test
//    @DisplayName("Should successfully like a review")
//    public void testLikeReview_Success() throws Exception {
//        Long courseReviewId = 4L; // Ensure this review ID exists in the test database
//        String status = "LIKED";
//        mockMvc.perform(post("/api/v1/course-review/like/{courseReviewId}/{status}", courseReviewId, status)
//                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Course review is liked successfully."))
//                .andExpect(jsonPath("$.data").value("Course review is liked successfully."));
//    }

//    @Test
//    @DisplayName("Should successfully dislike a review")
//    public void testDislikeReview_Success() throws Exception {
//        Long courseReviewId = 58L; // Ensure this review ID exists in the test database
//        String status = "DISLIKED";
//
//        mockMvc.perform(post("/api/v1/course-review/like/{courseReviewId}/{status}", courseReviewId, status)
//                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Course review is disliked successfully."))
//                .andExpect(jsonPath("$.data").value("Course review is disliked successfully."));
//    }

    @Test
    @DisplayName("Should return 400 when the user tries to like or dislike a review they have already liked or disliked")
    public void testLikeReview_AlreadyLiked() throws Exception {
        Long courseReviewId = 1L; // Ensure this review ID exists in the test database and is already liked by the user
        String status = "LIKED";

        mockMvc.perform(post("/api/v1/course-review/like/{courseReviewId}/{status}", courseReviewId, status)
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This user already LIKED the course review."));
    }

    @Test
    @DisplayName("Should return 400 when the user tries to dislike a review they have already liked or disliked")
    public void testDislikeReview_AlreadyDisliked() throws Exception {
        Long courseReviewId = 58L; // Ensure this review ID exists in the test database and is already disliked by the user
        String status = "DISLIKED";

        mockMvc.perform(post("/api/v1/course-review/like/{courseReviewId}/{status}", courseReviewId, status)
                        .header("Authorization", "Bearer " + jwtToken) // Include JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This user already DISLIKED the course review."));
    }



}
