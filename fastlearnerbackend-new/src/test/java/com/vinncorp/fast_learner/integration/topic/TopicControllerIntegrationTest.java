//package com.vinncorp.fast_learner.integration.topic;
//
//import com.vinncorp.fast_learner.integration.TokenUtils;
//import com.vinncorp.fast_learner.models.course.Course;
//import com.vinncorp.fast_learner.models.section.Section;
//import com.vinncorp.fast_learner.models.video.Video;
//import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
//import com.vinncorp.fast_learner.services.section.ISectionService;
//import com.vinncorp.fast_learner.services.user.IUserService;
//import com.vinncorp.fast_learner.services.video.IVideoService;
//import com.vinncorp.fast_learner.util.Constants.APIUrls;
//import org.junit.jupiter.api.BeforeEach;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.web.servlet.MockMvc;
//import com.vinncorp.fast_learner.test_util.Constants;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import static com.vinncorp.fast_learner.util.Constants.APIUrls.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class TopicControllerIntegrationTest {
//
//    private static Long COURSE_ID = 29L;
//    private static Long TOPIC_ID = 85L;
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private IEnrollmentService enrollmentService;
//    @Autowired
//    private ISectionService sectionService;
//    @Autowired
//    private IVideoService videoService;
//    @Autowired
//    private IUserService userService;
//    private String jwtToken;
//    private static final String TOPIC = APIUrls.TOPIC;
//
//    @BeforeEach
//    public void setup() throws Exception {
//        jwtToken = TokenUtils.getToken(mockMvc);
//    }
//
//    @Test
//    @DisplayName("Fetch all topics for update successfully by section ID")
//    public void getAllTopicBySectionForUpdate_whenValidRequest_thenReturnsSuccess() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_ALL_TOPIC_BY_SECTION_FOR_UPDATE.replace("{sectionId}", Constants.SECTION_ID.toString()))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
//                .andExpect(jsonPath("$.message").value("Fetching all topics of a section."));
//    }
//
//    @Test
//    @DisplayName("Return 400 Bad Request when section ID is null")
//    public void getAllTopicBySectionForUpdate_whenSectionIdIsNull_thenReturnsBadRequest() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_ALL_TOPIC_BY_SECTION_FOR_UPDATE.replace("{sectionId}", ""))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("Return 400 Bad Request when user does not have permission to update")
//    public void getAllTopicBySectionForUpdate_whenUserHasNoPermission_thenReturnsBadRequest() throws Exception {
//        Long sectionId = 319L;
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_ALL_TOPIC_BY_SECTION_FOR_UPDATE.replace("{sectionId}", sectionId.toString()))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
//                .andExpect(jsonPath("$.message").value("This has no permission for update for this section."));
//    }
//
//    @Test
//    @DisplayName("Fetch all topics successfully by section ID")
//    public void getAllTopicsBySectionId_whenValidRequest_thenReturnsSuccess() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_ALL_TOPIC_BY_COURSE_AND_SECTION.replace("{courseId}", Constants.VALID_COURSE_ID.toString()).replace("{sectionId}", Constants.SECTION_ID.toString()))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
//    }
//
//    @Test
//    @DisplayName("Return 400 Bad Request when user is not enrolled in the course")
//    public void getAllTopicsBySectionId_whenUserNotEnrolled_thenReturnsBadRequest() throws Exception {
//        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(Constants.VALID_COURSE_ID, Constants.EMAIL);
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_ALL_TOPIC_BY_COURSE_AND_SECTION.replace("{courseId}", Constants.VALID_COURSE_ID.toString()).replace("{sectionId}", Constants.SECTION_ID.toString()))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
//                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enroll in the course first."));
//        this.enrollmentService.enrolled(Constants.VALID_COURSE_ID, Constants.EMAIL);
//    }
//
//    @Test
//    @DisplayName("Return 404 Not Found when no topics are available for the section")
//    public void getAllTopicsBySectionId_whenNoTopicsFound_thenReturnsNotFound() throws Exception {
//        Section section = new Section();
//        section.setName("Section 1");
//        section.setCourse(Course.builder().id(Constants.VALID_COURSE_ID).build());
//        Section savedSection = this.sectionService.save(section);
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_ALL_TOPIC_BY_COURSE_AND_SECTION.replace("{courseId}", Constants.VALID_COURSE_ID.toString()).replace("{sectionId}", savedSection.getId().toString()))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
//                .andExpect(jsonPath("$.message").value("No topics found for this section."));
//    }
//
//    @Test
//    @DisplayName("Fetch video summary successfully by topic ID")
//    public void getSummaryOfVideoByTopicId_whenValidRequest_thenReturnsSuccess() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_SUMMARY.replace("{topicId}", this.TOPIC_ID.toString()))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
//    }
//
//    @Test
//    @DisplayName("Return 400 Bad Request when user is not enrolled in the course")
//    public void getSummaryOfVideoByTopicId_whenUserNotEnrolled_thenReturnsBadRequest() throws Exception {
//        Video video = videoService.getVideoByTopicId(this.TOPIC_ID);
//        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(video.getTopic().getSection().getCourse().getId(), Constants.EMAIL);
//        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC + GET_SUMMARY.replace("{topicId}", this.TOPIC_ID.toString()))
//                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
//                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enrolled in the course first."));
//        this.enrollmentService.enrolled(video.getTopic().getSection().getCourse().getId(), Constants.EMAIL);
//    }
//}
