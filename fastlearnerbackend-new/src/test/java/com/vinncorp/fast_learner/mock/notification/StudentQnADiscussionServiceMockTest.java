package com.vinncorp.fast_learner.mock.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.notification.StudentQnADiscussion;
import com.vinncorp.fast_learner.util.enums.CourseReviewStatus;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class StudentQnADiscussionServiceMockTest {

    @InjectMocks
    private StudentQnADiscussion studentQnADiscussion;

    @Mock
    private RabbitMQProducer producer;
    @Mock
    private ICourseUrlService courseUrlService;
    private Course course;

    @BeforeEach
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        this.course = CourseTestData.courseData();
        this.course.setId(COURSE_ID);
    }

    // TODO ERROR: Resolve below test method
    @Test
    @DisplayName("Test: Notify Student Q&A Discussion - when provided valid data (Success)")
    public void notifyStudentQnADiscussion_whenValidInput() throws IOException, EntityNotFoundException {
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());
        doNothing().when(producer).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(),
                        eq(NotificationContentType.TEXT), eq(NotificationType.COURSE_QnA_DISCUSSION), anyLong());

        studentQnADiscussion.notifyCourseQnADiscussion("What is this topic about?",
                course, "instructor1@mailinator.com");

        verify(producer, times(1)).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(),
                eq(NotificationContentType.TEXT), eq(NotificationType.COURSE_QnA_DISCUSSION), anyLong());
    }

    @Test
    @DisplayName("Test: Notify Liked/Disliked To User - when provided valid data (Success)")
    public void notifyToUserLikeDislikedReview_whenValidInput() throws IOException {
        doNothing().when(producer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                any(NotificationContentType.class), any(NotificationType.class), isNull());

        studentQnADiscussion.notifyToUserLikeDislikedReview("redirectUrl","Qasim Ali", CourseReviewStatus.LIKED.name(), CourseTestData.courseData(),
                1L, 1L);

        verify(producer, times(1)).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                any(NotificationContentType.class), any(NotificationType.class), isNull());
    }

    @Test
    @DisplayName("Test: Notify QnA Reply To User - when provided valid data (Success)")
    public void notifyToUserQnAReply_whenValidInput() throws IOException {
        doNothing().when(producer).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                any(NotificationContentType.class), any(NotificationType.class), isNull());

        studentQnADiscussion.notifyToUserQnAReply("redirectUrl","Qasim Ali", CourseTestData.courseData(), 1L, 1L);

        verify(producer, times(1)).sendMessageToLoggedInUser(anyString(), anyLong(), anyLong(), anyString(), any(),
                any(NotificationContentType.class), any(NotificationType.class), isNull());
    }
}
