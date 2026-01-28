package com.vinncorp.fast_learner.mock.course.course_review;

import com.vinncorp.fast_learner.dtos.course.CourseFeedback;
import com.vinncorp.fast_learner.dtos.course.FeedbackComment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.course.course_review.CourseReview;
import com.vinncorp.fast_learner.models.course.course_review.CourseReviewLikedDisliked;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.course.course_review.CourseReviewRepository;
import com.vinncorp.fast_learner.request.course.CreateCourseReviewRequest;
import com.vinncorp.fast_learner.response.course.CourseFeedbackResponse;
import com.vinncorp.fast_learner.response.course.CourseReviewResponse;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.course.course_review.CourseReviewService;
import com.vinncorp.fast_learner.services.course.course_review.ICourseReviewLikedDislikedService;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.notification.IStudentQnADiscussion;
import com.vinncorp.fast_learner.services.notification.StudentQnADiscussion;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseReviewStatus;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CourseReviewServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";
    private static Long COURSE_ID = 1L;
    private static String COURSE_TITLE = "Test Course";
    private static String COURSE_URL = "test-course";
    private static String COMMENT = "Great course!";
    private static Long REVIEW_ID = 1L;
    @InjectMocks
    CourseReviewService courseReviewService;
    @Mock
    UserService userService;
    @Mock
    CourseReviewRepository courseReviewRepository;
    @Mock
    private Tuple tuple;
    @Mock
    private ISubscribedUserService subscribedUserService;
    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private CourseService courseService;

    @Mock
    private RabbitMQProducer producer;
    @Mock
    private ICourseReviewLikedDislikedService courseReviewLikedDislikedService;
    @Mock
    private StudentQnADiscussion studentQnADiscussion;
    @Mock
    private ICourseUrlService courseUrlService;

    private CreateCourseReviewRequest request;
    private SubscribedUser subscribedUser;
    private Course course;
    private CourseReview review;

    private List<Tuple> rawData;
    private Page<Tuple> pageData;
    private Tuple allReviewTuple;
    private Tuple reviewMock;
    private CourseReviewLikedDisliked mockCourseReviewLikedDisliked;

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        subscribedUser = SubscribedUser.builder().user(UserTestData.userData()).build();
        course = new Course();
        course.setId(COURSE_ID);
        course.setTitle(COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());
        request = new CreateCourseReviewRequest();
        request.setCourseId(1L);
        request.setValue(4);
        request.setComment(COMMENT);

        review = CourseReview.builder().course(course).rating(4).comment(COMMENT).build();
        review.setCreatedBy(UserTestData.userData().getId());
        review.setCreationDate(new Date());
        review.setLikes(10);
        review.setDislikes(2);

        mockCourseReviewLikedDisliked = new CourseReviewLikedDisliked();
        mockCourseReviewLikedDisliked.setCourseReviewId(1L);
        mockCourseReviewLikedDisliked.setStatus(CourseReviewStatus.LIKED);
        mockCourseReviewLikedDisliked.setCreatedBy(UserTestData.userData().getId());

        Tuple tupleMock = mock(Tuple.class);
        when(tupleMock.get("rating")).thenReturn(5.0);
        when(tupleMock.get("users")).thenReturn(10L);

        rawData = List.of(tupleMock);

        reviewMock = mock(Tuple.class);
        when(reviewMock.get("comment")).thenReturn("Great course!");
        when(reviewMock.get("rating")).thenReturn(5.0);
        when(reviewMock.get("full_name")).thenReturn("John Doe");
        when(reviewMock.get("likes")).thenReturn(5);
        when(reviewMock.get("dislikes")).thenReturn(1);
        when(reviewMock.get("review_id")).thenReturn(1L);
        when(reviewMock.get("created_date")).thenReturn("2024-08-15");
        when(reviewMock.get("profile_picture")).thenReturn("profile.jpg");

        pageData = new PageImpl<>(List.of(reviewMock), PageRequest.of(0, 10), 1);

        allReviewTuple = mock(Tuple.class);
        when(allReviewTuple.get("avg_reviews")).thenReturn(50L);
        when(allReviewTuple.get("total_reviewers")).thenReturn(4.5);
    }

    @Test
    @DisplayName("Test likeDislikeReview - Like a Review Successfully")
    void testLikeDislikeReview_Like_Success() throws Exception {
        review.setId(REVIEW_ID);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findById(1L)).thenReturn(Optional.ofNullable(review));
        when(courseReviewLikedDislikedService.getByCourseReviewId(REVIEW_ID, UserTestData.userData().getId())).thenReturn(null);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl());
        when(courseReviewRepository.findByReviewId(Optional.of(review).get().getId())).thenReturn(this.reviewMock);
        when(courseReviewRepository.save(Optional.of(review).get())).thenReturn(review);
        doNothing().when(studentQnADiscussion).notifyToUserLikeDislikedReview(anyString(), anyString(), eq(CourseReviewStatus.LIKED.name()),
                any(), anyLong(), anyLong());

        Message<FeedbackComment> result = courseReviewService.likeDislikeReview(REVIEW_ID, CourseReviewStatus.LIKED, EMAIL);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Course review is liked successfully.", result.getMessage());

        assertEquals(11, review.getLikes());
        assertEquals(2, review.getDislikes());

        verify(courseReviewRepository, times(1)).save(review);
        verify(courseReviewLikedDislikedService, times(1)).save(any(CourseReviewLikedDisliked.class));
    }

    @Test
    @DisplayName("Test likeDislikeReview - Dislike a Review Successfully")
    void testLikeDislikeReview_Dislike_Success() throws Exception {
        review.setId(REVIEW_ID);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(courseReviewLikedDislikedService.getByCourseReviewId(REVIEW_ID, UserTestData.userData().getId())).thenReturn(null);
        doNothing().when(studentQnADiscussion).notifyToUserLikeDislikedReview(anyString(), anyString(), eq(CourseReviewStatus.DISLIKED.name()),
                any(), anyLong(), anyLong());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl());
        when(courseReviewRepository.save(review)).thenReturn(review);
        when(courseReviewRepository.findByReviewId(Optional.of(review).get().getId())).thenReturn(this.reviewMock);

        Message<FeedbackComment> result = courseReviewService.likeDislikeReview(1L, CourseReviewStatus.DISLIKED, EMAIL);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Course review is disliked successfully.", result.getMessage());

        assertEquals(10, review.getLikes());
        assertEquals(3, review.getDislikes());

        verify(courseReviewRepository, times(1)).save(review);
        verify(courseReviewLikedDislikedService, times(1)).save(any(CourseReviewLikedDisliked.class));
    }

    @Test
    @DisplayName("Test likeDislikeReview - Already Liked the Review")
    void testLikeDislikeReview_AlreadyLiked() throws Exception {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(courseReviewLikedDislikedService.getByCourseReviewId(REVIEW_ID, UserTestData.userData().getId())).thenReturn(mockCourseReviewLikedDisliked);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseReviewService.likeDislikeReview(REVIEW_ID, CourseReviewStatus.LIKED, EMAIL);
        });

        assertEquals("This user already LIKED the course review.", exception.getMessage());

        verify(courseReviewRepository, never()).save(any(CourseReview.class));
        verify(courseReviewLikedDislikedService, never()).save(any(CourseReviewLikedDisliked.class));
    }

    @Test
    @DisplayName("Test likeDislikeReview - Course Review Not Found")
    void testLikeDislikeReview_ReviewNotFound() throws Exception {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseReviewService.likeDislikeReview(REVIEW_ID, CourseReviewStatus.LIKED, EMAIL);
        });

        assertEquals("Course review not found with the provided course review id.", exception.getMessage());

        verify(courseReviewRepository, never()).save(any(CourseReview.class));
        verify(courseReviewLikedDislikedService, never()).save(any(CourseReviewLikedDisliked.class));
    }

    @Test
    @DisplayName("Test likeDislikeReview - Internal Server Exception")
    void testLikeDislikeReview_InternalServerException() throws Exception {
        review.setId(REVIEW_ID);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(courseReviewLikedDislikedService.getByCourseReviewId(REVIEW_ID, UserTestData.userData().getId())).thenReturn(null);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl());
        when(courseReviewRepository.findByReviewId(Optional.of(review).get().getId())).thenReturn(this.reviewMock);
        doNothing().when(studentQnADiscussion).notifyToUserLikeDislikedReview(anyString(), anyString(), eq(CourseReviewStatus.LIKED.name()),
                any(), anyLong(), anyLong());
        doThrow(new RuntimeException("Database error")).when(courseReviewRepository).save(any(CourseReview.class));
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            courseReviewService.likeDislikeReview(REVIEW_ID, CourseReviewStatus.LIKED, EMAIL);
        });
        assertEquals("Course review like or dislike status cannot updated.", exception.getMessage());
        verify(courseReviewRepository, times(1)).save(review);
    }

    @Test
    @DisplayName("Test fetchAllReviewOfACourse - Success")
    void testFetchAllReviewOfACourse_Success() {

        when(courseReviewRepository.findStudentReviewsByCourseId(1L)).thenReturn(allReviewTuple);
        Tuple result = courseReviewService.fetchAllReviewOfACourse(1L);
        assertNotNull(result);
        assertEquals(50L, result.get("avg_reviews"));
        assertEquals(4.5, result.get("total_reviewers"));
        verify(courseReviewRepository, times(1)).findStudentReviewsByCourseId(1L);
    }

    @Test
    @DisplayName("Test fetchAllReviewOfACourse - No Reviews Found")
    void testFetchAllReviewOfACourse_NoReviewsFound() {

        when(courseReviewRepository.findStudentReviewsByCourseId(1L)).thenReturn(null);
        Tuple result = courseReviewService.fetchAllReviewOfACourse(1L);
        assertNull(result);
        verify(courseReviewRepository, times(1)).findStudentReviewsByCourseId(1L);
    }

    @Test
    @DisplayName("Test fetchAllReviewOfACourse - Exception Handling")
    void testFetchAllReviewOfACourse_ExceptionHandling() {

        when(courseReviewRepository.findStudentReviewsByCourseId(1L)).thenThrow(new RuntimeException("Database error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseReviewService.fetchAllReviewOfACourse(1L);
        });
        assertEquals("Database error", exception.getMessage());
        verify(courseReviewRepository, times(1)).findStudentReviewsByCourseId(1L);
    }

    @Test
    @DisplayName("Test fetchAllReviewsForAnInstructorCourses - Success")
    void testFetchAllReviewsForAnInstructorCourses_Success() {

        when(courseReviewRepository.findAllCoursesReviewsOfAnInstructor(1L)).thenReturn(allReviewTuple);

        Tuple result = courseReviewService.fetchAllReviewsForAnInstructorCourses(1L);

        assertNotNull(result);
        assertEquals(50L, result.get("avg_reviews"));
        assertEquals(4.5, result.get("total_reviewers"));

        verify(courseReviewRepository, times(1)).findAllCoursesReviewsOfAnInstructor(1L);
    }

    @Test
    @DisplayName("Test fetchAllReviewsForAnInstructorCourses - No Reviews Found")
    void testFetchAllReviewsForAnInstructorCourses_NoReviewsFound() {

        when(courseReviewRepository.findAllCoursesReviewsOfAnInstructor(1L)).thenReturn(null);
        Tuple result = courseReviewService.fetchAllReviewsForAnInstructorCourses(1L);
        assertNull(result);
        verify(courseReviewRepository, times(1)).findAllCoursesReviewsOfAnInstructor(1L);
    }

    @Test
    @DisplayName("Test fetchAllReviewsForAnInstructorCourses - Exception Handling")
    void testFetchAllReviewsForAnInstructorCourses_ExceptionHandling() {

        when(courseReviewRepository.findAllCoursesReviewsOfAnInstructor(1L)).thenThrow(new RuntimeException("Database error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseReviewService.fetchAllReviewsForAnInstructorCourses(1L);
        });
        assertEquals("Database error", exception.getMessage());
        verify(courseReviewRepository, times(1)).findAllCoursesReviewsOfAnInstructor(1L);
    }

    @Test
    @DisplayName("Test findStudentFeedbackByCourseId - Success")
    void testFindStudentFeedbackByCourseId_Success() throws Exception {

        when(courseReviewRepository.findStudentFeedbackByCourseId(COURSE_ID)).thenReturn(rawData);
        when(courseReviewRepository.findByCourseId(COURSE_ID, PageRequest.of(0, 10))).thenReturn(pageData);

        Message<CourseFeedbackResponse> response = courseReviewService.findStudentFeedbackByCourseId(COURSE_ID, 0, 10);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully fetched course feedback.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().getPageNo());
        assertEquals(10, response.getData().getPageSize());
        assertEquals(1L, response.getData().getTotalElements());
        assertEquals(1, response.getData().getTotalPages());

        CourseFeedback feedback = response.getData().getFeedback();
        assertNotNull(feedback);
        assertEquals(999.0, feedback.getRating5());
        assertEquals(10.0, feedback.getTotalUsers());
        assertEquals(1, feedback.getFeedbackComments().size());
        assertEquals(COMMENT, feedback.getFeedbackComments().get(0).getComment());
        assertEquals("John Doe", feedback.getFeedbackComments().get(0).getUserName());

        verify(courseReviewRepository, times(1)).findStudentFeedbackByCourseId(COURSE_ID);
        verify(courseReviewRepository, times(1)).findByCourseId(COURSE_ID, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Test findStudentFeedbackByCourseId - No Feedback Found")
    void testFindStudentFeedbackByCourseId_NoFeedbackFound() {

        when(courseReviewRepository.findStudentFeedbackByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseReviewService.findStudentFeedbackByCourseId(COURSE_ID, 0, 10);
        });

        assertEquals("No feedback is found for the course.", exception.getMessage());

        verify(courseReviewRepository, times(1)).findStudentFeedbackByCourseId(COURSE_ID);
        verify(courseReviewRepository, never()).findByCourseId(anyLong(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Test findByCourseId - Success")
    void testFindByCourseId_Success() throws Exception {

        review.setId(REVIEW_ID);

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(courseReviewRepository.findByCreatedByAndCourseId(UserTestData.userData().getId(), COURSE_ID)).thenReturn(review);
        when(courseReviewRepository.countByCourse_Id(COURSE_ID)).thenReturn(10L);

        Message<CourseReviewResponse> response = courseReviewService.findByCourseId(COURSE_ID, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Course review fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getCourseReviewId());
        assertEquals(1L, response.getData().getCourseId());
        assertEquals(COMMENT, response.getData().getComment());
        assertEquals(4.0, response.getData().getValue());
        assertEquals(10L, response.getData().getTotalReviews());

        verify(courseReviewRepository, times(1)).findByCreatedByAndCourseId(UserTestData.userData().getId(), COURSE_ID);
        verify(courseReviewRepository, times(1)).countByCourse_Id(COURSE_ID);
    }

    @Test
    @DisplayName("Test findByCourseId - User Not Enrolled")
    void testFindByCourseId_UserNotEnrolled() {

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseReviewService.findByCourseId(COURSE_ID, EMAIL);
        });

        assertEquals("You are not enrolled in this course please enroll in the course first.", exception.getMessage());

        verify(courseReviewRepository, never()).findByCreatedByAndCourseId(anyLong(), anyLong());
        verify(courseReviewRepository, never()).countByCourse_Id(anyLong());
    }

    @Test
    @DisplayName("Test findByCourseId - User Has No Subscription")
    void testFindByCourseId_UserHasNoSubscription() throws EntityNotFoundException {

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseReviewService.findByCourseId(COURSE_ID, EMAIL);
        });

        assertEquals("No plan is subscribed by user: "+EMAIL, exception.getMessage());

        verify(courseReviewRepository, never()).findByCreatedByAndCourseId(anyLong(), anyLong());
        verify(courseReviewRepository, never()).countByCourse_Id(anyLong());
    }

    @Test
    @DisplayName("Test findByCourseId - Course Review Not Found")
    void testFindByCourseId_ReviewNotFound() throws EntityNotFoundException {

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(courseReviewRepository.findByCreatedByAndCourseId(UserTestData.userData().getId(), COURSE_ID)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseReviewService.findByCourseId(COURSE_ID, EMAIL);
        });

        assertEquals("Course review is not found for this course by user.", exception.getMessage());

        verify(courseReviewRepository, times(1)).findByCreatedByAndCourseId(UserTestData.userData().getId(), COURSE_ID);
        verify(courseReviewRepository, never()).countByCourse_Id(anyLong());
    }

    @Test
    @DisplayName("Test createReview - Success for New Review")
    void testCreateReview_Success_NewReview() throws Exception {
        review.setId(REVIEW_ID);
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(courseReviewRepository.findByCreatedByAndCourseId(subscribedUser.getUser().getId(), COURSE_ID)).thenReturn(null);
        when(courseReviewRepository.save(any(CourseReview.class))).thenReturn(review);
        when(courseReviewRepository.findByReviewId(Optional.of(review).get().getId())).thenReturn(this.reviewMock);
        when(courseReviewRepository.save(Optional.of(review).get())).thenReturn(review);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl());
        when(courseReviewRepository.findByReviewId(Optional.of(review).get().getId())).thenReturn(this.reviewMock);
        Message<FeedbackComment> response = courseReviewService.createReview(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Review is given successfully.", response.getMessage());

        verify(courseReviewRepository, times(1)).save(any(CourseReview.class));
        verify(producer, times(1)).sendMessage(
                eq(COURSE_TITLE),
                anyString(),
                eq(EMAIL),
                eq(UserTestData.userData().getId()),
                isNull(),
                eq(NotificationContentType.TEXT),
                eq(NotificationType.COURSE_REVIEW),
                anyLong()
        );
    }

    @Test
    @DisplayName("Test createReview - Success for Updating Existing Review")
    void testCreateReview_Success_UpdateReview() throws Exception {
        review.setId(REVIEW_ID);
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(courseReviewRepository.findByCreatedByAndCourseId(UserTestData.userData().getId(), COURSE_ID)).thenReturn(review);
        when(courseReviewRepository.findByReviewId(Optional.of(review).get().getId())).thenReturn(this.reviewMock);
        when(courseReviewRepository.save(Optional.of(review).get())).thenReturn(review);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-course").build());
        when(courseReviewRepository.findByReviewId(Optional.of(review).get().getId())).thenReturn(this.reviewMock);

        Message<FeedbackComment> response = courseReviewService.createReview(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Review is given successfully.", response.getMessage());

        verify(courseReviewRepository, times(1)).save(any(CourseReview.class));
        verify(producer, times(1)).sendMessage(
                eq(COURSE_TITLE),
                eq("student/course-content/test-course"),
                eq(EMAIL),
                eq(UserTestData.userData().getId()),
                isNull(),
                eq(NotificationContentType.TEXT),
                eq(NotificationType.COURSE_REVIEW_UPDATED),
                anyLong()
        );
    }

    @Test
    @DisplayName("Test createReview - User Not Enrolled")
    void testCreateReview_UserNotEnrolled() {

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseReviewService.createReview(request, EMAIL);
        });

        assertEquals("You are not enrolled in this course please enroll in the course first.", exception.getMessage());

        verify(courseReviewRepository, never()).save(any(CourseReview.class));
        verify(producer, never()).sendMessage(any(), any(), any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("Test createReview - User Has No Subscription")
    void testCreateReview_UserHasNoSubscription() throws EntityNotFoundException {

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseReviewService.createReview(request, EMAIL);
        });

        assertEquals("No plan is subscribed by user: "+EMAIL, exception.getMessage());

        verify(courseReviewRepository, never()).save(any(CourseReview.class));
        verify(producer, never()).sendMessage(any(), any(), any(), any(),any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("Test createReview - Course Not Found")
    void testCreateReview_CourseNotFound() throws EntityNotFoundException {

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(courseService.findById(COURSE_ID)).thenThrow(new EntityNotFoundException("Course not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseReviewService.createReview(request, EMAIL);
        });

        assertEquals("Course not found", exception.getMessage());

        verify(courseReviewRepository, never()).save(any(CourseReview.class));
        verify(producer, never()).sendMessage(any(), any(), any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("Test createReview - Internal Server Exception During Save")
    void testCreateReview_InternalServerExceptionDuringSave() throws EntityNotFoundException {

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(subscribedUser);
        when(courseService.findById(1L)).thenReturn(course);
        when(courseReviewRepository.save(any(CourseReview.class))).thenThrow(new RuntimeException("Database error"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            courseReviewService.createReview(request, EMAIL);
        });

        assertEquals("Course review " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());

        verify(courseReviewRepository, times(1)).save(any(CourseReview.class));
        verify(producer, never()).sendMessage(any(), any(), any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("Should fetch all feedback for instructor's courses when courseId is null")
    void fetchAllFeedbackForInstructorCourses() throws EntityNotFoundException, EntityNotFoundException, BadRequestException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findStudentFeedbackForCoursesOfInstructor(UserTestData.userData().getId()))
                .thenReturn(Collections.singletonList(tuple));
        when(courseReviewRepository.findAllCoursesReviewsOfAnInstructor(eq(UserTestData.userData().getId()), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(tuple)));

        when(tuple.get("rating")).thenReturn(5.0);
        when(tuple.get("users")).thenReturn(10.0);

        Message<CourseFeedbackResponse> response = courseReviewService.fetchStudentFeedbackOnCoursesOfInstructor(EMAIL, null, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully fetched course feedback.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Should fetch feedback for a specific course")
    void fetchFeedbackForSpecificCourse() throws EntityNotFoundException, BadRequestException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findStudentFeedbackByCourseId(COURSE_ID)).thenReturn(Collections.singletonList(tuple));
        when(courseReviewRepository.findAllCoursesReviewsOfAnInstructor(eq(UserTestData.userData().getId()), eq(COURSE_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(tuple)));

        when(tuple.get("rating")).thenReturn(5.0);
        when(tuple.get("users")).thenReturn(10.0);

        Message<CourseFeedbackResponse> response = courseReviewService.fetchStudentFeedbackOnCoursesOfInstructor(EMAIL, COURSE_ID, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully fetched course feedback.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Should return empty feedback list when no feedback found for instructor's courses")
    void noFeedbackFoundForInstructorCourses() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findStudentFeedbackForCoursesOfInstructor(UserTestData.userData().getId())).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () ->
                        courseReviewService.fetchStudentFeedbackOnCoursesOfInstructor(EMAIL, null, 0, 10),
                "No feedback is found for the course."
        );
    }

    @Test
    @DisplayName("Should return empty feedback list when no feedback found for specific course")
    void noFeedbackFoundForSpecificCourse() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findStudentFeedbackByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () ->
                        courseReviewService.fetchStudentFeedbackOnCoursesOfInstructor(EMAIL, COURSE_ID, 0, 10),
                "No feedback is found for the course."
        );
    }

    @Test
    @DisplayName("Should handle invalid page number or page size gracefully")
    void invalidPageNumberOrSize() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseReviewRepository.findStudentFeedbackForCoursesOfInstructor(UserTestData.userData().getId()))
                .thenReturn(Collections.singletonList(tuple));
        when(courseReviewRepository.findAllCoursesReviewsOfAnInstructor(eq(UserTestData.userData().getId()), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(BadRequestException.class, () ->
                        courseReviewService.fetchStudentFeedbackOnCoursesOfInstructor(EMAIL, null, -1, 0),
                "Page no or Page size cannot be negative."
        );
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when instructor email is invalid")
    void instructorNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found."));

        assertThrows(EntityNotFoundException.class, () ->
                        courseReviewService.fetchStudentFeedbackOnCoursesOfInstructor(EMAIL, null, 0, 10),
                "User not found."
        );
    }

}
