package com.vinncorp.fast_learner.mock.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.course.CourseUrlTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.notification.MilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.test_util.Constants;
import com.vinncorp.fast_learner.util.enums.ContentType;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MilestoneAchievementNotificationServiceMockTest {

    @InjectMocks
    private MilestoneAchievementNotificationService service;

    @Mock
    private RabbitMQProducer rabbitMQProducer;
    @Mock
    private ICourseUrlService courseUrlService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Test: Notify Course Achievements - when provided valid data (Success)")
    @Test
    public void testNotifyCourseAchievements_whenProvidedValidData() throws IOException, EntityNotFoundException {
        Course course = CourseTestData.courseData();
        course.setId(Constants.VALID_COURSE_ID);
        course.setTitle(Constants.COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());

        when(courseUrlService.findActiveUrlByCourseIdAndStatus(Constants.VALID_COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url(Constants.COURSE_URL).build());
        doNothing().when(rabbitMQProducer).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.ENROLLMENT_ACHIEVEMENT), anyLong());
        service.notifyCourseMilestoneAchievements(50, course, "instructor1@mailinator.com");

        verify(rabbitMQProducer, times(1)).sendMessage(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @DisplayName("Test: Notify To User Course Achieved - when provided valid data (Success)")
    @Test
    public void testNotifyToUserCourseAchieved_whenProvidedValidData() throws IOException, EntityNotFoundException {

        Course course = CourseTestData.courseData();
        course.setId(1L);
        course.setId(Constants.VALID_COURSE_ID);
        course.setTitle(Constants.COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());

        when(courseUrlService.findActiveUrlByCourseIdAndStatus(Constants.VALID_COURSE_ID, GenericStatus.ACTIVE)).thenReturn(CourseUrlTestData.courseUrl());

        doNothing().when(rabbitMQProducer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.COURSE_MILESTONE_ACHIEVED), anyLong());
        service.notifyToUserCourseMilestoneAchieved(50, course, 1L);

        verify(rabbitMQProducer, times(1)).sendMessageToLoggedInUser(any(), any(), any(), any(), any(),
                any(), any(), any());
    }

    @Test
    @DisplayName("Test: Notify To User Course Enrollment - when provided valid data (Success)")
    public void notifyToUserCourseEnrollment_whenProvidedValidData() throws IOException {
        doNothing().when(rabbitMQProducer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.ENROLLMENT_CONFIRMATION), anyLong());
        service.notifyToUserCourseEnrollment(CourseTestData.courseData(), "url", 1L);

        verify(rabbitMQProducer, times(1)).sendMessageToLoggedInUser(any(), any(), any(), any(), any(),
                any(), any(), any());
    }

    @Test
    @DisplayName("Test: Notify To User Course Completion - when provided valid data (Success)")
    public void notifyToUserCourseCompletion_whenProvidedValidData() throws IOException {
        doNothing().when(rabbitMQProducer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.COURSE_COMPLETION), anyLong());
        service.notifyToUserCourseCompletion(CourseTestData.courseData(), "url", 1L);

        verify(rabbitMQProducer, times(1)).sendMessageToLoggedInUser(any(), any(), any(), any(), any(),
                any(), any(), any());
    }

    @DisplayName("Test: Notify Course Visit Milestone Achievement - when provided valid data (Success)")
    @Test
    public void testNotifyCourseVisitMilestoneAchievement_whenProvidedValidData() throws IOException, EntityNotFoundException {
        Course course = new Course();
        course.setId(Constants.VALID_COURSE_ID);
        course.setTitle(Constants.COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(Constants.VALID_COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url(Constants.COURSE_URL).build());
        doNothing().when(rabbitMQProducer).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.COURSE_VISIT_ACHIEVEMENT), anyLong());
        service.notifyCourseVisitMilestoneAchievement(50, course, "instructor1@mailinator.com");

        verify(rabbitMQProducer, times(1)).sendMessage(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @DisplayName("Test: Notify Certification Completion - when provided valid data (Success)")
    @Test
    public void testNotifyCertificationCompletion_whenProvidedValidData() throws IOException {
        var course = CourseTestData.courseData();
        course.setContentType(ContentType.COURSE);

        doNothing().when(rabbitMQProducer).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.CERTIFICATION_COMPLETION),
                anyLong());
        service.notifyCertificationCompletion("Qasim Ali", CourseTestData.courseData().getTitle(), "url", course.getCreatedBy(), course, "instructor1@mailinator.com");

        verify(rabbitMQProducer, times(1)).sendMessage(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @DisplayName("Test: Notify To User Certificate Awarded - when provided valid data (Success)")
    @Test
    public void testNotifyCertificateAwarded_whenProvidedValidData() throws IOException {
        doNothing().when(rabbitMQProducer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.CERTIFICATE_AWARDED), anyLong());
        var course = CourseTestData.courseData();
        service.notifyToUserCertificateAwarded(course.getTitle(), 1L, CourseTestData.courseData().getCreatedBy(), 1L, course);

        verify(rabbitMQProducer, times(1)).sendMessageToLoggedInUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    // TODO ERROR: java.lang.NullPointerException: Cannot invoke "com.vinncorp.fast_learner.models.course.Course.getContentType()" because "course" is null
    @DisplayName("Test: Is Milestones Met - when current count given (Success)")
    @Test
    public void isMilestoneMet_whenCurrentCountGiven() {
        boolean milestones50 = service.isMilestoneMet(50);
        assertThat(milestones50).isTrue();

        boolean milestones100 = service.isMilestoneMet(100);
        assertThat(milestones100).isTrue();

        boolean milestones1000 = service.isMilestoneMet(1000);
        assertThat(milestones1000).isTrue();

        boolean milestones10000 = service.isMilestoneMet(10000);
        assertThat(milestones10000).isTrue();
    }

    @DisplayName("Test: Is Milestones Met - when invalid current count given")
    @Test
    public void isMilestoneMet_whenInvalidCurrentCountGiven() {
        boolean milestones49 = service.isMilestoneMet(49);
        assertThat(milestones49).isFalse();

        boolean milestones99 = service.isMilestoneMet(99);
        assertThat(milestones99).isFalse();

        boolean milestones999 = service.isMilestoneMet(999);
        assertThat(milestones999).isFalse();

        boolean milestones9999 = service.isMilestoneMet(9999);
        assertThat(milestones9999).isFalse();
    }
}