package com.vinncorp.fast_learner.mock.user_session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.course.CourseUrlTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user_session.UserSession;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionRepository;
import com.vinncorp.fast_learner.repositories.user_session.UserSessionRepository;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.user.IUserService;

import com.vinncorp.fast_learner.services.user_session.IUserSessionService;
import com.vinncorp.fast_learner.services.user_session.UserSessionService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
class UserSessionServiceTest {

    @Mock
    private ICourseService iCourseService;

    @Mock
    private ICourseUrlService iCourseUrlService;

    @Mock
    private IUserService iUserService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private UserSessionService userSessionService;

    @Mock
    private Principal principal;

    @Mock
    private TaskScheduler scheduler;

    @BeforeEach
    void setup() throws EntityNotFoundException {
        User user = UserTestData.userData();
        when(principal.getName()).thenReturn(user.getEmail());
        when(iUserService.findByEmail(user.getEmail())).thenReturn(user);
    }

    @Test
    @DisplayName("Test createSessionId when provided missing parameters")
    void testCreateSessionId_MissingParameters_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () ->
                userSessionService.createSessionId(null, null, principal)
        );
    }

    @Test
    @DisplayName("Test createSessionId when subscription not found")
    void testCreateSessionId_SubscriptionNotFound_ReturnsNotFound() throws Exception {
        Long subscriptionId = 1L;

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        Message<?> response = userSessionService.createSessionId(subscriptionId, null, principal);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Subscription not found with this ID: 1", response.getMessage());
    }

    @Test
    void testCreateSessionId_WithValidSubscription_ReturnsSuccess() throws Exception {
        Long subscriptionId = 2L;
        Subscription subscription = SubscriptionTestData.standardSubscription();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(i -> i.getArgument(0));

        Message<?> response = userSessionService.createSessionId(subscriptionId, null, principal);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Session id created Successfully", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testCreateSessionId_CourseNotFound_ThrowsEntityNotFoundException() throws EntityNotFoundException {
        Long courseId = 3L;

        when(iCourseService.findById(courseId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () ->
                userSessionService.createSessionId(null, courseId, principal)
        );
    }

    @Test
    @DisplayName("Test createSessionId when course found")
    void testCreateSessionId_WithValidCourse_ReturnsSuccess() throws Exception {
        Course course = CourseTestData.courseData();
        course.setId(1L);
        CourseUrl courseUrl = CourseUrlTestData.courseUrl();

        when(iCourseService.findById(course.getId())).thenReturn(course);
        when(iCourseUrlService.findActiveUrlByCourseIdAndStatus(eq(course.getId()), eq(GenericStatus.ACTIVE)))
                .thenReturn(courseUrl);
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(i -> i.getArgument(0));

        Message<?> response = userSessionService.createSessionId(null, course.getId(), principal);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Session id created Successfully", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testCreateSessionId_SchedulesDeletion() throws Exception {
        Course course = CourseTestData.courseData();
        course.setId(1L);
        CourseUrl courseUrl = CourseUrlTestData.courseUrl();

        when(iCourseService.findById(course.getId())).thenReturn(course);
        when(iCourseUrlService.findActiveUrlByCourseIdAndStatus(eq(course.getId()), eq(GenericStatus.ACTIVE)))
                .thenReturn(courseUrl);

        UserSession savedSession = UserSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(UserTestData.userData().getId())
                .createdAt(LocalDateTime.now())
                .build();

        when(userSessionRepository.save(any(UserSession.class))).thenReturn(savedSession);
        doNothing().when(userSessionRepository).deleteById(anyLong());

        userSessionService.createSessionId(null, course.getId(), principal);

    }
}
