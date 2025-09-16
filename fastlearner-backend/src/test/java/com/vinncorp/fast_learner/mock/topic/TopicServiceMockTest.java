package com.vinncorp.fast_learner.mock.topic;

import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.section.SectionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnswer;
import com.vinncorp.fast_learner.dtos.topic.NoOfTopicInCourse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.repositories.enrollment.EnrollmentRepository;
import com.vinncorp.fast_learner.repositories.topic.TopicRepository;
import com.vinncorp.fast_learner.response.topic.TopicDetailForUpdateResponse;
import com.vinncorp.fast_learner.response.topic.TopicDetailResponse;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.quiz.QuizQuestionAnswerService;
import com.vinncorp.fast_learner.services.section.SectionService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.services.topic.TopicService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.services.video.VideoService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.ContentType;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
public class TopicServiceMockTest {

    private static final String EMAIL = "qasim@mailinator.com";
    private static final Long COURSE_ID = 1L;
    private static final Long SECTION_ID = 16L;
    private static Long TOPIC_ID = 1L;
    @InjectMocks
    TopicService topicService;
    @Mock
    private SectionService sectionService;
    @Mock
    private UserService userService;
    @Mock
    QuizQuestionAnswerService quizQuestionAnswerService;
    @Mock
    EnrollmentService enrollmentService;
    @Mock
    SubscribedUserService subscribedUserService;
    @Mock
    VideoService videoService;
    @Mock
    TopicRepository repo;
    @Mock
    EnrollmentRepository enrollmentRepository;
    Tuple tuple;
    Section section;
    Course course;
    Topic topic;
    Tuple topicTuple1;
    Tuple topicTuple2;
    List<Tuple> topicTuples;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<LinkedHashMap<String, Object>> documentList = new ArrayList<>();
        LinkedHashMap<String, Object> documentMap = new LinkedHashMap<>();
        documentMap.put("id", 1);
        documentMap.put("name", "Document 1");
        documentMap.put("url", "http://example.com/doc1");
        documentMap.put("summary", "Summary 1");
        documentList.add(documentMap);
        tuple = mock(Tuple.class);
        when(tuple.get("id")).thenReturn(1L);
        when(tuple.get("name")).thenReturn("Topic 1");
        when(tuple.get("document_object")).thenReturn(documentList);
        section = new Section();
        course = new Course();

