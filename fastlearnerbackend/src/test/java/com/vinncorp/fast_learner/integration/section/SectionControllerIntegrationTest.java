package com.vinncorp.fast_learner.integration.section;

import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.integration.course.CourseIntegrationTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.test_util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.GET_SECTION;
import static com.vinncorp.fast_learner.util.Constants.APIUrls.GET_SECTION_FOR_UPDATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SectionControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private IEnrollmentService enrollmentService;
    @Autowired
    private ICourseService courseService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ISubscribedUserService subscribedUserService;
    @Autowired
    private ISubscriptionService subscriptionService;
    private String jwtToken;
    private static Long COURSE_ID = 29L;
    private static String SECTION = APIUrls.SECTION;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Fetch all sections for update successfully")
    public void fetchAllSectionForUpdate_whenValidCourseAndUser_thenReturnsSectionsForUpdate() throws Exception {
        mockMvc.perform(get(SECTION + GET_SECTION_FOR_UPDATE.replace("{courseId}", this.COURSE_ID.toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Successfully fetched all sections for update."));
    }

    @Test
    @DisplayName("Return 400 Bad Request when course ID is null")
    public void fetchAllSectionForUpdate_whenCourseIdIsNull_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get(SECTION + GET_SECTION_FOR_UPDATE.replace("{courseId}", ""))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Return 404 Not Found when user cannot access the course")
    public void fetchAllSectionForUpdate_whenUserCannotAccessCourse_thenReturnsNotFound() throws Exception {
        Course course = courseRepository.save(CourseIntegrationTestData.courseData());
        mockMvc.perform(get(SECTION + GET_SECTION_FOR_UPDATE.replace("{courseId}", course.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("The user cannot access this course."));
    }

    @Test
    @DisplayName("Fetch all sections by course ID successfully")
    public void fetchAllSectionsByCourseId_whenValidCourseAndEnrolledUser_thenReturnsSections() throws Exception {
        mockMvc.perform(get(SECTION + GET_SECTION.replace("{courseId}", Constants.VALID_COURSE_ID.toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Fetched all sections by course and user successfully."));
    }

    @Test
    @DisplayName("Return 400 Bad Request when user is not enrolled in the course")
    public void fetchAllSectionsByCourseId_whenUserNotEnrolled_thenReturnsBadRequest() throws Exception {
        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(Constants.VALID_COURSE_ID, Constants.EMAIL);
        mockMvc.perform(get(SECTION + GET_SECTION.replace("{courseId}", Constants.VALID_COURSE_ID.toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enrolled in the course first."));
        this.enrollmentService.enrolled(Constants.VALID_COURSE_ID, Constants.EMAIL, false);
    }

    @Test
    @DisplayName("Return 404 Not Found when the course does not exist")
    public void fetchAllSectionsByCourseId_whenCourseNotFound_thenReturnsNotFound() throws Exception {
        mockMvc.perform(get(SECTION + GET_SECTION.replace("{courseId}", Constants.INVALID_COURSE_ID.toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Course not found by course id: "+ Constants.INVALID_COURSE_ID));
    }

    @Test
    @DisplayName("Return 404 Not Found when no sections are found for the course")
    public void fetchAllSectionsByCourseId_whenNoSectionsFound_thenReturnsNotFound() throws Exception {
        Course course = courseRepository.save(CourseIntegrationTestData.courseData());
        enrollmentService.enrolled(course.getId(), Constants.EMAIL, false);

        mockMvc.perform(get(SECTION + GET_SECTION.replace("{courseId}", course.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Sections not found."));
    }

}
