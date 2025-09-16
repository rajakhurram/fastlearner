package com.vinncorp.fast_learner.mock.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.section.SectionReview;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.enrollment.EnrollmentRepository;
import com.vinncorp.fast_learner.repositories.section.SectionReviewRepository;
import com.vinncorp.fast_learner.request.section.CreateSectionReviewRequest;
import com.vinncorp.fast_learner.response.section.SectionReviewResponse;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.section.SectionReviewService;
import com.vinncorp.fast_learner.services.section.SectionService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SectionReviewServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";
    private static Long COURSE_ID = 1L;
    private static String COURSE_TITLE = "Test Course";
    private static String COMMENT = "Great section!";
    private static Long REVIEW_ID = 1L;
    private static Long SECTION_ID = 1L;
    private static String PAYPAL_SUBSCRIPTION_ID = "1L";
    @Mock
    private SectionReviewRepository repo;
    @Mock
    private SectionService sectionService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private SubscribedUserService subscribedUserService;
    @Mock
    private RabbitMQProducer producer;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private ICourseUrlService courseUrlService;
    @InjectMocks
    private SectionReviewService service;
    private CreateSectionReviewRequest request;
    private SubscribedUser subscribedUser;
    private Section section;
    private SectionReview sectionReview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new CreateSectionReviewRequest();
        request.setCourseId(COURSE_ID);
        request.setSectionId(SECTION_ID);
        request.setValue(4);
        request.setComment(COMMENT);

        subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User());
        subscribedUser.getUser().setId(UserTestData.userData().getId());
        subscribedUser.getUser().setEmail(EMAIL);
        subscribedUser.setPaypalSubscriptionId(PAYPAL_SUBSCRIPTION_ID);

        section = new Section();
        section.setId(SECTION_ID);
        section.setFree(true);
        section.setCourse(new Course());
        section.getCourse().setTitle(COURSE_TITLE);
        section.getCourse().setCreatedBy(UserTestData.userData().getId());

        sectionReview = new SectionReview();
        sectionReview.setSection(section);
        sectionReview.setComment(COMMENT);
        sectionReview.setRating(3.0);
        sectionReview.setCreatedBy(UserTestData.userData().getId());
    }

    @Test
    @DisplayName("Test findBySectionId - Successful Retrieval of Section Review")
    void testFindBySectionId_Success() throws Exception {
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(repo.findBySectionIdAndCreatedBy(SECTION_ID, UserTestData.userData().getId())).thenReturn(sectionReview);
        when(repo.countBySection_Id(SECTION_ID)).thenReturn(10L);

        Message<SectionReviewResponse> response = service.findBySectionId(SECTION_ID, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(COMMENT, response.getData().getComment());
        assertEquals(3.0, response.getData().getValue());
        assertEquals(10L, response.getData().getTotalReviews());
        verify(repo, times(1)).findBySectionIdAndCreatedBy(SECTION_ID, UserTestData.userData().getId());
    }

    @Test
    @DisplayName("Test findBySectionId - No Subscription Found for User")
    void testFindBySectionId_NoSubscription() throws EntityNotFoundException {
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.findBySectionId(SECTION_ID, EMAIL));
        assertEquals("No plan is subscribed by user: " + EMAIL, exception.getMessage());
        verify(repo, never()).findBySectionIdAndCreatedBy(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Test findBySectionId - User Has No Permission for Non-Free Section")
    void testFindBySectionId_NoPermissionForNonFreeSection() throws EntityNotFoundException {
        section.setFree(false);
        subscribedUser.setPaypalSubscriptionId(null);

        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.findBySectionId(SECTION_ID, EMAIL));
        assertEquals("You doesn't have permission of this section.", exception.getMessage());
        verify(repo, never()).findBySectionIdAndCreatedBy(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Test findBySectionId - Section Review Not Found")
    void testFindBySectionId_ReviewNotFound() throws EntityNotFoundException {
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(repo.findBySectionIdAndCreatedBy(SECTION_ID, UserTestData.userData().getId())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.findBySectionId(SECTION_ID, EMAIL));
        assertEquals("Section review is not found for the provided section.", exception.getMessage());
    }

    @Test
    @DisplayName("Test createSectionReview - Successful Section Review Creation")
    void testCreateSectionReview_Success() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(repo.findBySectionIdAndCreatedBy(SECTION_ID, UserTestData.userData().getId())).thenReturn(null);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());

        Message<String> response = service.createSectionReview(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Section review is created successfully.", response.getMessage());
        verify(repo, times(1)).save(any(SectionReview.class));
//        verify(producer, times(1)).sendMessage(eq(COURSE_TITLE), anyString(), eq(EMAIL), anyLong(), eq(NotificationContentType.TEXT), eq(NotificationType.SECTION_REVIEW));
    }

    // TODO ERROR: Resolve below test method
    @Test
    @DisplayName("Test createSectionReview - Update Existing Section Review")
    void testCreateSectionReview_UpdateExistingReview() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(repo.findBySectionIdAndCreatedBy(SECTION_ID, UserTestData.userData().getId())).thenReturn(sectionReview);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());

        Message<String> response = service.createSectionReview(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Section review is created successfully.", response.getMessage());
        assertEquals(COMMENT, sectionReview.getComment());
        assertEquals(4.0, sectionReview.getRating());
        verify(repo, times(1)).save(any(SectionReview.class));
    }

    @Test
    @DisplayName("Test createSectionReview - User Not Enrolled in the Course")
    void testCreateSectionReview_UserNotEnrolled() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            service.createSectionReview(request, EMAIL);
        });

        assertEquals("You are not enrolled in this course please enroll in the course first.", exception.getMessage());
        verify(repo, never()).save(any(SectionReview.class));
    }

    @Test
    @DisplayName("Test createSectionReview - No Plan Subscribed by User")
    void testCreateSectionReview_NoPlanSubscribed() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            service.createSectionReview(request, EMAIL);
        });

        assertEquals("No plan is subscribed by user: "+EMAIL, exception.getMessage());
        verify(repo, never()).save(any(SectionReview.class));
    }

    @Test
    @DisplayName("Test createSectionReview - User Does Not Have Permission for the Section")
    void testCreateSectionReview_NoPermissionForSection() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        subscribedUser.setPaypalSubscriptionId(null);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        section.setFree(false);
        when(sectionService.findById(SECTION_ID)).thenReturn(section);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            service.createSectionReview(request, EMAIL);
        });

        assertEquals("You doesn't have permission for this section.", exception.getMessage());
        verify(repo, never()).save(any(SectionReview.class));
    }

    @Test
    @DisplayName("Test createSectionReview - Internal Server Error on Save")
    void testCreateSectionReview_InternalServerError() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(sectionService.findById(SECTION_ID)).thenReturn(section);
        when(repo.findBySectionIdAndCreatedBy(SECTION_ID, UserTestData.userData().getId())).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(repo).save(any(SectionReview.class));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.createSectionReview(request, EMAIL);
        });

        assertEquals("Section review cannot be saved due to database error.", exception.getMessage());
        verify(repo, times(1)).save(any(SectionReview.class));
    }

}
