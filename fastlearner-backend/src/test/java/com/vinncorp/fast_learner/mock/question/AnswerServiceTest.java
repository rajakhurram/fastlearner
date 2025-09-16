package com.vinncorp.fast_learner.mock.question;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.question_answer.Answer;
import com.vinncorp.fast_learner.models.question_answer.Question;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.question_answer.AnswerRepository;
import com.vinncorp.fast_learner.request.question_answer.AnswerRequest;
import com.vinncorp.fast_learner.response.question_answer.AnswerResponse;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.notification.StudentQnADiscussion;
import com.vinncorp.fast_learner.services.question_answer.AnswerService;
import com.vinncorp.fast_learner.services.question_answer.IQuestionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Optional;

import static com.vinncorp.fast_learner.mock.course.CourseVisitorServiceMockTest.COURSE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class AnswerServiceTest {

    @Mock
    private AnswerRepository repo;

    @Mock
    private IQuestionService questionService;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private IEnrollmentService enrollmentService;

    @Mock
    private StudentQnADiscussion studentQnADiscussion;
    @Mock
    private ICourseUrlService courseUrlService;

    @InjectMocks
    private AnswerService answerService;

    public AnswerServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate_Success() throws BadRequestException, EntityNotFoundException, InternalServerException, IOException {
        // Setup mocks
        AnswerRequest request = new AnswerRequest();
        request.setCourseId(1L); // Use Long here
        request.setQuestionId(1L);
        request.setText("Sample answer");

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(UserTestData.userData()); // Ensure User is appropriately set

        when(enrollmentService.isEnrolled(anyLong(), anyString())).thenReturn(true);
        when(subscribedUserService.findByUser(anyString())).thenReturn(subscribedUser);
        Question question = new Question();
        question.setCourse(CourseTestData.courseData());
        question.setCreatedBy(1L);
        when(questionService.findById(anyLong())).thenReturn(Optional.of(question));
        when(repo.save(any(Answer.class))).thenReturn(new Answer());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());
        doNothing().when(studentQnADiscussion).notifyToUserQnAReply(anyString(), anyString(), any(),
                anyLong(), anyLong());

        // Execute the service method
        Message<String> response = answerService.create(request, "user@example.com");

        // Verify results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Answer saved successfully.", response.getMessage());
    }

    @Test
    void testCreate_NotEnrolled() {
        AnswerRequest request = new AnswerRequest();
        request.setCourseId(1L); // Use Long here

        when(enrollmentService.isEnrolled(anyLong(), anyString())).thenReturn(false);

        // Execute and verify exception
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            answerService.create(request, "user@example.com");
        });
        assertEquals("You are not enrolled in this course please enrolled in the course first.", thrown.getMessage());
    }

    @Test
    void testCreate_NoSubscribedUser() throws EntityNotFoundException {
        AnswerRequest request = new AnswerRequest();
        request.setCourseId(1L); // Use Long here
        request.setQuestionId(1L); // Use Long here
        request.setText("Sample answer");

        when(enrollmentService.isEnrolled(anyLong(), anyString())).thenReturn(true);
        when(subscribedUserService.findByUser(anyString())).thenReturn(null);

        // Execute and verify exception
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            answerService.create(request, "user@example.com");
        });
        assertEquals("No plan is subscribed by user: user@example.com", thrown.getMessage());
    }

    @Test
    void testCreate_InternalServerError() throws BadRequestException, EntityNotFoundException, InternalServerException {
        AnswerRequest request = new AnswerRequest();
        request.setCourseId(1L); // Use Long here
        request.setQuestionId(1L); // Use Long here
        request.setText("Sample answer");

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User()); // Ensure User is appropriately set

        when(enrollmentService.isEnrolled(anyLong(), anyString())).thenReturn(true);
        when(subscribedUserService.findByUser(anyString())).thenReturn(subscribedUser);
        when(questionService.findById(anyLong())).thenReturn(Optional.of(new Question()));
        when(repo.save(any(Answer.class))).thenThrow(new RuntimeException("Database error"));

        // Execute and verify exception
        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            answerService.create(request, "user@example.com");
        });
        assertEquals("Answer "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, thrown.getMessage());
    }
    @Test
    void testCreate_CourseIdNull() throws EntityNotFoundException {
        // Setup request
        AnswerRequest request = new AnswerRequest();
        request.setCourseId(null);
        request.setQuestionId(1L);
        request.setText("Sample answer");

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            answerService.create(request, "user@example.com");
        });
        assertEquals("Course id Cannot be Null", thrown.getMessage());
    }
    @Test
    void testGetAllAnswerWithPagination_Success() throws BadRequestException, EntityNotFoundException {
        Long courseId = 1L;
        Long questionId = 1L;
        int pageNo = 0;
        int pageSize = 10;
        String email = "user@example.com";

        // Setup mocks
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User()); // Assume User has a method getFullName()
        User user = subscribedUser.getUser();
        user.setFullName("John Doe");

        List<Tuple> rawDataList = Collections.singletonList(mock(Tuple.class));
        Page<Tuple> rawDataPage = new PageImpl<>(rawDataList, PageRequest.of(pageNo, pageSize), 1);

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllAnswerByCourseIdAndQuestionId(courseId, questionId, PageRequest.of(pageNo, pageSize)))
                .thenReturn(rawDataPage);

        // Execute the service method
        Message<AnswerResponse> response = answerService.getAllAnswerWithPagination(courseId, questionId, pageNo, pageSize, email);

        // Verify results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("All answer are fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(pageNo, response.getData().getPageNo());
        assertEquals(pageSize, response.getData().getPageSize());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals(1, response.getData().getTotalPages());
    }

    @Test
    void testGetAllAnswerWithPagination_UserNotEnrolled() {
        Long courseId = 1L;
        Long questionId = 1L;
        int pageNo = 0;
        int pageSize = 10;
        String email = "user@example.com";

        // Setup mocks
        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(false);

        // Execute and verify exception
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            answerService.getAllAnswerWithPagination(courseId, questionId, pageNo, pageSize, email);
        });
        assertEquals("You are not enrolled in this course please enrolled in the course first.", thrown.getMessage());
    }

    @Test
    void testGetAllAnswerWithPagination_NoSubscriptionPlan() throws EntityNotFoundException {
        Long courseId = 1L;
        Long questionId = 1L;
        int pageNo = 0;
        int pageSize = 10;
        String email = "user@example.com";

        // Setup mocks
        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(null);

        // Execute and verify exception
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            answerService.getAllAnswerWithPagination(courseId, questionId, pageNo, pageSize, email);
        });
        assertEquals("No plan is subscribed by user: user@example.com", thrown.getMessage());
    }

    @Test
    void testGetAllAnswerWithPagination_NoAnswersFound() throws EntityNotFoundException {
        Long courseId = 1L;
        Long questionId = 1L;
        int pageNo = 0;
        int pageSize = 10;
        String email = "user@example.com";

        // Setup mocks
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User()); // Assume User has a method getFullName()
        User user = subscribedUser.getUser();
        user.setFullName("John Doe");

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllAnswerByCourseIdAndQuestionId(courseId, questionId, PageRequest.of(pageNo, pageSize)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(pageNo, pageSize), 0));

        // Execute and verify exception
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            answerService.getAllAnswerWithPagination(courseId, questionId, pageNo, pageSize, email);
        });
        assertEquals("No answers are present.", thrown.getMessage());
    }
}

