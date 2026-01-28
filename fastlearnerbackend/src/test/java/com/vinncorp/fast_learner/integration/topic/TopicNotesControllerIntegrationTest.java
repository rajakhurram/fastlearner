package com.vinncorp.fast_learner.integration.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.topic.TopicNotes;
import com.vinncorp.fast_learner.repositories.topic.TopicNotesRepository;
import com.vinncorp.fast_learner.request.topic.CreateUpdateTopicNotesRequest;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.topic.ITopicNotesService;
import com.vinncorp.fast_learner.services.topic.TopicService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.test_util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TopicNotesControllerIntegrationTest {

    private static Long COURSE_ID = 98L;
    private static Long TOPIC_ID = 837L;
    @Autowired
    IEnrollmentService enrollmentService;
    @Autowired
    ITopicNotesService topicNotesService;
    @Autowired
    TopicNotesRepository topicNotesRepository;
    @Autowired
    TopicService topicService;
    @Autowired
    IUserService userService;
    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;
    private static final String TOPIC_NOTES = APIUrls.TOPIC_NOTES;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Delete topic notes successfully")
    public void deleteTopicNotes_whenValidRequest_thenReturnsSuccess() throws Exception {
        Topic topic = topicService.getTopicById(TOPIC_ID);
        TopicNotes topicNotes = new TopicNotes();
        topicNotes.setNote("note");
        topicNotes.setTime("00:05:00");
        topicNotes.setCreatedBy(userService.findByEmail(Constants.EMAIL).getId());
        TopicNotes notes = topicNotesRepository.save(topicNotes);
        mockMvc.perform(MockMvcRequestBuilders.delete(TOPIC_NOTES + DELETE_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("topicNoteId", notes.getId().toString())
                        .param("topicId", topic.getId().toString())
                        .param("courseId", COURSE_ID.toString()))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Topic note is deleted successfully."));
    }

    @Test
    @DisplayName("Return 404 Not Found when topic notes ID is invalid")
    public void deleteTopicNotes_whenInvalidTopicNoteId_thenReturnsNotFound() throws Exception {
        Topic topic = topicService.getTopicById(TOPIC_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(TOPIC_NOTES + DELETE_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("topicNoteId", Constants.INVALID_ID.toString())
                        .param("topicId", topic.getId().toString())
                        .param("courseId", COURSE_ID.toString()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Topic note is not found for the user."));
    }

    @Test
    @DisplayName("Return 400 Bad Request when user is not enrolled in the course")
    public void deleteTopicNotes_whenUserNotEnrolled_thenReturnsBadRequest() throws Exception {
        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(COURSE_ID, Constants.EMAIL);
        TopicNotes topicNotes = new TopicNotes();
        topicNotes.setNote("note");
        topicNotes.setTime("00:05:00");
        topicNotes.setCreatedBy(userService.findByEmail(Constants.EMAIL).getId());
        TopicNotes notes = topicNotesRepository.save(topicNotes);
        mockMvc.perform(MockMvcRequestBuilders.delete(TOPIC_NOTES + DELETE_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("topicNoteId", notes.getId().toString())
                        .param("topicId", TOPIC_ID.toString())
                        .param("courseId", COURSE_ID.toString()))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enroll in the course first."));
        this.enrollmentService.enrolled(COURSE_ID, Constants.EMAIL, false);
    }

    @Test
    @DisplayName("Fetch all topic notes successfully")
    public void getTopicNotes_whenValidRequest_thenReturnsSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC_NOTES + GET_ALL_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("courseId", String.valueOf(COURSE_ID))
                        .param("pageNo", "0")
                        .param("pageSize", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Fetched all topic's notes successfully."));
    }

    @Test
    @DisplayName("Return 404 Not Found when no topic notes are found")
    public void getTopicNotes_whenNoNotesFound_thenReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC_NOTES + GET_ALL_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("courseId", String.valueOf(Constants.INVALID_COURSE_ID))
                        .param("pageNo", "0")
                        .param("pageSize", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("No topic notes present for the user."));
    }

    @Test
    @DisplayName("Return 400 Bad Request when course ID is missing")
    public void getTopicNotes_whenCourseIdIsMissing_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC_NOTES + GET_ALL_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("pageNo", "0")
                        .param("pageSize", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create new topic notes successfully")
    public void createTopicNotes_whenValidRequest_thenReturnsSuccess() throws Exception {
        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(COURSE_ID, Constants.EMAIL);
        this.enrollmentService.enrolled(COURSE_ID, Constants.EMAIL, false);
        mockMvc.perform(MockMvcRequestBuilders.post(TOPIC_NOTES + CREATE_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(TopicNotesTestData.createUpdateTopicNotesRequest(COURSE_ID, TOPIC_ID))))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Successfully created/update a note."));
    }

    @Test
    @DisplayName("Update existing topic notes successfully")
    public void updateTopicNotes_whenValidRequest_thenReturnsSuccess() throws Exception {

        Topic topic = topicService.getTopicById(TOPIC_ID);
        TopicNotes notes = topicNotesRepository.save(TopicNotes.builder()
                .note("note")
                .time("00:05:00")
                .topic(topic).build());

        CreateUpdateTopicNotesRequest createUpdateTopicNotesRequest = TopicNotesTestData.createUpdateTopicNotesRequest(COURSE_ID, TOPIC_ID);
        createUpdateTopicNotesRequest.setTopicNotesId(notes.getId());

        mockMvc.perform(MockMvcRequestBuilders.post(TOPIC_NOTES + CREATE_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createUpdateTopicNotesRequest)))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Successfully created/update a note."));
    }

    @Test
    @DisplayName("Return 404 Not Found when topic notes ID is invalid during update")
    public void updateTopicNotes_whenInvalidTopicNotesId_thenReturnsNotFound() throws Exception {

        CreateUpdateTopicNotesRequest createUpdateTopicNotesRequest = TopicNotesTestData.createUpdateTopicNotesRequest(COURSE_ID, TOPIC_ID);
        createUpdateTopicNotesRequest.setTopicNotesId(-1L);

        mockMvc.perform(MockMvcRequestBuilders.post(TOPIC_NOTES + CREATE_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createUpdateTopicNotesRequest)))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Provided topic note id is not valid."));
    }

    @Test
    @DisplayName("Return 400 Bad Request when user is not enrolled in the course")
    public void createOrUpdateTopicNotes_whenUserNotEnrolled_thenReturnsBadRequest() throws Exception {
        this.enrollmentService.deleteEnrollmentByCourseIdAndStudentId(COURSE_ID, Constants.EMAIL);
        mockMvc.perform(MockMvcRequestBuilders.post(TOPIC_NOTES + CREATE_TOPIC_NOTES)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(TopicNotesTestData.createUpdateTopicNotesRequest(COURSE_ID, TOPIC_ID))))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("You are not enrolled in this course please enroll in the course first."));
        this.enrollmentService.enrolled(COURSE_ID, Constants.EMAIL, false);
    }
}
