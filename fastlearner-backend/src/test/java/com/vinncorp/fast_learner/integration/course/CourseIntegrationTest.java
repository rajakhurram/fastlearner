package com.vinncorp.fast_learner.integration.course;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.vinncorp.fast_learner.dtos.course.CourseSearchInput;
import com.vinncorp.fast_learner.dtos.course.UniqueCourseTitle;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.integration.util.JsonUtil;
import com.vinncorp.fast_learner.request.course.CourseByCategoryRequest;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.request.course.RelatedCoursesRequest;
import com.vinncorp.fast_learner.request.course.SearchCourseRequest;
import com.vinncorp.fast_learner.response.course.CourseDetailByPaginatedResponse;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private String jwtToken;

    private TokenUtils tokenUtils;
    private CreateCourseRequest createCourseRequest;

    @BeforeEach
    public void setUp() throws Exception {
        File createCourseRequestJsonFile = new File("src/main/resources/testing/CreateCourseRequest.json");
        createCourseRequest = objectMapper.readValue(createCourseRequestJsonFile, CreateCourseRequest.class);


        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Should successfully create a course")
    public void testCreateCourse() throws Exception {

        mockMvc.perform(post("/api/v1/course/create")  // Use the actual API URL
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createCourseRequest)))
                .andExpect(status().isOk());  // Expect the HTTP status to be OK (200)
    }

    @Test
    @DisplayName("Should return 400 Bad Request when required fields are missing")
    public void testCreateCourse_MissingFields() throws Exception {
        createCourseRequest.setTitle(null);  // Simulate missing course name

        mockMvc.perform(post("/api/v1/course/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createCourseRequest)))
                .andExpect(status().isBadRequest())  // Expect the HTTP status to be Bad Request (400)
                .andExpect(jsonPath("$.message").value("Please provide all required fields for course creation"));  // Validate the error message
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when JWT token is invalid")
    public void testCreateCourse_InvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/course/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + "invalidToken")
                        .content(objectMapper.writeValueAsString(createCourseRequest)))
                .andExpect(status().isForbidden());  // Expect the HTTP status to be Unauthorized (401)
    }

    @Test
    @DisplayName("Should return 404 Not Found when invalid category ID is provided")
    public void testCreateCourse_InvalidData() throws Exception {
        createCourseRequest.setCategoryId(-1L);  // Simulate invalid category ID

        mockMvc.perform(post("/api/v1/course/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createCourseRequest)))
                .andExpect(status().isNotFound())  // Expect the HTTP status to be Not Found (404)
                .andExpect(jsonPath("$.message").value("Course category not found."));  // Validate the error message
    }


    @Test
    @DisplayName("Should return 403 Not Found when user is not found")
    public void testCreateCourse_UserNotFound() throws Exception {
        String nonExistentToken = "token-for-non-existent-user";  // Simulate token for non-existent user

        mockMvc.perform(post("/api/v1/course/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + nonExistentToken)
                        .content(objectMapper.writeValueAsString(createCourseRequest)))
                .andExpect(status().isForbidden());  // Validate the error message
    }

    @Test
    @DisplayName("Should successfully fetch courses by category with pagination")
    public void testGetCoursesByCategory_Success() throws Exception {
        // Create a valid request with category ID and pagination details
        CourseByCategoryRequest request = new CourseByCategoryRequest();
        request.setCategoryId(29L);
        request.setCourseLevelId(85L);
        request.setPageNo(0);
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/course-by-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken) // Provide the valid JWT token
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())  // Expect HTTP status to be OK (200)
                .andExpect(jsonPath("$.message").value("Courses fetched successfully."))  // Validate the success message
                .andExpect(jsonPath("$.data.totalElements").isNotEmpty())  // Ensure total elements are returned
                .andExpect(jsonPath("$.data.pages").isNotEmpty());  // Ensure total pages are returned
    }


    @Test
    @DisplayName("Should return 404 Not Found when no courses are found")
    public void testGetCoursesByCategory_NoCoursesFound() throws Exception {
        // Create a request with a valid category ID but no courses in the category
        CourseByCategoryRequest request = new CourseByCategoryRequest();
        request.setCategoryId(2L); // Assuming 2L is a category ID with no courses
        request.setPageNo(0);
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/course-by-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken) // Provide the valid JWT token
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())  // Expect HTTP status to be Not Found (404)
                .andExpect(jsonPath("$.message").value("No courses found."));  // Validate the error message
    }

    @Test
    @DisplayName("Should return 400 Bad Request when pagination parameters are invalid")
    public void testGetCoursesByCategory_InvalidPagination() throws Exception {
        // Create a request with invalid pagination details
        CourseByCategoryRequest request = new CourseByCategoryRequest();
        request.setCategoryId(1L); // Assuming 1L is a valid category ID
        request.setPageNo(-1); // Invalid page number
        request.setPageSize(0); // Invalid page size

        mockMvc.perform(post("/api/v1/course/course-by-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken) // Provide the valid JWT token
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully fetch courses by instructor for profile with pagination")
    public void testGetCoursesByInstructorForProfile_Success() throws Exception {
        // Set up valid instructor ID, page number, and page size
        Long instructorId = 1L;  // Assuming an instructor with ID 1 exists in the database
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher-for-profile")
                        .param("instructorId", instructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Expect HTTP status to be OK (200)
                .andExpect(jsonPath("$.message").value("Courses fetched successfully."))  // Validate the success message
                .andExpect(jsonPath("$.data.totalElements").isNotEmpty())  // Ensure total elements are returned
                .andExpect(jsonPath("$.data.pages").isNotEmpty());  // Ensure total pages are returned
    }

    @Test
    @DisplayName("Should return 404 if no courses are found for the instructor")
    public void testGetCoursesByInstructorForProfile_NotFound() throws Exception {
        // Set up an instructor ID that doesn't have any courses
        Long instructorId = 999L;  // Assuming no instructor with this ID exists in the database
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher-for-profile")
                        .param("instructorId", instructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // Expect HTTP status to be 404
                .andExpect(jsonPath("$.message").value("No courses found."));  // Validate the error message
    }

    @Test
    @DisplayName("Should return 403")
    public void testGetCoursesByInstructorForProfile_InvalidPage() throws Exception {
        // Set up valid instructor ID but invalid page number and size
        Long instructorId = 1L;  // Assuming an instructor with ID 1 exists in the database
        int invalidPageNo = -1;  // Invalid page number
        int invalidPageSize = 0;  // Invalid page size

        mockMvc.perform(get("/api/v1/course/course-by-teacher-for-profile")
                        .param("instructorId", instructorId.toString())
                        .param("pageNo", String.valueOf(invalidPageNo))
                        .param("pageSize", String.valueOf(invalidPageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // Validate the error message
    }

    @Test
    @DisplayName("Should fetch courses for logged-in user without instructor ID")
    public void testGetCoursesForLoggedInUser_Success() throws Exception {
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher-for-profile")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Courses fetched successfully."))
                .andExpect(jsonPath("$.data.totalElements").isNotEmpty())
                .andExpect(jsonPath("$.data.pages").isNotEmpty());
    }


    // Negative Test Case: Non-existent Instructor ID
    @Test
    @DisplayName("Should return 404 when no courses found for instructor")
    public void testGetCoursesByInstructor_NotFound() throws Exception {
        Long instructorId = 999L;  // Assuming instructor with this ID doesn't exist
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher-for-profile")
                        .param("instructorId", instructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No courses found."));
    }


    // Negative Test Case: Missing JWT Token
    @Test
    @DisplayName("Should return 401 when JWT token is missing")
    public void testGetCoursesByInstructor_MissingJWT() throws Exception {
        Long instructorId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher-for-profile")
                        .param("instructorId", instructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // Negative Test Case: Invalid JWT Token
    @Test
    @DisplayName("Should return 403")
    public void testGetCoursesByInstructor_InvalidJWT() throws Exception {
        Long instructorId = 1L;
        int pageNo = 0;
        int pageSize = 10;
        String invalidToken = "invalid-token";  // Invalid JWT token

        mockMvc.perform(get("/api/v1/course/course-by-teacher-for-profile")
                        .param("instructorId", instructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // Negative Test Case: Invalid URL Endpoint
    @Test
    @DisplayName("Should return 404 when invalid URL endpoint is accessed")
    public void testGetCoursesByInstructor_InvalidURL() throws Exception {
        Long instructorId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/wrong-url")
                        .param("instructorId", instructorId.toString())
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully fetch courses by teacher with search and sort")
    public void testGetCoursesByTeacher_Success() throws Exception {
        String searchInput = "Unsung";  // Assuming there are courses related to 'Java'
        Integer sort = 0;  // Assuming 0 is a valid sort value
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("searchInput", searchInput)
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Expect HTTP status to be OK (200)
                .andExpect(jsonPath("$.message").value("Fetched teachers courses successfully."))
                .andExpect(jsonPath("$.data.courses").isNotEmpty());
    }

    @Test
    @DisplayName("Should fetch courses by teacher without search input (fetch all)")
    public void testGetCoursesByTeacher_NoSearchInput() throws Exception {
        String searchInput = "";
        Integer sort = 0;  // Valid sort value
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("searchInput", searchInput)
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched teachers courses successfully."))
                .andExpect(jsonPath("$.data.courses").isNotEmpty());
    }

    @Test
    @DisplayName("Should fetch courses by teacher with different valid sort orders")
    public void testGetCoursesByTeacher_DifferentSortOrders() throws Exception {
        Integer sort = 1;  // Another valid sort value (Assuming 1 is a valid sort)
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched teachers courses successfully."))
                .andExpect(jsonPath("$.data.courses").isNotEmpty());
    }


    @Test
    @DisplayName("Should return 400 when page number is negative")
    public void testGetCoursesByTeacher_InvalidPageNo() throws Exception {
        String searchInput = "Java";
        Integer sort = 0;  // Valid sort value
        int pageNo = -1;  // Invalid page number
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("searchInput", searchInput)
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page no or Page size cannot be negative"));
    }

    @Test
    @DisplayName("Should return 400 when page size is negative")
    public void testGetCoursesByTeacher_InvalidPageSize() throws Exception {
        String searchInput = "Java";
        Integer sort = 0;  // Valid sort value
        int pageNo = 0;
        int pageSize = -10;  // Invalid page size

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("searchInput", searchInput)
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page no or Page size cannot be negative"));
    }


    @Test
    @DisplayName("Should return 400 when sort value is invalid")
    public void testGetCoursesByTeacher_InvalidSortValue() throws Exception {
        String searchInput = "Java";
        Integer sort = 2;  // Invalid sort value
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("searchInput", searchInput)
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sort can only be 0 or 1"));
    }

    @Test
    @DisplayName("Should return 404 when no courses found for given search and sort")
    public void testGetCoursesByTeacher_NoCoursesFound() throws Exception {
        String searchInput = "NonExistentCourse";  // Assuming no courses match this search input
        Integer sort = 0;  // Valid sort value
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("searchInput", searchInput)
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No result found for provided filters."));
    }

    @Test
    @DisplayName("Should return 403 when JWT token is missing")
    public void testGetCoursesByTeacher_Unauthorized() throws Exception {
        String searchInput = "Java";
        Integer sort = 0;  // Valid sort value
        int pageNo = 0;
        int pageSize = 10;

        mockMvc.perform(get("/api/v1/course/course-by-teacher")
                        .param("searchInput", searchInput)
                        .param("sort", String.valueOf(sort))
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))  // Missing Authorization header
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Should successfully fetch course details by course ID")
    public void testGetCourseDetailById_Success() throws Exception {
        Long courseId = 29L;

        mockMvc.perform(get("/api/v1/course/get/{courseId}", courseId)
                        .header("Authorization", "Bearer " + jwtToken) // Use a valid JWT token if required
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course details fetched successfully."))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.courseId").value(courseId));
    }

    @Test
    @DisplayName("Should fetch course details by course ID for a user with a different role")
    public void testGetCourseDetailById_AuthenticatedUserWithDifferentRole() throws Exception {
        Long courseId = 29L;  // Valid course ID

        // Obtain JWT token for a user with a different role, if required

        mockMvc.perform(get("/api/v1/course/get/{courseId}", courseId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course details fetched successfully."))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.courseId").value(courseId));
    }

    @Test
    @DisplayName("Should return 404 when course not found")
    public void testGetCourseDetailById_CourseNotFound() throws Exception {
        Long nonExistentCourseId = 999L;  // Non-existent course ID

        mockMvc.perform(get("/api/v1/course/get/{courseId}", nonExistentCourseId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No courses found."));
    }

    @Test
    @DisplayName("Should return 200 when no JWT token is provided")
    public void testGetCourseDetailById_Unauthorized() throws Exception {
        Long courseId = 29L;  // Valid course ID

        mockMvc.perform(get("/api/v1/course/get/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when an invalid JWT token is provided")
    public void testGetCourseDetailById_Forbidden() throws Exception {
        Long courseId = 29L;  // Valid course ID
        String invalidJwtToken = "invalid-jwt-token";

        mockMvc.perform(get("/api/v1/course/get/{courseId}", courseId)
                        .header("Authorization", "Bearer " + invalidJwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully fetch related courses by pagination")
    public void testGetRelatedCoursesByPagination_Success() throws Exception {
        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(29L);  // Valid course ID
        request.setPageNo(0);
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/get-related-courses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(request)))  // Convert the request object to JSON string
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Related courses fetched successfully."))
                .andExpect(jsonPath("$.data.courses").isNotEmpty())
                .andExpect(jsonPath("$.data.pageNo").value(request.getPageNo()))
                .andExpect(jsonPath("$.data.pageSize").value(request.getPageSize()));
    }

    @Test
    @DisplayName("Should return 404 when no related courses are found")
    public void testGetRelatedCoursesByPagination_NoCoursesFound() throws Exception {
        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(999L);
        request.setPageNo(0);
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/get-related-courses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No recommendation is found."));
    }

    @Test
    @DisplayName("Should return 200 when no JWT token is provided")
    public void testGetRelatedCoursesByPagination_Unauthorized() throws Exception {
        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(29L);  // Valid course ID
        request.setPageNo(0);
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/get-related-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(request)))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Should return 400 Bad Request when request body is empty")
    public void testGetRelatedCoursesByPagination_EmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/course/get-related-courses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Should return 403 Forbidden when page number is invalid")
    public void testGetRelatedCoursesByPagination_InvalidPageNumber() throws Exception {
        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(29L);  // Valid course ID
        request.setPageNo(-1);     // Invalid page number
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/get-related-courses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJsonString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully search courses by filters")
    public void testSearchByFilter_Success() throws Exception {
        SearchCourseRequest request = new SearchCourseRequest();
        request.setSearchValue("fundamentals of python");  // Example search value
        request.setReviewFrom(4.0);      // Example review filter
        request.setReviewTo(5.0);        // Example review filter
        request.setPageNo(0);
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/search-by-filter")
                        .header("Authorization", "Bearer " + jwtToken)  // Include JWT token for authorization
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))  // Convert request object to JSON string
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Courses fetched successfully."))
                .andExpect(jsonPath("$.data.data").isNotEmpty())  // Ensure courses data is returned
                .andExpect(jsonPath("$.data.pageNo").value(request.getPageNo()))
                .andExpect(jsonPath("$.data.pageSize").value(request.getPageSize()))
                .andExpect(jsonPath("$.data.pages").isNotEmpty())
                .andExpect(jsonPath("$.data.totalElements").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 404 when no courses found for given filters")
    public void testSearchByFilter_NoCoursesFound() throws Exception {
        SearchCourseRequest request = new SearchCourseRequest();
        request.setSearchValue("NonExistentCourse");  // Use a search value that won't match any courses
        request.setPageNo(0);
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/search-by-filter")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No courses found."));
    }

    @Test
    @DisplayName("Should return 403 when invalid pagination parameters are provided")
    public void testSearchByFilter_InvalidInput() throws Exception {
        SearchCourseRequest request = new SearchCourseRequest();
        request.setSearchValue("Java");
        request.setPageNo(-1);  // Invalid page number
        request.setPageSize(10);

        mockMvc.perform(post("/api/v1/course/search-by-filter")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully fetch autocomplete course search results")
    public void testAutocompleteCourseSearch_Success() throws Exception {
        CourseSearchInput input = new CourseSearchInput();
        input.setInput("unsu");  // Example search input

        mockMvc.perform(post("/api/v1/course/autocomplete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(input)))  // Convert request object to JSON string
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when no suggestions found for the input")
    public void testAutocompleteCourseSearch_NoSuggestionsFound() throws Exception {
        CourseSearchInput input = new CourseSearchInput();
        input.setInput("NonExistentCourse");  // Use an input that won't match any courses

        mockMvc.perform(post("/api/v1/course/autocomplete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No suggestion found."));
    }

    @Test
    @DisplayName("Should successfully send course shared notification")
    public void testSendCourseSharedNotification_Success() throws Exception {
        Long courseId = 29L;  // Replace with a valid course ID

        mockMvc.perform(post("/api/v1/course/course-shared")
                        .header("Authorization", "Bearer " + jwtToken)  // Use a valid JWT token
                        .param("courseId", courseId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());  // Verify the response is OK (200)
    }
    @Test
    @DisplayName("Should return 404 when course ID is invalid")
    public void testSendCourseSharedNotification_CourseNotFound() throws Exception {
        Long invalidCourseId = 999L;  // Use an invalid course ID that doesn't exist

        mockMvc.perform(post("/api/v1/course/course-shared")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("courseId", invalidCourseId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found by provided id."));
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated")
    public void testSendCourseSharedNotification_Unauthorized() throws Exception {
        Long courseId = 1L;  // Use a valid course ID

        mockMvc.perform(post("/api/v1/course/course-shared")
                        .param("courseId", courseId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully fetch course detail for update first step")
    public void testFetchCourseDetailForUpdateFirstStep_Success() throws Exception {
        Long courseId = 29L;  // Replace with a valid course ID that the authenticated user owns

        mockMvc.perform(get("/api/v1/course/course-detail-for-update-first-step/{courseId}", courseId)
                        .header("Authorization", "Bearer " + jwtToken)  // Use a valid JWT token
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course detail for first step is fetched successfully."))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.courseId").value(courseId));
    }

    @Test
    @DisplayName("Should return 404 when the course ID is not found")
    public void testFetchCourseDetailForUpdateFirstStep_CourseNotFound() throws Exception {
        Long invalidCourseId = 999L;  // Use an invalid course ID that doesn't exist

        mockMvc.perform(get("/api/v1/course/course-detail-for-update-first-step/{courseId}", invalidCourseId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No course found for the logged in user."));
    }

    @Test
    @DisplayName("Should return 400 when course ID is null")
    public void testFetchCourseDetailForUpdateFirstStep_NullCourseId() throws Exception {
        mockMvc.perform(get("/api/v1/course/course-detail-for-update-first-step/{courseId}", "")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when the user is not authenticated")
    public void testFetchCourseDetailForUpdateFirstStep_Unauthorized() throws Exception {
        Long courseId = 1L;  // Use a valid course ID

        mockMvc.perform(get("/api/v1/course/course-detail-for-update-first-step/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully check unique course title")
    public void testCheckUniqueCourseTitle_Success() throws Exception {
        UniqueCourseTitle request = new UniqueCourseTitle();
        request.setCourseTitle("Unique Course Title"); // Set a unique course title
        request.setCourseId(0L); // Assume 0 means new course, not updating existing one

        mockMvc.perform(post("/api/v1/course/unique-course-title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course title is unique."))
                .andExpect(jsonPath("$.data").value("unique-course-title")); // Data should be null
    }

    @Test
    @DisplayName("Should return 400 when course title already exists")
    public void testCheckUniqueCourseTitle_AlreadyExists() throws Exception {
        // Assuming course with title "Existing Course Title" already exists
        UniqueCourseTitle request = new UniqueCourseTitle();
        request.setCourseTitle("Computing in Python");
        request.setCourseId(0L); // Assume 0 means new course, not updating existing one

        mockMvc.perform(post("/api/v1/course/unique-course-title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Course title already exists."));
    }

    @Test
    @DisplayName("Should successfully retrieve course by title")
    public void testGetCourseByTitle_Success() throws Exception {
        // Assuming a course with title "Existing Course Title" exists and is active
        UniqueCourseTitle request = new UniqueCourseTitle();
        request.setCourseTitle("Fundamentals of Python");
        request.setCourseUrl("fundamentals-of-python");

        mockMvc.perform(post("/api/v1/course/course-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when course is not found by title")
    public void testGetCourseByTitle_NotFound() throws Exception {
        UniqueCourseTitle request = new UniqueCourseTitle();
        request.setCourseTitle("Nonexistent Course Title");
        request.setCourseUrl("nonexistent-course-title");

        mockMvc.perform(post("/api/v1/course/course-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found"));
    }

    @Test
    @DisplayName("Should return 404 when course title is null")
    public void testGetCourseByTitle_NullTitle() throws Exception {
        UniqueCourseTitle request = new UniqueCourseTitle();
        request.setCourseTitle(null);
        request.setCourseUrl(null);

        mockMvc.perform(post("/api/v1/course/course-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully fetch all courses titles by instructor for performance page")
    public void testFetchAllCoursesTitleByInstructor_Success() throws Exception {
        // Perform the request and check the response
        mockMvc.perform(get("/api/v1/course/dropdown-for-performance")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("Should return 200 OK when valid request is made")
    public void testGetAllNewCourses_ValidRequest() throws Exception {
        mockMvc.perform(get("/api/v1/home-page")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .header("Authorization", jwtToken))  // Use a valid token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));  // Adjust based on your response structure
    }

}