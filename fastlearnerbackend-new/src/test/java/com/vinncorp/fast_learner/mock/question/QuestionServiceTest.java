package com.vinncorp.fast_learner.mock.question;

import com.vinncorp.fast_learner.services.question_answer.QuestionService;
import com.vinncorp.fast_learner.dtos.question_answer.QuestionDetail;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.question_answer.Question;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.question_answer.QuestionRepository;
import com.vinncorp.fast_learner.request.question_answer.QuestionRequest;
import com.vinncorp.fast_learner.response.question_answer.QuestionResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.notification.StudentQnADiscussion;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;

class QuestionServiceTest {

    @Mock
    private QuestionRepository repo;
    @Mock
    private ISubscribedUserService subscribedUserService;
    @Mock
    private IEnrollmentService enrollmentService;
    @Mock
    private ITopicService topicService;
    @Mock
    private ICourseService courseService;
    @Mock
    private StudentQnADiscussion studentQnADiscussion;

    @InjectMocks
    private QuestionService questionService;

    public QuestionServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate_Success() throws Exception {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        Long topicId = 2L;
        Long questionId = 3L;

        QuestionRequest request = new QuestionRequest();
        request.setText("Sample question?");
        request.setCourseId(courseId);
        request.setTopicId(topicId);

        Course course = new Course();
        Topic topic = new Topic();
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User());
        subscribedUser.getUser().setId(1L);
        subscribedUser.getUser().setFullName("User Name");

        // Mock enrollment service and repository
        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(courseService.findById(courseId)).thenReturn(course);
        when(topicService.getTopicById(topicId)).thenReturn(topic);

        // Mock saved question object
        Question question = Question.builder()
                .id(questionId)
                .questionText(request.getText())
                .course(course)
                .topic(topic)
                .build();
        question.setCreatedBy(1L);

        when(repo.save(any(Question.class))).thenReturn(question);

        // Mock Tuple object
        Tuple mockTuple = mock(Tuple.class);
        when(mockTuple.get("question_id")).thenReturn(questionId);
        when(mockTuple.get("question_text")).thenReturn("Sample question?");
        when(mockTuple.get("no_of_answers")).thenReturn(3);
        when(mockTuple.get("topic_id")).thenReturn(topicId);
        when(mockTuple.get("topic_name")).thenReturn("Sample Topic");
        when(mockTuple.get("course_id")).thenReturn(courseId);
        when(mockTuple.get("full_name")).thenReturn("User Name");
        when(mockTuple.get("profile_picture")).thenReturn("profile.jpg");
        when(mockTuple.get("created_by")).thenReturn(1L);

        when(repo.findQuestionById(questionId)).thenReturn(mockTuple);