        topicTuple1 = mock(Tuple.class);
        when(topicTuple1.get("course_id")).thenReturn(1L);
        when(topicTuple1.get("total_topics")).thenReturn(5);
        when(topicTuple1.get("duration")).thenReturn(10);
        topicTuple2 = mock(Tuple.class);
        when(topicTuple2.get("course_id")).thenReturn(2L);
        when(topicTuple2.get("total_topics")).thenReturn(8);
        when(topicTuple2.get("duration")).thenReturn(15);
        topicTuples = List.of(topicTuple1, topicTuple2);
    }

    @Test
    @DisplayName("Should fetch all topics by section ID successfully")
    void testFetchAllTopicsBySectionIdSuccess() throws EntityNotFoundException {
        List<Topic> topics = List.of(mock(Topic.class), mock(Topic.class));
        when(repo.findBySectionId(SECTION_ID)).thenReturn(topics);
        List<Topic> result = topicService.fetchAllTopicsBySectionId(SECTION_ID);
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repo, times(1)).findBySectionId(SECTION_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no topics are found for the provided section ID")
    void testFetchAllTopicsBySectionIdWhenNoTopicsFound() {
        when(repo.findBySectionId(SECTION_ID)).thenReturn(List.of());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicService.fetchAllTopicsBySectionId(SECTION_ID)
        );
        assertEquals("No topics found for provided section.", exception.getMessage());
        verify(repo, times(1)).findBySectionId(SECTION_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when the repository returns null for the provided section ID")
    void testFetchAllTopicsBySectionIdWhenRepositoryReturnsNull() {
        when(repo.findBySectionId(SECTION_ID)).thenReturn(null);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicService.fetchAllTopicsBySectionId(SECTION_ID)
        );
        assertEquals("No topics found for provided section.", exception.getMessage());
        verify(repo, times(1)).findBySectionId(SECTION_ID);
    }

    @Test
    @DisplayName("Should fetch summary of video by topic ID successfully")
    void testGetSummaryOfVideoByTopicIdSuccess() throws EntityNotFoundException, BadRequestException {
        Video video = mock(Video.class);
        Topic topic = mock(Topic.class);
        Section section = mock(Section.class);
        Course course = mock(Course.class);

        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(videoService.getVideoByTopicId(TOPIC_ID)).thenReturn(video);
        when(video.getTopic()).thenReturn(topic);
        when(topic.getSection()).thenReturn(section);
        when(section.getCourse()).thenReturn(course);
        when(course.getId()).thenReturn(1L);
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(video.getSummary()).thenReturn("This is a summary of the video.");
        Message<String> result = topicService.getSummaryOfVideoByTopicId(TOPIC_ID, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Fetched summary successfully.", result.getMessage());
        assertEquals("This is a summary of the video.", result.getData());

        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(videoService, times(1)).getVideoByTopicId(TOPIC_ID);
        verify(enrollmentService, times(1)).isEnrolled(1L, EMAIL);
    }

    @Test
    @DisplayName("Should throw BadRequestException when user is not subscribed")
    void testGetSummaryOfVideoByTopicIdWhenUserNotSubscribed() throws EntityNotFoundException {
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicService.getSummaryOfVideoByTopicId(TOPIC_ID, EMAIL)
        );
        assertEquals("No plan is subscribed by user: " + EMAIL, exception.getMessage());
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(videoService, never()).getVideoByTopicId(anyLong());
    }

    @Test
    @DisplayName("Should throw BadRequestException when user is not enrolled in the course")
    void testGetSummaryOfVideoByTopicIdWhenUserNotEnrolled() throws EntityNotFoundException, BadRequestException {
        SubscribedUser subscribedUser = mock(SubscribedUser.class);
        Video video = mock(Video.class);
        Topic topic = mock(Topic.class);
        Section section = mock(Section.class);
        Course course = mock(Course.class);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(videoService.getVideoByTopicId(TOPIC_ID)).thenReturn(video);
        when(video.getTopic()).thenReturn(topic);
        when(topic.getSection()).thenReturn(section);
        when(section.getCourse()).thenReturn(course);
        when(course.getId()).thenReturn(1L);
        when(enrollmentService.isEnrolled(1L, EMAIL)).thenReturn(false);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicService.getSummaryOfVideoByTopicId(TOPIC_ID, EMAIL)
        );
        assertEquals("You are not enrolled in this course please enrolled in the course first.", exception.getMessage());
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(videoService, times(1)).getVideoByTopicId(TOPIC_ID);
        verify(enrollmentService, times(1)).isEnrolled(1L, EMAIL);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when video is not found by topic ID")
    void testGetSummaryOfVideoByTopicIdWhenVideoNotFound() throws EntityNotFoundException {
        SubscribedUser subscribedUser = mock(SubscribedUser.class);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(videoService.getVideoByTopicId(TOPIC_ID)).thenThrow(new EntityNotFoundException("Video not found"));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicService.getSummaryOfVideoByTopicId(TOPIC_ID, EMAIL)
        );
        assertEquals("Video not found", exception.getMessage());
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(videoService, times(1)).getVideoByTopicId(TOPIC_ID);
        verify(enrollmentService, never()).isEnrolled(anyLong(), anyString());
    }

    // TODO ERROR: Resolve below test method
    @Test
    @DisplayName("Should return all topics of a section successfully")
    void testGetAllTopicBySectionSuccess() throws EntityNotFoundException, BadRequestException, IOException {
        var course = CourseTestData.courseData();
        course.setContentType(ContentType.COURSE);
        Section section = SectionTestData.sectionData();
        section.setCourse(course);
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        List<Tuple> topics = List.of(mock(Tuple.class));
        QuizQuestionAnswer quizQuestionAnswers = new QuizQuestionAnswer();
        when(repo.findAllBySectionId(SECTION_ID, SubscribedUserTestData.subscribedUser().getUser().getId())).thenReturn(topics);
        when(quizQuestionAnswerService.fetchAllQuestionAndAnswersByTopicId(SECTION_ID, true, false,
                PageRequest.of(0, 10))).thenReturn(quizQuestionAnswers);

        Message<List<TopicDetailResponse>> result = topicService.getAllTopicBySection(COURSE_ID, SECTION_ID, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Fetching all topics of a section.", result.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(sectionService, times(1)).findById(SECTION_ID);
        verify(repo, times(1)).findAllBySectionId(SECTION_ID, SubscribedUserTestData.subscribedUser().getUser().getId());
        verify(quizQuestionAnswerService, times(1)).fetchAllQuestionAndAnswersByTopicId(SECTION_ID,
                true, false, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Should throw BadRequestException when the user is not enrolled in the course")
    void testGetAllTopicBySectionUserNotEnrolled() {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(false);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            topicService.getAllTopicBySection(COURSE_ID, SECTION_ID, EMAIL);
        });
        assertEquals("You are not enrolled in this course please enroll in the course first.", exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
    }

    @Test
    @DisplayName("Should throw BadRequestException when no subscription plan is found for the user")
    void testGetAllTopicBySectionNoSubscriptionPlan() throws EntityNotFoundException {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            topicService.getAllTopicBySection(COURSE_ID, SECTION_ID, EMAIL);
        });
        assertEquals("No plan is subscribed by user: "+EMAIL, exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
    }

    @Test
    @DisplayName("Should throw BadRequestException when the section is not free and the user has no subscription")
    void testGetAllTopicBySection_whenSectionNotFree() throws EntityNotFoundException, IOException {
        var course = CourseTestData.courseData();
        course.setCreatedBy(22L);

        var user = UserTestData.userData();
        user.setId(21L);

        var subscribedUser = SubscribedUserTestData.freeSubscribedUser();
        subscribedUser.setUser(user);

        Section section = Section.builder().id(SECTION_ID).isFree(false).course(course).build();
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            topicService.getAllTopicBySection(COURSE_ID, SECTION_ID, EMAIL);
        });
        assertEquals("User have to get a paid subscription, this section isn't free.", exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(sectionService, times(1)).findById(SECTION_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no topics are found for the section")
    void testGetAllTopicBySectionNoTopicsFound() throws EntityNotFoundException, IOException {
        Section section = Section.builder().id(SECTION_ID).isFree(true).course(CourseTestData.courseData()).build();
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(repo.findAllBySectionId(SECTION_ID, SubscribedUserTestData.subscribedUser().getId())).thenReturn(Collections.emptyList());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicService.getAllTopicBySection(COURSE_ID, SECTION_ID, EMAIL);
        });
        assertEquals("No topics found for this section.", exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(sectionService, times(1)).findById(SECTION_ID);
        verify(repo, times(1)).findAllBySectionId(SECTION_ID, SubscribedUserTestData.subscribedUser().getId());
    }

    @Test
    @DisplayName("Should return topic when topic ID is found")
    void testGetTopicByIdSuccess() throws EntityNotFoundException, IOException {
        when(repo.findById(TOPIC_ID)).thenReturn(Optional.ofNullable(TopicTestData.topicData()));
        Topic result = topicService.getTopicById(TOPIC_ID);
        assertNotNull(result);
        assertEquals(TOPIC_ID, result.getId());
        assertEquals("Sample Topic", result.getName());
        verify(repo, times(1)).findById(TOPIC_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when topic ID is not found")
    void testGetTopicByIdNotFound() {
        when(repo.findById(TOPIC_ID)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicService.getTopicById(TOPIC_ID);
        });
        assertEquals("No topic found by topic id: 1", exception.getMessage());
        verify(repo, times(1)).findById(TOPIC_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when topic ID is null")
    void testGetTopicByIdWithNullId() {
        when(repo.findById(null)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicService.getTopicById(null);
        });
        assertEquals("No topic found by topic id: null", exception.getMessage());
        verify(repo, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should fetch the number of topics present in a course successfully")
    void testGetAllTopicByCoursesSuccess() throws EntityNotFoundException {
        List<Long> courseIdList = List.of(1L, 2L);
        when(repo.findAllTopicsByCourseIdList(courseIdList)).thenReturn(topicTuples);
        List<NoOfTopicInCourse> result = topicService.getAllTopicByCourses(courseIdList);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getCourseId());
        assertEquals(5, result.get(0).getTopics());
        assertEquals(10, result.get(0).getDuration());
        assertEquals(2L, result.get(1).getCourseId());
        assertEquals(8, result.get(1).getTopics());
        assertEquals(15, result.get(1).getDuration());
        verify(repo, times(1)).findAllTopicsByCourseIdList(courseIdList);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no topics are found for the provided course IDs")
    void testGetAllTopicByCoursesNotFound() {
        List<Long> courseIdList = List.of(1L, 2L);

        when(repo.findAllTopicsByCourseIdList(courseIdList)).thenReturn(Collections.emptyList());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicService.getAllTopicByCourses(courseIdList);
        });

        assertEquals("No topics found.", exception.getMessage());
        verify(repo, times(1)).findAllTopicsByCourseIdList(courseIdList);
    }

    @Test
    @DisplayName("Should handle null values gracefully when converting tuples to NoOfTopicInCourse objects")
    void testGetAllTopicByCoursesWithNullValues() throws EntityNotFoundException {
        List<Long> courseIdList = List.of(1L);
        Tuple tuple = mock(Tuple.class);
        when(tuple.get("course_id")).thenReturn(1L);
        when(tuple.get("total_topics")).thenReturn(null);
        when(tuple.get("duration")).thenReturn(null);
        when(repo.findAllTopicsByCourseIdList(courseIdList)).thenReturn(List.of(tuple));
        List<NoOfTopicInCourse> result = topicService.getAllTopicByCourses(courseIdList);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCourseId());
        assertEquals(0, result.get(0).getTopics()); // Should default to 0
        assertEquals(0, result.get(0).getDuration()); // Should default to 0
        verify(repo, times(1)).findAllTopicsByCourseIdList(courseIdList);
    }

    @Test
    @DisplayName("Should handle empty course ID list gracefully")
    void testGetAllTopicByCoursesWithEmptyCourseIdList() {
        List<Long> courseIdList = Collections.emptyList();
        when(repo.findAllTopicsByCourseIdList(courseIdList)).thenReturn(Collections.emptyList());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicService.getAllTopicByCourses(courseIdList);
        });
        assertEquals("No topics found.", exception.getMessage());
        verify(repo, times(1)).findAllTopicsByCourseIdList(courseIdList);
    }

    @Test
    @DisplayName("Save Topic - Success")
    void saveTopicSuccess() throws InternalServerException {
        topic = Topic.builder().name("New Topic").build();
        when(repo.save(any(Topic.class))).thenReturn(topic);

        Topic result = topicService.save(topic);

        assertNotNull(result);
        assertEquals("New Topic", result.getName());
        verify(repo, times(1)).save(topic);
    }

    @Test
    @DisplayName("Save Topic - Delete Existing Topic")
    void saveTopicDeleteExistingTopic() throws InternalServerException {
        Topic topic = Topic.builder().id(1L).delete(true).build();

        Topic result = topicService.save(topic);

        assertNull(result);
        verify(repo, times(1)).deleteById(1L);
        verify(repo, never()).save(any(Topic.class));
    }

    @Test
    @DisplayName("Save Topic - Internal Server Error on Save")
    void saveTopicInternalServerErrorOnSave() {
        Topic topic = Topic.builder().name("New Topic").build();

        when(repo.save(any(Topic.class))).thenThrow(new DataAccessException("DB error") {});

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            topicService.save(topic);
        });

        assertEquals("Topic" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).save(topic);
    }

    @Test
    @DisplayName("Save Topic - Internal Server Error on Delete")
    void saveTopicInternalServerErrorOnDelete() {
        Topic topic = Topic.builder().id(1L).delete(true).build();

        doThrow(new DataAccessException("DB error") {}).when(repo).deleteById(1L);

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            topicService.save(topic);
        });

        assertEquals("Topic" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).deleteById(1L);
        verify(repo, never()).save(any(Topic.class));
    }

    @DisplayName("Fetch all topics by section for updates with valid data")
    @Test
    void getAllTopicBySectionForUpdate_validSectionAndUserWithTopics() throws EntityNotFoundException, BadRequestException {

        List<Tuple> topics = Collections.singletonList(tuple);
        QuizQuestionAnswer quizQuestionAnswers = new QuizQuestionAnswer();
        course.setCreatedBy(COURSE_ID);
        section.setCourse(course);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(repo.findAllBySectionId(SECTION_ID, UserTestData.userData().getId())).thenReturn(topics);
        when(quizQuestionAnswerService.fetchAllQuestionAndAnswersByTopicId(SECTION_ID, false,
                false, PageRequest.of(0, 10))).thenReturn(quizQuestionAnswers);

        Message<List<TopicDetailForUpdateResponse>> response = topicService.getAllTopicBySectionForUpdate(SECTION_ID, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetching all topics of a section.", response.getMessage());
        assertNotNull(response.getData());
    }

    @DisplayName("Fetch all topics by section for updates when no topics found")
    @Test
    void getAllTopicBySectionForUpdate_validSectionAndUserWithNoTopics() throws EntityNotFoundException {
        Section section = new Section();
        Course course = new Course();
        course.setCreatedBy(1L);
        section.setCourse(course);

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(repo.findAllBySectionId(SECTION_ID, UserTestData.userData().getId())).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicService.getAllTopicBySectionForUpdate(SECTION_ID,EMAIL ));
        assertEquals("No topics found for this section.", exception.getMessage());
    }

    @DisplayName("Fetch all topics by section for updates with invalid section id")
    @Test
    void getAllTopicBySectionForUpdate_invalidSectionId() throws EntityNotFoundException {

        when(sectionService.findById(SECTION_ID)).thenThrow(new EntityNotFoundException("No section found by provided section id."));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicService.getAllTopicBySectionForUpdate(SECTION_ID, EMAIL));
        assertEquals("No section found by provided section id.", exception.getMessage());
    }

    @DisplayName("Fetch all topics by section for updates with invalid user data")
    @Test
    void getAllTopicBySectionForUpdate_userNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicService.getAllTopicBySectionForUpdate(SECTION_ID, EMAIL));
        assertEquals("User not found", exception.getMessage());
    }

    @DisplayName("Fetch all topics by section for updates when user not creator")
    @Test
    void getAllTopicBySectionForUpdate_userNotCreator() throws EntityNotFoundException {
        Section section = new Section();
        Course course = new Course();
        course.setCreatedBy(2L);
        section.setCourse(course);

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(sectionService.findById(SECTION_ID)).thenReturn(section);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicService.getAllTopicBySectionForUpdate(SECTION_ID, EMAIL));
        assertEquals("This has no permission for update for this section.", exception.getMessage());
    }

    @DisplayName("Fetch all topics by section for updates when null section id")
    @Test
    void getAllTopicBySectionForUpdate_nullSectionId() {

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicService.getAllTopicBySectionForUpdate(null, EMAIL));
        assertEquals("Section ID cannot be null", exception.getMessage());
    }

    @DisplayName("Fetch all topics by section for updates when null email")
    @Test
    void getAllTopicBySectionForUpdate_nullEmail() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicService.getAllTopicBySectionForUpdate(SECTION_ID, null));
        assertEquals("Email cannot be null", exception.getMessage());
    }
}