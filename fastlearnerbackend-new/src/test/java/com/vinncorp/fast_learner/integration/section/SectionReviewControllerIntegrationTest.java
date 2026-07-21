package com.vinncorp.fast_learner.integration.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.section.ISectionReviewService;
import com.vinncorp.fast_learner.services.section.ISectionService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SectionReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private IEnrollmentService enrollmentService;
    @Autowired
    private ISectionReviewService sectionReviewService;
    @Autowired
    private ISectionService sectionService;
    private String jwtToken;
    private static Long COURSE_ID = 29L;
    private static String SECTION_REVIEW = APIUrls.SECTION_REVIEW;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Fetch section review successfully")
    public void getSectionReviewBySection_whenValidRequest_thenReturnsSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SECTION_REVIEW + GET_SECTION_REVIEW.replace("{sectionId}", Constants.SECTION_ID.toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
    }

    @Test
    @DisplayName("Return 404 Not Found when section review does not exist")
    public void getSectionReviewBySection_whenReviewNotFound_thenReturnsNotFound() throws Exception {
        Section section = new Section();
        section.setName("Section 1");
        section.setCourse(Course.builder().id(Constants.VALID_COURSE_ID).build());
        Section savedSection = this.sectionService.save(section);

        mockMvc.perform(MockMvcRequestBuilders.get(SECTION_REVIEW + GET_SECTION_REVIEW.replace("{sectionId}", savedSection.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Section review is not found for the provided section."));
    }

    @Test
    @DisplayName("Create section review successfully")
    public void createSectionReview_whenValidRequest_thenReturnsSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(SECTION_REVIEW + CREATE_SECTION_REVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .content(new ObjectMapper().writeValueAsString(SectionReviewIntegrationTestData.createSectionReviewRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Section review is created successfully."));
    }

    @Test
    @DisplayName("Return 400 Bad Request when user is not enrolled")
    public void createSectionReview_whenUserNotEnrolled_thenReturnsBadRequest() throws Exception {
        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(Constants.VALID_COURSE_ID, Constants.EMAIL);
        mockMvc.perform(MockMvcRequestBuilders.post(SECTION_REVIEW + CREATE_SECTION_REVIEW)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(SectionReviewIntegrationTestData.createSectionReviewRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enroll in the course first."));
        this.enrollmentService.enrolled(Constants.VALID_COURSE_ID, Constants.EMAIL, false);
    }
}
