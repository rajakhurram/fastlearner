package com.vinncorp.fast_learner.mock.user.user_course_progress;

import com.vinncorp.fast_learner.mock.section.SectionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.topic.TopicTestData;
import com.vinncorp.fast_learner.dtos.user.user_course_progress.CoursesProgress;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import com.vinncorp.fast_learner.repositories.user.UserCourseCompletionRepository;
import com.vinncorp.fast_learner.repositories.user.UserCourseProgressRepository;
import com.vinncorp.fast_learner.request.user.CreateUserCourseProgressRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.ActiveStudentsResponse;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.notification.IInstructorPerformanceInsightService;
import com.vinncorp.fast_learner.services.notification.IMilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.services.topic.TopicService;
import com.vinncorp.fast_learner.services.user.UserCourseProgressService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserCourseProgressServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";

    @InjectMocks
    private UserCourseProgressService service;

    @Mock
    private UserCourseProgressRepository repo;

    @Mock
    private UserCourseCompletionRepository userCourseCompletionRepo;

    @Mock
    private UserService userService;

    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private TopicService topicService;

    @Mock
    private SubscribedUserService subscribedUserService;

    @Mock
    private IInstructorPerformanceInsightService instructorPerformanceInsightService;

    @Mock
    private IMilestoneAchievementNotificationService milestoneAchievementNotificationService;
    @Mock
    private ICourseUrlService courseUrlService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test Topic Completion - User Successfully Completes a Topic")
    void testMarkComplete_TopicCompletionSuccess() throws Exception {
        CreateUserCourseProgressRequest request = UserCourseProgressTestData.createUserCourseProgressRequest();

        Topic topic = TopicTestData.topicData();
        Section section = SectionTestData.sectionData();
        section.setCourse(Course.builder().id(1L).build());
        topic.setSection(section);


        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(enrollmentService.isEnrolled(topic.getSection().getCourse().getId(), EMAIL)).thenReturn(true);

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(repo.findByTopic_IdAndStudent_Email(request.getTopicId(), EMAIL)).thenReturn(Optional.empty());

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.fetchCourseProgress(any(), any())).thenReturn(30.0);
        when(instructorPerformanceInsightService.isPercentageRateMet(30.0)).thenReturn(false);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(section.getCourse().getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl());
        doNothing().when(milestoneAchievementNotificationService).notifyToUserCourseCompletion(any(), anyString(),
                anyLong());
        doNothing().when(milestoneAchievementNotificationService).notifyToUserCourseMilestoneAchieved(anyDouble(), any(Course.class),
                anyLong());

        Message<String> response = service.markComplete(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Topic is marked completed.", response.getMessage());
    }

    @Test
    @DisplayName("Test Topic Completion - User Not Enrolled in Course")
    void testMarkComplete_UserNotEnrolled() throws EntityNotFoundException, IOException {
        CreateUserCourseProgressRequest request = UserCourseProgressTestData.createUserCourseProgressRequest();

        Topic topic = TopicTestData.topicData();
        Section section = SectionTestData.sectionData();
        section.setCourse(Course.builder().id(1L).build());
        topic.setSection(section);

        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(enrollmentService.isEnrolled(anyLong(), eq(EMAIL))).thenReturn(false);
        when(instructorPerformanceInsightService.isPercentageRateMet(10.0)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.markComplete(request, EMAIL));

        verify(repo, times(0)).save(any(UserCourseProgress.class));
    }


    @Test
    @DisplayName("Test Topic Completion - UserCourseProgress Already Exists")
    void testMarkComplete_UserCourseProgressExists() throws Exception {
        CreateUserCourseProgressRequest request = UserCourseProgressTestData.createUserCourseProgressRequest();

        Topic topic = TopicTestData.topicData();
        Section section = SectionTestData.sectionData();
        section.setCourse(Course.builder().id(1L).build());
        topic.setSection(section);

        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(enrollmentService.isEnrolled(topic.getSection().getCourse().getId(), EMAIL)).thenReturn(true);

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        UserCourseProgress existingProgress = UserCourseProgressTestData.userCourseProgressData();

        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(repo.findByTopic_IdAndStudent_Email(request.getTopicId(), EMAIL)).thenReturn(Optional.of(existingProgress));

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.fetchCourseProgress(any(), any())).thenReturn(30.0);
        when(instructorPerformanceInsightService.isPercentageRateMet(10.0)).thenReturn(false);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(section.getCourse().getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl());
        doNothing().when(milestoneAchievementNotificationService).notifyToUserCourseCompletion(any(), anyString(),
                anyLong());
        doNothing().when(milestoneAchievementNotificationService).notifyToUserCourseMilestoneAchieved(anyDouble(), any(Course.class),
                anyLong());

        Message<String> response = service.markComplete(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Topic is marked completed.", response.getMessage());
    }

    @Test
    @DisplayName("Test Topic Completion - Unmarking a Completed Topic")
    void testMarkComplete_UnmarkTopic() throws Exception {
        CreateUserCourseProgressRequest request = UserCourseProgressTestData.createUserCourseProgressRequest();
        request.setIsCompleted(false);

        Topic topic = TopicTestData.topicData();
        Section section = SectionTestData.sectionData();
        section.setCourse(Course.builder().id(1L).build());
        topic.setSection(section);

        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(enrollmentService.isEnrolled(topic.getSection().getCourse().getId(), EMAIL)).thenReturn(true);

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        UserCourseProgress userCourseProgress = UserCourseProgressTestData.userCourseProgressData();
        userCourseProgress.setCompleted(false);

        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(repo.findByTopic_IdAndStudent_Email(request.getTopicId(), EMAIL)).thenReturn(Optional.of(userCourseProgress));

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.fetchCourseProgress(any(), any())).thenReturn(30.0);
        when(instructorPerformanceInsightService.isPercentageRateMet(10.0)).thenReturn(false);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(section.getCourse().getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl());
        doNothing().when(milestoneAchievementNotificationService).notifyToUserCourseCompletion(any(), anyString(),
                anyLong());
        doNothing().when(milestoneAchievementNotificationService).notifyToUserCourseMilestoneAchieved(anyDouble(), any(Course.class),
                anyLong());

        Message<String> response = service.markComplete(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Unmarked the topic.", response.getMessage());

    }

    @Test
    @DisplayName("Test Topic Completion - Internal Server Exception During Save")
    void testMarkComplete_InternalServerException() throws Exception {
        CreateUserCourseProgressRequest request = UserCourseProgressTestData.createUserCourseProgressRequest();

        Topic topic = TopicTestData.topicData();
        Section section = SectionTestData.sectionData();
        section.setCourse(Course.builder().id(1L).build());
        topic.setSection(section);

        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(enrollmentService.isEnrolled(topic.getSection().getCourse().getId(), EMAIL)).thenReturn(true);

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(repo.findByTopic_IdAndStudent_Email(request.getTopicId(), EMAIL)).thenReturn(Optional.empty());

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.fetchCourseProgress(anyLong(), anyLong())).thenReturn(3.0);
        when(instructorPerformanceInsightService.isPercentageRateMet(10.0)).thenReturn(false);

        doThrow(new RuntimeException("Database error")).when(repo).save(any(UserCourseProgress.class));

        assertThrows(InternalServerException.class, () -> service.markComplete(request, EMAIL));
    }

    @Test
    @DisplayName("Test: Successfully Retrieve Previous Topic for User and Course")
    void testGetPreviousTopicByUserAndCourse_Success() throws IOException {
        Long courseId = 1L;
        String email = "user@example.com";

        UserCourseProgress userCourseProgress = new UserCourseProgress();
        userCourseProgress.setCourse(new Course());
        userCourseProgress.setStudent(new User());

        // Mocking the repository to return a non-null Optional
        when(repo.findFirstByCourse_IdAndStudent_EmailOrderByLastModifiedDateDesc(courseId, email))
                .thenReturn(Optional.of(userCourseProgress));

        UserCourseProgress result = service.getPreviousTopicByUserAndCourse(courseId, email);

        // Assert that the result is not null
        assertNotNull(result);
        assertEquals(userCourseProgress, result);
    }

    @Test
    @DisplayName("Test: No Previous Topic Found for User and Course")
    void testGetPreviousTopicByUserAndCourse_NotFound() {
        when(repo.findFirstByCourse_IdAndStudent_EmailOrderByLastModifiedDateDesc(1L, EMAIL))
                .thenReturn(Optional.empty());

        UserCourseProgress result = service.getPreviousTopicByUserAndCourse(1L, EMAIL);

        assertNull(result);
    }

    @Test
    @DisplayName("Test: Invalid Course ID Provided")
    void testGetPreviousTopicByUserAndCourse_InvalidCourseId() {

        when(repo.findFirstByCourse_IdAndStudent_EmailOrderByLastModifiedDateDesc(-1L, EMAIL))
                .thenReturn(Optional.empty());

        UserCourseProgress result = service.getPreviousTopicByUserAndCourse(-1L, EMAIL);

        assertNull(result);
        verify(repo, times(1)).findFirstByCourse_IdAndStudent_EmailOrderByLastModifiedDateDesc(-1L, EMAIL);
    }

    @Test
    @DisplayName("Test: Successfully Fetch Course Progress")
    void testFetchCourseProgress_Success() throws EntityNotFoundException {

        User mockUser = UserTestData.userData();

        when(userService.findByEmail(EMAIL)).thenReturn(mockUser);

        when(repo.fetchCourseProgress(1L, 1L)).thenReturn(75.0);

        Message<Double> response = service.fetchCourseProgress(1L, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully fetch course progress", response.getMessage());
        assertEquals(75.0, response.getData());

        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).fetchCourseProgress(1L, 1L);
    }

    @Test
    @DisplayName("Test: User Not Found")
    void testFetchCourseProgress_UserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, () -> service.fetchCourseProgress(1L, EMAIL));

        verify(repo, never()).fetchCourseProgress(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Test: Course Progress Not Found (Returns Null)")
    void testFetchCourseProgress_NoProgressFound() throws EntityNotFoundException {
        User mockUser = UserTestData.userData();

        when(userService.findByEmail(EMAIL)).thenReturn(mockUser);

        when(repo.fetchCourseProgress(1L, 1L)).thenReturn(null);

        Message<Double> response = service.fetchCourseProgress(1L, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully fetch course progress", response.getMessage());
        assertNull(response.getData());

        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).fetchCourseProgress(1L, 1L);
    }

    @Test
    @DisplayName("Test: Exception Handling in Repository Call")
    void testFetchCourseProgress_RepositoryException() throws EntityNotFoundException {
        User mockUser = UserTestData.userData();

        // Mocking the userService to return a mock user object
        when(userService.findByEmail(EMAIL)).thenReturn(mockUser);

        // Mocking the repository to throw an exception
        when(repo.fetchCourseProgress(1L, 1L)).thenThrow(new RuntimeException("Database error"));

        // Asserting that the exception is thrown
        assertThrows(RuntimeException.class, () -> service.fetchCourseProgress(1L, EMAIL));

        // Verifying the interactions
        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).fetchCourseProgress(1L, 1L);
    }

    @Test
    @DisplayName("Test: Successfully Fetch Courses Progress by User")
    void testGetCoursesProgressByUser_Success() {
        List<Long> coursesId = Arrays.asList(1L, 2L);

        List<Tuple> mockProgressList = new ArrayList<>();
        Tuple tuple1 = mock(Tuple.class);
        Tuple tuple2 = mock(Tuple.class);

        when(tuple1.get("id")).thenReturn(1L);
        when(tuple1.get("completion_percentage")).thenReturn(50.0);

        when(tuple2.get("id")).thenReturn(2L);
        when(tuple2.get("completion_percentage")).thenReturn(70.0);

        mockProgressList.add(tuple1);
        mockProgressList.add(tuple2);

        when(repo.findCourseProgressByListOfCoursesAndUser(coursesId, 1L)).thenReturn(mockProgressList);

        List<CoursesProgress> result = service.getCoursesProgressByUser(coursesId, 1L);

        assertNotNull(result);
        assertEquals(mockProgressList.size(), result.size());

        verify(repo, times(1)).findCourseProgressByListOfCoursesAndUser(coursesId, 1L);
    }

    @Test
    @DisplayName("Test: No Course Progress Found")
    void testGetCoursesProgressByUser_NoProgressFound() {
        when(repo.findCourseProgressByListOfCoursesAndUser(anyList(), anyLong())).thenReturn(Collections.emptyList());

        List<CoursesProgress> result = service.getCoursesProgressByUser(anyList(), anyLong());

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repo, times(1)).findCourseProgressByListOfCoursesAndUser(anyList(), anyLong());
    }

    @Test
    @DisplayName("Test: Successfully Fetch Course Completion")
    void testFetchCourseCompletion_Success() {
        String period = "monthly";
        Long instructorId = 5L;

        Tuple mockTuple = mock(Tuple.class);
        when(mockTuple.get("enrolled")).thenReturn(2);
        when(mockTuple.get("completed")).thenReturn(2);

        when(userCourseCompletionRepo.fetchCourseCompletion(period.toLowerCase(), instructorId)).thenReturn(mockTuple);

        Tuple result = service.fetchCourseCompletion(period, instructorId);

        assertNotNull(result);
        assertEquals(mockTuple, result);

        verify(userCourseCompletionRepo, times(1)).fetchCourseCompletion(period.toLowerCase(), instructorId);
    }

    @Test
    @DisplayName("Test: Fetch Course Completion with Invalid Period")
    void testFetchCourseCompletion_InvalidPeriod() {
        String period = "invalid_period";
        Long instructorId = 5L;

        when(userCourseCompletionRepo.fetchCourseCompletion(period.toLowerCase(), instructorId)).thenReturn(null);

        Tuple result = service.fetchCourseCompletion(period, instructorId);

        assertNull(result);

        verify(userCourseCompletionRepo, times(1)).fetchCourseCompletion(period.toLowerCase(), instructorId);
    }

    @Test
    @DisplayName("Test: Successfully Mark All Topics of a Section as Completed")
    void testMarkCompletedAllTopicsOfASection_Success() throws EntityNotFoundException {
        Long sectionId = 6L;
        Long userId = 7L;

        List<Topic> mockTopics = Arrays.asList(new Topic(), new Topic());
        when(topicService.fetchAllTopicsBySectionId(sectionId)).thenReturn(mockTopics);

        service.markCompletedAllTopicsOfASection(sectionId, userId);

        verify(topicService, times(1)).fetchAllTopicsBySectionId(sectionId);
    }

    @Test
    @DisplayName("Test: No Topics Found for Section")
    void testMarkCompletedAllTopicsOfASection_NoTopicsFound() throws EntityNotFoundException {
        Long sectionId = 6L;
        Long userId = 7L;

        when(topicService.fetchAllTopicsBySectionId(sectionId)).thenThrow(new EntityNotFoundException("No topics found"));

        service.markCompletedAllTopicsOfASection(sectionId, userId);

        verify(topicService, times(1)).fetchAllTopicsBySectionId(sectionId);
    }


    @Test
    @DisplayName("Should return active students when data is found")
    void getAllActiveStudentsByCourseIdOrInstructorId_DataFound() throws EntityNotFoundException, BadRequestException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("month_name")).thenReturn("January");
        when(tuple.get("month")).thenReturn(1);
        when(tuple.get("active_students")).thenReturn(10L);
        List<Tuple> tuples = List.of(tuple);
        when(repo.findAllActiveStudents(1L, UserTestData.userData().getId())).thenReturn(tuples);

        Message<List<ActiveStudentsResponse>> response = service.getAllActiveStudentsByCourseIdOrInstructorId(1L, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetched all active students successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no data is found")
    void getAllActiveStudentsByCourseIdOrInstructorId_NoDataFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllActiveStudents(1L, UserTestData.userData().getId())).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.getAllActiveStudentsByCourseIdOrInstructorId(1L, EMAIL));
        assertEquals("No active students found.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when email is null")
    void getAllActiveStudentsByCourseIdOrInstructorId_UserNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.getAllActiveStudentsByCourseIdOrInstructorId(1L,null));
        assertEquals("Email cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Test: Successfully Delete All User Course Progress for a Topic")
    void testDeleteAllUserCourseProgressOfVideo_Success() {
        Long topicId = 8L;

        service.deleteAllUserCourseProgressOfVideo(topicId);

        verify(repo, times(1)).deleteAllByTopicId(topicId);
    }

    @Test
    @DisplayName("Test: Delete All User Course Progress for a Non-Existent Topic")
    void testDeleteAllUserCourseProgressOfVideo_NonExistentTopic() {
        Long topicId = 9L;

        doNothing().when(repo).deleteAllByTopicId(topicId);

        service.deleteAllUserCourseProgressOfVideo(topicId);

        verify(repo, times(1)).deleteAllByTopicId(topicId);
    }

    @Test
    @DisplayName("Test: Delete All User Course Progress Throws Exception")
    void testDeleteAllUserCourseProgressOfVideo_Exception() {
        Long topicId = 10L;

        doThrow(new RuntimeException("Error deleting user course progress")).when(repo).deleteAllByTopicId(topicId);

        assertThrows(RuntimeException.class, () -> service.deleteAllUserCourseProgressOfVideo(topicId));

        verify(repo, times(1)).deleteAllByTopicId(topicId);
    }
}
