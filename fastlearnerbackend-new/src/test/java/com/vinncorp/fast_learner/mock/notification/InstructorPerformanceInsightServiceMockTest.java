package com.vinncorp.fast_learner.mock.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.course.CourseUrlTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.notification.InstructorPerformanceInsightService;
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

import static com.vinncorp.fast_learner.mock.course.CourseVisitorServiceMockTest.COURSE_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class InstructorPerformanceInsightServiceMockTest {

    @InjectMocks
    private InstructorPerformanceInsightService service;

    @Mock
    private RabbitMQProducer rabbitMQProducer;
    @Mock
    private ICourseUrlService courseUrlService;
    private Course course;

    @BeforeEach
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        course = CourseTestData.courseData();
        course.setId(COURSE_ID);
    }

    @Test
    @DisplayName("Test: Notify Certificate Completion Rate - when provided valid data (Success)")
    public void notifyCertificateCompletionRate_whenProvidedValidData() throws IOException, EntityNotFoundException {
        doNothing().when(rabbitMQProducer).sendMessage(anyString(), anyString(), anyString(),
                        anyLong(), any(), any(), any(), any());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE)).thenReturn(CourseUrlTestData.courseUrl());

        service.notifyCertificateCompletionRate(40.0, course, "instructor1@mailinator.com");

        verify(rabbitMQProducer, times(1)).sendMessage(anyString(), anyString(), anyString(),
                anyLong(), any(), any(), any(), any());
    }

    // TODO ERROR: Resolve below test method
    @Test
    @DisplayName("Test: Notify To User Progress Update - when provided valid data (Success)")
    public void notifyToUserProgressUpdate_whenProvidedValidData() throws IOException, EntityNotFoundException {

        doNothing().when(rabbitMQProducer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(),
                any(), any(), any(), any());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE)).thenReturn(CourseUrlTestData.courseUrl());
        service.notifyToUserProgressUpdate(course, 1L);

        verify(rabbitMQProducer, times(1)).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(),
                any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test: Notify To User On New Subscription- when provided valid data (Success)")
    public void notifyToUserOnNewSubscription_whenProvidedValidData() throws IOException {
        doNothing().when(rabbitMQProducer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(),
                any(), any(), any(), any());
        service.notifyToUserOnNewSubscription("Monthly", 1L);

        verify(rabbitMQProducer, times(1)).sendMessageToLoggedInUser(anyString(), anyLong(), eq(null), eq(null),
                any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test: Notify To User Exclusive Course Access- when provided valid data (Success)")
    public void notifyToUserExclusiveCourseAccess_whenProvidedValidData() throws IOException {
        doNothing().when(rabbitMQProducer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(),
                any(), any(), any(), any());
        service.notifyToUserExclusiveCourseAccess("Monthly", 1L);

        verify(rabbitMQProducer, times(1)).sendMessageToLoggedInUser(anyString(), anyLong(), eq(null), eq(null),
                any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test: Is Percentage Rate Met - when provided valid data (Success)")
    public void isPercentageRateMet_whenProvidedValidData() throws IOException {
        var isMet = service.isPercentageRateMet(40.0);

        assertTrue(isMet);
    }

    @Test
    @DisplayName("Test: Is Percentage Rate Met - when provided invalid data")
    public void isPercentageRateMet_whenProvidedInvalidData() throws IOException {
        var isMet = service.isPercentageRateMet(41.0);

        assertFalse(isMet);
    }
}