        // Act
        Message<QuestionDetail> response = questionService.create(request, email);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Question is created successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(questionId, response.getData().getQuestionId());
        assertEquals("Sample question?", response.getData().getQuestionText());
        assertEquals("Sample Topic", response.getData().getTopicName());
        assertEquals("User Name", response.getData().getUserName());
    }


    @Test
    void create_EnrolledButNoPlan() throws BadRequestException, EntityNotFoundException, InternalServerException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;

        QuestionRequest request = new QuestionRequest();
        request.setText("Sample question?");
        request.setCourseId(courseId);

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(null);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            questionService.create(request, email);
        });
        assertEquals("No plan is subscribed by user: " + email, thrown.getMessage());
    }

    @Test
    void create_NotEnrolled() throws BadRequestException, EntityNotFoundException, InternalServerException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;

        QuestionRequest request = new QuestionRequest();
        request.setText("Sample question?");
        request.setCourseId(courseId);

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(false);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            questionService.create(request, email);
        });
        assertEquals("You are not enrolled in this course please enrolled in the course first.", thrown.getMessage());
    }

    @Test
    void create_InternalServerError() throws BadRequestException, EntityNotFoundException, InternalServerException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        Long topicId = 2L;

        QuestionRequest request = new QuestionRequest();
        request.setText("Sample question?");
        request.setCourseId(courseId);
        request.setTopicId(topicId);

        Course course = new Course();
        Topic topic = new Topic();
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User());  // Assuming User class exists
        subscribedUser.getUser().setId(1L);

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(courseService.findById(courseId)).thenReturn(course);
        when(topicService.getTopicById(topicId)).thenReturn(topic);
        when(repo.save(any(Question.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            questionService.create(request, email);
        });
        assertEquals("Question cannot be saved due to database error.", thrown.getMessage());
    }

    @Test
    void create_SubscribedUserNotFound() throws BadRequestException, EntityNotFoundException, InternalServerException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        Long topicId = 2L;

        QuestionRequest request = new QuestionRequest();
        request.setText("Sample question?");
        request.setCourseId(courseId);
        request.setTopicId(topicId);

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(null);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            questionService.create(request, email);
        });
        assertEquals("No plan is subscribed by user: " + email, thrown.getMessage());
    }

    @Test
    void findAllQuestionsWithPagination_Success() throws BadRequestException, EntityNotFoundException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User());
        subscribedUser.getUser().setFullName("John Doe");

        Tuple tuple = mock(Tuple.class); // Mock Tuple or use a real instance if needed
        List<Tuple> tuples = Collections.singletonList(tuple);
        Page<Tuple> page = new PageImpl<>(tuples, PageRequest.of(pageNo, pageSize), 1);

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllQuestionsByCourse(eq(courseId), any(PageRequest.class))).thenReturn(page);

        // Act
        Message<QuestionResponse> response = questionService.findAllQuestionsWithPagination(courseId, pageNo, pageSize, email);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetched all questions successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(pageNo, response.getData().getPageNo());
        assertEquals(pageSize, response.getData().getPageSize());
        assertEquals(page.getTotalElements(), response.getData().getTotalElements());
        assertEquals(page.getTotalPages(), response.getData().getTotalPages());
    }

    @Test
    void findAllQuestionsWithPagination_UserNotEnrolled() {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(false);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            questionService.findAllQuestionsWithPagination(courseId, pageNo, pageSize, email);
        });
        assertEquals("You are not enrolled in this course please enrolled in the course first.", thrown.getMessage());
    }

    @Test
    void findAllQuestionsWithPagination_NoSubscriptionPlan() throws EntityNotFoundException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(null);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            questionService.findAllQuestionsWithPagination(courseId, pageNo, pageSize, email);
        });
        assertEquals("No plan is subscribed by user: " + email, thrown.getMessage());
    }

    @Test
    void findAllQuestionsWithPagination_NoQuestionsFound() throws EntityNotFoundException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User());
        subscribedUser.getUser().setFullName("John Doe");

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllQuestionsByCourse(eq(courseId), any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            questionService.findAllQuestionsWithPagination(courseId, pageNo, pageSize, email);
        });
        assertEquals("No questions are present for this course.", thrown.getMessage());
    }


    @Test
    void findAllQuestionsWithPagination_RepositoryFailure() throws EntityNotFoundException {
        // Arrange
        String email = "user@example.com";
        Long courseId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setUser(new User());
        subscribedUser.getUser().setFullName("John Doe");

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllQuestionsByCourse(eq(courseId), any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            questionService.findAllQuestionsWithPagination(courseId, pageNo, pageSize, email);
        });
        assertEquals("Database error", thrown.getMessage());
    }
    @Test
    void testFindById_Success() {
        Long questionId = 1L;
        Question question = new Question();
        question.setId(questionId);
        question.setQuestionText("Sample question");  // Using a setter if available, or the constructor

        // Mock repository behavior
        when(repo.findById(questionId)).thenReturn(Optional.of(question));

        // Execute the service method
        Optional<Question> result = questionService.findById(questionId);

        // Verify the result
        assertTrue(result.isPresent(), "Question should be present");
        assertEquals(questionId, result.get().getId(), "Question ID should match");
        assertEquals("Sample question", result.get().getQuestionText(), "Question text should match");
    }

    @Test
    void testFindById_DifferentQuestion() {
        Long questionId = 2L;
        Question question = new Question();
        question.setId(questionId);
        question.setQuestionText("Another sample question");

        when(repo.findById(questionId)).thenReturn(Optional.of(question));

        Optional<Question> result = questionService.findById(questionId);

        assertTrue(result.isPresent(), "Question should be present");
        assertEquals(questionId, result.get().getId(), "Question ID should match");
        assertEquals("Another sample question", result.get().getQuestionText(), "Question text should match");
    }

    @Test
    void testFindById_QuestionNotFound() {
        Long questionId = 1L;

        when(repo.findById(questionId)).thenReturn(Optional.empty());

        Optional<Question> result = questionService.findById(questionId);

        assertFalse(result.isPresent(), "Question should not be present");
    }
    @Test
    void testFindById_NullId() {
        Long questionId = null;

        when(repo.findById(questionId)).thenThrow(new IllegalArgumentException("ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> questionService.findById(questionId), "Should throw IllegalArgumentException");
    }
    @Test
    void testFindById_InvalidId() {
        Long questionId = -1L;

        when(repo.findById(questionId)).thenReturn(Optional.empty());

        Optional<Question> result = questionService.findById(questionId);

        assertFalse(result.isPresent(), "Question should not be present for an invalid ID");
    }

    @Test
    void testFindById_NotFound() {
        Long questionId = 1L;

        // Mock repository behavior
        when(repo.findById(questionId)).thenReturn(Optional.empty());

        // Execute the service method
        Optional<Question> result = questionService.findById(questionId);

        // Verify the result
        assertFalse(result.isPresent(), "Question should not be present");
    }

}
