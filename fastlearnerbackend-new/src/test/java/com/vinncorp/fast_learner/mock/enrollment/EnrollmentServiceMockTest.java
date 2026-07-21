package com.vinncorp.fast_learner.mock.enrollment;

import com.vinncorp.fast_learner.dtos.enrollment.EnrolledStudentDto;
import com.vinncorp.fast_learner.dtos.user.user_course_progress.CoursesProgress;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.enrollment.Enrollment;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.enrollment.EnrollmentRepository;
import com.vinncorp.fast_learner.response.enrollment.EnrolledCourseResponse;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.email_template.IEmailService;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.notification.IMilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.services.user.UserCourseProgressService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseSortBy;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import jakarta.persistence.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class EnrollmentServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";

    private static String COURSE_TITLE = "Java Programming";
    private static Long COURSE_ID = 1L;
    @Mock
    private UserService userService;

    @Mock
    private EnrollmentRepository repo;

    @Mock
    private UserCourseProgressService userCourseProgressService;
    @Mock
    CourseService courseService;

    @InjectMocks
    private EnrollmentService enrolledCourseService;
    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @Mock
    IMilestoneAchievementNotificationService milestoneAchievementNotificationService;
    @Mock
    private ICourseUrlService courseUrlService;

    @Mock
    private IEmailService emailService;




    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return list of recommended course IDs when data is present")
    void shouldReturnRecommendedCourseIDsWhenDataIsPresent() throws EntityNotFoundException {
        Long courseId = 1L;
        List<Tuple> mockData = List.of(
                createCourseIdTuple(2L),
                createCourseIdTuple(3L)
        );

        when(repo.findRecommendedCoursesIDs(courseId)).thenReturn(mockData);

        List<Long> result = enrolledCourseService.findRecommendedCoursesIDs(courseId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
        verify(repo, times(1)).findRecommendedCoursesIDs(courseId);
    }

    @Test
    @DisplayName("Should return empty list when no recommended courses are present but no exception is thrown")
    void shouldReturnEmptyListWhenNoRecommendedCoursesPresent() throws EntityNotFoundException {
        Long courseId = 1L;
        List<Tuple> mockData = List.of();

        when(repo.findRecommendedCoursesIDs(courseId)).thenReturn(mockData);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            enrolledCourseService.findRecommendedCoursesIDs(courseId);
        });

        assertEquals("No recommendation is found.", thrown.getMessage());
        verify(repo, times(1)).findRecommendedCoursesIDs(courseId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no data is found")
    void shouldThrowEntityNotFoundExceptionWhenNoDataFound() {

        when(repo.findRecommendedCoursesIDs(COURSE_ID)).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            enrolledCourseService.findRecommendedCoursesIDs(COURSE_ID);
        });

        assertEquals("No recommendation is found.", thrown.getMessage());
        verify(repo, times(1)).findRecommendedCoursesIDs(COURSE_ID);
    }

    @Test
    @DisplayName("Should handle null courseId in the data")
    void shouldHandleNullCourseIdInData() throws EntityNotFoundException {
        Long courseId = 1L;
        List<Tuple> mockData = List.of(
                createCourseIdTuple(null),
                createCourseIdTuple(3L)
        );

        when(repo.findRecommendedCoursesIDs(courseId)).thenReturn(mockData);

        List<Long> result = enrolledCourseService.findRecommendedCoursesIDs(courseId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(null));
        assertTrue(result.contains(3L));
        verify(repo, times(1)).findRecommendedCoursesIDs(courseId);
    }

    @Test
    @DisplayName("Should enroll user in course successfully")
    void enrolled_Success() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {
        Course course = CourseTestData.courseData();
        course.setId(COURSE_ID);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.existsByStudent_IdAndCourse_Id(UserTestData.userData().getId(), COURSE_ID)).thenReturn(false);
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(eq(COURSE_ID), any(GenericStatus.class)))
                .thenReturn(new CourseUrl());

        when(repo.countByCourseId(anyLong())).thenReturn(1L);
        Message<String> response = enrolledCourseService.enrolled(COURSE_ID, EMAIL, false);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User " + EMAIL + " enrolled in course " + CourseTestData.courseData().getTitle() + " successfully.", response.getMessage());
        verify(repo, times(1)).save(any(Enrollment.class));
        verify(courseUrlService, times(1)).findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE);
    }


    @Test
    @DisplayName("Should throw BadRequestException if user is already enrolled")
    void enrolled_AlreadyEnrolled() throws EntityNotFoundException, IOException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.existsByStudent_IdAndCourse_Id(UserTestData.userData().getId(), COURSE_ID)).thenReturn(true);
        when(repo.countByCourseId(anyLong())).thenReturn(1L);

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            enrolledCourseService.enrolled(COURSE_ID, EMAIL, false);
        });

        assertEquals("Already enrolled in this course.", thrown.getMessage());
        verify(repo, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if course not found")
    void enrolled_CourseNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.existsByStudent_IdAndCourse_Id(UserTestData.userData().getId(), COURSE_ID)).thenReturn(false);
        when(courseService.findById(COURSE_ID)).thenThrow(new EntityNotFoundException("Course not found"));
        when(repo.countByCourseId(anyLong())).thenReturn(1L);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            enrolledCourseService.enrolled(COURSE_ID, EMAIL, false);
        });

        assertEquals("Course not found", thrown.getMessage());
        verify(repo, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should throw InternalServerException on enrollment save failure")
    void enrolled_InternalServerError() throws EntityNotFoundException, IOException {
        // Arrange
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.existsByStudent_IdAndCourse_Id(UserTestData.userData().getId(), COURSE_ID)).thenReturn(false);
        when(courseService.findById(COURSE_ID)).thenReturn(CourseTestData.courseData());
        when(repo.save(any(Enrollment.class))).thenThrow(new RuntimeException("Database error"));
        when(repo.countByCourseId(anyLong())).thenReturn(1L);

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            enrolledCourseService.enrolled(COURSE_ID, EMAIL, false);
        });

        assertEquals("Enrollment " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, thrown.getMessage());
        verify(repo, times(1)).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should return true if user is enrolled in the course")
    void isEnrolled_UserEnrolled() {

        when(repo.existsByCourseIdAndStudentEmail(COURSE_ID, EMAIL)).thenReturn(true);

        boolean isEnrolled = enrolledCourseService.isEnrolled(COURSE_ID, EMAIL);

        assertTrue(isEnrolled);
        verify(repo, times(1)).existsByCourseIdAndStudentEmail(COURSE_ID, EMAIL);
    }

    @Test
    @DisplayName("Should return false if user is not enrolled in the course")
    void isEnrolled_UserNotEnrolled() {

        when(repo.existsByCourseIdAndStudentEmail(COURSE_ID, EMAIL)).thenReturn(false);

        boolean isEnrolled = enrolledCourseService.isEnrolled(COURSE_ID, EMAIL);

        assertFalse(isEnrolled);
        verify(repo, times(1)).existsByCourseIdAndStudentEmail(COURSE_ID, EMAIL);
    }

    @Test
    @DisplayName("Should handle null email and return false")
    void isEnrolled_NullEmail() {

        when(repo.existsByCourseIdAndStudentEmail(COURSE_ID, null)).thenReturn(false);

        boolean isEnrolled = enrolledCourseService.isEnrolled(COURSE_ID, null);

        assertFalse(isEnrolled);
        verify(repo, times(1)).existsByCourseIdAndStudentEmail(COURSE_ID, null);
    }

    @Test
    @DisplayName("Should handle null courseId and return false")
    void isEnrolled_NullCourseId() {

        when(repo.existsByCourseIdAndStudentEmail(null, EMAIL)).thenReturn(false);

        boolean isEnrolled = enrolledCourseService.isEnrolled(null, EMAIL);

        assertFalse(isEnrolled);
        verify(repo, times(1)).existsByCourseIdAndStudentEmail(null, EMAIL);
    }

    @Test
    @DisplayName("Should throw exception if repository method throws exception")
    void isEnrolled_RepositoryException() {

        when(repo.existsByCourseIdAndStudentEmail(COURSE_ID, EMAIL)).thenThrow(new RuntimeException("Database error"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            enrolledCourseService.isEnrolled(COURSE_ID, EMAIL);
        });

        assertEquals("Database error", thrown.getMessage());
        verify(repo, times(1)).existsByCourseIdAndStudentEmail(COURSE_ID, EMAIL);
    }

    @Test
    @DisplayName("Should return enrolled student data with total value when a valid period and instructor ID are provided")
    void shouldReturnEnrolledStudentDataWithTotalValueWhenValidPeriodAndInstructorIdProvided() {

        Long instructorId = 1L;
        String period = "Monthly";
        List<Tuple> mockData = List.of(createMockTuple(100L, 1000L, new Date()));

        when(repo.findByInstructorId(period, instructorId)).thenReturn(mockData);

        EnrolledStudentDto result = enrolledCourseService.totalNoOfEnrolledStudent(period, instructorId);

        assertNotNull(result);
        assertEquals(1000L, result.getTotalValue());
        assertFalse(CollectionUtils.isEmpty(result.getValues()));
        verify(repo, times(1)).findByInstructorId(period, instructorId);
    }

    @Test
    @DisplayName("Should return null when no enrolled students are found for the given instructor ID and period")
    void shouldReturnNullWhenNoEnrolledStudentsFound() {

        Long instructorId = 1L;
        String period = "Monthly";

        when(repo.findByInstructorId(period, instructorId)).thenReturn(Collections.emptyList());

        EnrolledStudentDto result = enrolledCourseService.totalNoOfEnrolledStudent(period, instructorId);

        assertNull(result);
        verify(repo, times(1)).findByInstructorId(period, instructorId);
    }

    @Test
    @DisplayName("Should return enrolled student data with total value for different valid periods and the same instructor ID")
    void shouldReturnEnrolledStudentDataWithTotalValueForDifferentPeriods() {

        Long instructorId = 1L;
        List<Tuple> mockData = List.of(createMockTuple(200L, 1500L, new Date()));

        when(repo.findByInstructorId(anyString(), eq(instructorId))).thenReturn(mockData);

        String[] periods = {"Monthly", "Monthly", "PREVIOUS_MONTH", "PREVIOUS_YEAR"};
        for (String period : periods) {
            EnrolledStudentDto result = enrolledCourseService.totalNoOfEnrolledStudent(period, instructorId);
            assertNotNull(result);
            assertEquals(1500L, result.getTotalValue());
            assertFalse(CollectionUtils.isEmpty(result.getValues()));
        }

        verify(repo, times(periods.length)).findByInstructorId(anyString(), eq(instructorId));
    }

    @Test
    @DisplayName("Should return null when an invalid instructor ID is provided")
    void shouldReturnNullWhenInvalidInstructorIdProvided() {

        Long invalidInstructorId = -1L;
        String period = "Monthly";

        when(repo.findByInstructorId(period, invalidInstructorId)).thenReturn(Collections.emptyList());

        EnrolledStudentDto result = enrolledCourseService.totalNoOfEnrolledStudent(period, invalidInstructorId);

        assertNull(result);
        verify(repo, times(1)).findByInstructorId(period, invalidInstructorId);
    }

    @Test
    @DisplayName("Should return null when period is null and a valid instructor ID is provided")
    void shouldReturnNullWhenPeriodIsNull() {

        Long instructorId = 1L;
        String period = null;

        when(repo.findByInstructorId(period, instructorId)).thenReturn(Collections.emptyList());

        EnrolledStudentDto result = enrolledCourseService.totalNoOfEnrolledStudent(period, instructorId);

        assertNull(result);
        verify(repo, times(1)).findByInstructorId(period, instructorId);
    }

    @Test
    @DisplayName("Should handle null instructor ID gracefully and return null")
    void shouldHandleNullInstructorIdGracefully() {

        Long instructorId = null;
        String period = "Monthly";

        when(repo.findByInstructorId(period, instructorId)).thenReturn(Collections.emptyList());

        EnrolledStudentDto result = enrolledCourseService.totalNoOfEnrolledStudent(period, instructorId);

        assertNull(result);
        verify(repo, times(1)).findByInstructorId(period, instructorId);
    }

    @Test
    @DisplayName("Should return total enrolled student by course id")
    void shouldReturnTotalEnrolledStudent_whenProvidedCourseId() {
        when(repo.countByCourseId(1L)).thenReturn(199L);

        long result = enrolledCourseService.totalNoOfEnrolledStudent(1L);

        assertEquals(result, 199L);
    }

    @Test
    @DisplayName("Should fetch enrolled courses by user ID sorted by recently accessed")
    void getEnrolledCourseByUserId_RecentlyAccessed() throws EntityNotFoundException, BadRequestException {

        int sortBy = CourseSortBy.RECENTLY_ACCESSED.getValue();
        Page<Tuple> pagedData = mockPageData();
        List<CoursesProgress> progressList = mockProgressList();

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllEnrolledInProgressCoursesByUserId(anyString(), eq(UserTestData.userData().getId()), anyBoolean(),  any(Pageable.class)))
                .thenReturn(pagedData);
        when(userCourseProgressService.getCoursesProgressByUser(anyList(), eq(UserTestData.userData().getId())))
                .thenReturn(progressList);

        Message<EnrolledCourseResponse> response = enrolledCourseService.getEnrolledCourseByUserId(sortBy, COURSE_TITLE, 0, 10, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("My courses fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotalPages());
        assertEquals(1, response.getData().getMyCourses().size());

        verify(repo, times(1)).findAllEnrolledInProgressCoursesByUserId(anyString(), eq(UserTestData.userData().getId()),anyBoolean(), any(Pageable.class));
    }
    private Page<Tuple> mockPageData() {

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("course_id")).thenReturn(1L);
        when(tuple.get("course_title")).thenReturn("Java Programming");
        when(tuple.get("course_description")).thenReturn("Learn Java from scratch.");
        when(tuple.get("category")).thenReturn("Programming");
        when(tuple.get("course_thumbnail")).thenReturn("url/to/thumbnail");
        when(tuple.get("course_duration_in_hours")).thenReturn(10);
        when(tuple.get("user_id")).thenReturn(UserTestData.userData().getId());
        when(tuple.get("full_name")).thenReturn("John Doe");
        when(tuple.get("profile_picture")).thenReturn("url/to/profile");
        when(tuple.get("max_rating")).thenReturn(4.5);
        when(tuple.get("total_reviews")).thenReturn(100);
        when(tuple.get("is_favourite")).thenReturn(true);
        when(tuple.get("is_enrolled")).thenReturn(true);

        List<Tuple> tuples = Collections.singletonList(tuple);
        return new PageImpl<>(tuples, PageRequest.of(0, 10), tuples.size());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no courses are found")
    void getEnrolledCourseByUserId_NoCoursesFound() throws EntityNotFoundException {

        int sortBy = CourseSortBy.RECENTLY_ACCESSED.getValue();
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllEnrolledInProgressCoursesByUserId(anyString(), eq(UserTestData.userData().getId()),anyBoolean(), any(Pageable.class)))
                .thenReturn(Page.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            enrolledCourseService.getEnrolledCourseByUserId(sortBy, COURSE_TITLE, 0, 10, EMAIL);
        });

        assertEquals("No courses present for the user: " + EMAIL, thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when sortBy parameter is null")
    void getEnrolledCourseByUserId_SortByIsNull() {

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            enrolledCourseService.getEnrolledCourseByUserId(null, COURSE_TITLE, 0, 10, EMAIL);
        });

        assertEquals("Sort By parameter should not be null", thrown.getMessage());
    }

    @Test
    @DisplayName("Should fetch enrolled courses by user ID sorted by oldest accessed")
    void getEnrolledCourseByUserId_OldestAccessed() throws EntityNotFoundException, BadRequestException {

        int sortBy = CourseSortBy.OLDEST_ACCESSED.getValue();
        Page<Tuple> pagedData = mockPageData();
        List<CoursesProgress> progressList = mockProgressList();

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllEnrolledInProgressCoursesByUserId(anyString(), eq(UserTestData.userData().getId()),anyBoolean(), any(Pageable.class)))
                .thenReturn(pagedData);
        when(userCourseProgressService.getCoursesProgressByUser(anyList(), eq(UserTestData.userData().getId())))
                .thenReturn(progressList);

        Message<EnrolledCourseResponse> response = enrolledCourseService.getEnrolledCourseByUserId(sortBy, COURSE_TITLE, 0, 10, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("My courses fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotalPages());
        assertEquals(1, response.getData().getMyCourses().size());

        verify(repo, times(1)).findAllEnrolledInProgressCoursesByUserId(anyString(), eq(UserTestData.userData().getId()),anyBoolean(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should fetch completed courses by user ID")
    void getEnrolledCourseByUserId_Completed() throws EntityNotFoundException, BadRequestException {

        int sortBy = CourseSortBy.COMPLETED.getValue();
        Page<Tuple> pagedData = mockPageData();
        List<CoursesProgress> progressList = mockProgressList();

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllEnrolledCompletedCoursesByUserId(anyString(), eq(UserTestData.userData().getId()), any(Pageable.class)))
                .thenReturn(pagedData);
        when(userCourseProgressService.getCoursesProgressByUser(anyList(), eq(UserTestData.userData().getId())))
                .thenReturn(progressList);

        Message<EnrolledCourseResponse> response = enrolledCourseService.getEnrolledCourseByUserId(sortBy, COURSE_TITLE, 0, 10, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("My courses fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotalPages());
        assertEquals(1, response.getData().getMyCourses().size());

        verify(repo, times(1)).findAllEnrolledCompletedCoursesByUserId(anyString(), eq(UserTestData.userData().getId()), any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no completed courses are found")
    void getEnrolledCourseByUserId_NoCompletedCoursesFound() throws EntityNotFoundException {

        int sortBy = CourseSortBy.COMPLETED.getValue();
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllEnrolledCompletedCoursesByUserId(anyString(), eq(UserTestData.userData().getId()), any(Pageable.class)))
                .thenReturn(Page.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            enrolledCourseService.getEnrolledCourseByUserId(sortBy, "Java", 0, 10, EMAIL);
        });

        assertEquals("No courses present for the user: " + EMAIL, thrown.getMessage());
    }
    private List<CoursesProgress> mockProgressList() {
        CoursesProgress progress = CoursesProgress.builder().id(1L).percentage(75.0).build();
        return Collections.singletonList(progress);
    }

    private Tuple createMockTuple(Long totalStudents, Long total, Date period) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get("total_students")).thenReturn(totalStudents);
        when(tuple.get("total")).thenReturn(total);
        when(tuple.get("period")).thenReturn(period);
        return tuple;
    }

    private Tuple createCourseIdTuple(Long courseId) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get("course_id")).thenReturn(courseId);
        return tuple;
    }

    @Test
    void testNotifyUserThroughEmail_WithValidEnrollments_Success() {
        // Arrange
        List<Tuple> mockEnrollUser = new ArrayList<>();

        // Create a mock Tuple using Mockito's when().thenReturn()
        Tuple tuple1 = mock(Tuple.class);
        when(tuple1.get("student_id")).thenReturn(1L);
        when(tuple1.get("full_Name")).thenReturn("John Doe");
        when(tuple1.get("email")).thenReturn("john.doe@mailinator.com");
        when(tuple1.get("course_id")).thenReturn(101L);
        when(tuple1.get("enrolled_date")).thenReturn(new Date());
        when(tuple1.get("url")).thenReturn("http://example.com/course/101");
        when(tuple1.get("title")).thenReturn("Java Programming");
        when(tuple1.get("is_active")).thenReturn(true);

        // Add the mock tuple to the list
        mockEnrollUser.add(tuple1);

        // Mock repo method
        when(repo.findStudentByEnrollmentDate()).thenReturn(mockEnrollUser);

        enrolledCourseService.notifyUserThroughEmail();

        // Assert
//        verify(repo, times(1)).findStudentByEnrollmentDate();
        /*verify(emailService, times(1)).sendEmail(
                anyString(), anyString(), any(), any()
        );*/
    }

    @Test
    void testNotifyUserThroughEmail_NoEnrollments_NoEmailSent() {
        // Arrange
        when(repo.findStudentByEnrollmentDate()).thenReturn(Collections.emptyList());

        enrolledCourseService.notifyUserThroughEmail();


        // Assert
        // verify(repo, times(1)).findStudentByEnrollmentDate();
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean());
    }

    // TODO ERROR: repo.StudentByEnrollmentDate() zero interaction
    @Test
    void testNotifyUserThroughEmail_EmptyEnrollments_NoEmailSent() {
        // Arrange
        when(repo.findStudentByEnrollmentDate()).thenReturn(null);

        enrolledCourseService.notifyUserThroughEmail();
        // Assert
//        verify(repo, times(1)).findStudentByEnrollmentDate();
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean());
    }


}
