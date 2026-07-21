package com.vinncorp.fast_learner.mock.course;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseVisitor;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.course.CourseVisitorRepository;
import com.vinncorp.fast_learner.response.course.CourseVisitorResponse;
import com.vinncorp.fast_learner.services.course.CourseVisitorService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.notification.IMilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

public class CourseVisitorServiceMockTest {
    public static String EMAIL = "qasim@mailinator.com";
    public static Long COURSE_ID = 1L;
    @InjectMocks
    private CourseVisitorService courseVisitorService;
    @Mock
    private IMilestoneAchievementNotificationService milestoneAchievementNotificationService;
    @Mock
    private CourseVisitorRepository courseVisitorRepository;
    @Mock
    private ICourseUrlService courseUrlService;
    @Mock
    private IUserService userService;
    @Mock
    private RabbitMQProducer rabbitMQProducer;
    @Mock
    private Tuple tuple;
    private Course course;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(tuple.get("month_name")).thenReturn("January");
        when(tuple.get("month")).thenReturn(1);
        when(tuple.get("visit_count")).thenReturn(150L);

        course = new Course();
        course.setId(1L);
        course.setCreatedBy(2L);
    }

    @Test
    @DisplayName("Test save - Success")
    void testSave_Success() throws IOException, EntityNotFoundException {
        when(milestoneAchievementNotificationService.isMilestoneMet(1)).thenReturn(false);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(CourseUrlTestData.courseUrl());
        assertDoesNotThrow(() -> courseVisitorService.save(course, UserTestData.userData()));
        verify(courseVisitorRepository, times(1)).save(any(CourseVisitor.class));
        verify(rabbitMQProducer, times(1)).sendMessage(isNull(), anyString(), anyString(), anyLong(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test save - Repository Exception Handling")
    void testSave_RepositoryExceptionHandling() {

        doThrow(new RuntimeException("Database error")).when(courseVisitorRepository).save(any(CourseVisitor.class));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            courseVisitorService.save(course, UserTestData.userData());
        });

        assertEquals("Course visitor cannot be saved due to database error.", exception.getMessage());

        verify(courseVisitorRepository, times(1)).save(any(CourseVisitor.class));
    }

    @Test
    @DisplayName("Test save - Null Course")
    void testSave_NullCourse() {
        assertThrows(BadRequestException.class, () -> courseVisitorService.save(null, UserTestData.userData()));
    }

    @Test
    @DisplayName("Test save - Null User")
    void testSave_NullUser() {
        assertThrows(BadRequestException.class, () -> courseVisitorService.save(course, null));
    }

    @Test
    @DisplayName("Should fetch all visitors successfully when valid instructor and course ID are provided")
    void fetchAllVisitors_validInstructorAndCourseWithVisitors() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        List<Tuple> visitors = Collections.singletonList(tuple);
        when(courseVisitorRepository.findAllVisitorsByInstructorId(UserTestData.userData().getId(), COURSE_ID)).thenReturn(visitors);

        Message<List<CourseVisitorResponse>> response = courseVisitorService.fetchAllVisitors(COURSE_ID, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetched all visitors successfully.", response.getMessage());
        assertNotNull(response.getData());
    }
    @Test
    @DisplayName("Should throw EntityNotFoundException when no visitors found for valid instructor and course ID")
    void fetchAllVisitors_validInstructorAndCourseWithNoVisitors() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseVisitorRepository.findAllVisitorsByInstructorId(UserTestData.userData().getId(), COURSE_ID)).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> courseVisitorService.fetchAllVisitors(COURSE_ID, EMAIL));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no data found for the instructor")
    void fetchAllVisitors_noDataFoundForInstructor() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseVisitorRepository.findAllVisitorsByInstructorId(UserTestData.userData().getId(), COURSE_ID)).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            courseVisitorService.fetchAllVisitors(COURSE_ID, EMAIL);
        });

        assertEquals("No data found for the instructor: " + UserTestData.userData().getFullName(), thrown.getMessage());
    }

}
