package com.vinncorp.fast_learner.integration.section;

import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
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

import static com.vinncorp.fast_learner.util.Constants.APIUrls.GET_ALL_ALTERNATE_SECTIONS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AlternateSectionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private IEnrollmentService enrollmentService;
    private static Long COURSE_ID = 98L;
    private static Long SECTION_ID = 361L;
    private String jwtToken;
    private static String ALTERNATE_SECTION = APIUrls.ALTERNATE_SECTION;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Fetch alternate sections successfully")
    public void fetchAlternateSection_whenValidRequest_thenReturnsSuccess() throws Exception {
        mockMvc.perform(get(ALTERNATE_SECTION + GET_ALL_ALTERNATE_SECTIONS)
                        .param("courseId", String.valueOf(COURSE_ID))
                        .param("sectionId", String.valueOf(SECTION_ID))
                        .param("pageNo", "0")
                        .param("pageSize", "5")
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
    }

    @Test
    @DisplayName("Return 400 Bad Request when user is not enrolled")
    public void fetchAlternateSection_whenUserNotEnrolled_thenReturnsBadRequest() throws Exception {
        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(COURSE_ID, Constants.EMAIL);
        mockMvc.perform(get(ALTERNATE_SECTION + GET_ALL_ALTERNATE_SECTIONS)
                        .param("courseId", String.valueOf(COURSE_ID))
                        .param("sectionId", String.valueOf(SECTION_ID))
                        .param("pageNo", "0")
                        .param("pageSize", "5")
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enrolled in the course first."));
        this.enrollmentService.enrolled(COURSE_ID, Constants.EMAIL, false);
    }

    @Test
    @DisplayName("Return 404 Not Found when no alternate sections are found")
    public void fetchAlternateSection_whenNoAlternateSectionsFound_thenReturnsNotFound() throws Exception {
        Long sectionId = 363L;
        mockMvc.perform(get(ALTERNATE_SECTION + GET_ALL_ALTERNATE_SECTIONS)
                        .param("courseId", String.valueOf(COURSE_ID))
                        .param("sectionId", String.valueOf(sectionId))
                        .param("pageNo", "0")
                        .param("pageSize", "5")
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Alternate section not found."));
    }

}
