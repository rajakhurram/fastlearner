package com.vinncorp.fast_learner.mock.quiz;

import com.vinncorp.fast_learner.dtos.quiz.QuizAnswer;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestion;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnswer;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.quiz.QuizAttemptRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizQuestionAnwserRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizQuestionRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import com.vinncorp.fast_learner.response.quiz.QuizAnswerResponse;
import com.vinncorp.fast_learner.response.quiz.QuizQuestionAnswerResponse;
import com.vinncorp.fast_learner.services.quiz.QuizQuestionAnswerService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class QuizQuestionAnswerServiceMockTest {

    private static Long QUESTION_ID = 1L;
    private static Long ANSWER_ID = 1L;
    private static String EMAIL = "qasim@mailinator.com";
    @Mock
    private QuizQuestionAnwserRepository repo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @InjectMocks
    private QuizQuestionAnswerService service;

    private List<ValidationAnswerRequest> validationAnswerRequests;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should validate the answer as correct when the provided answer is correct")
    void testValidateAnswerCorrect() throws BadRequestException, EntityNotFoundException {
        QuizQuestionAnwser answer1 = QuizTestData.quizQuestionAnwser();
        answer1.setId(1L);
        answer1.setCorrectAnswer(true);
        QuizQuestionAnwser answer2 = QuizTestData.quizQuestionAnwser();
        answer2.setId(2L);
        answer2.setCorrectAnswer(false);
        List<QuizQuestionAnwser> answers = List.of(answer1, answer2);
        when(repo.findByQuizQuestionId(QUESTION_ID)).thenReturn(answers);
        Message<QuizQuestionAnswerResponse> result = service.validateAnswer(QUESTION_ID, ANSWER_ID, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Answer is correct.", result.getMessage());
        assertTrue(result.getData().getIsCorrect());
        assertEquals(1L, result.getData().getCorrectAnswerId());
        verify(repo, times(1)).findByQuizQuestionId(QUESTION_ID);
    }

    @Test
    @DisplayName("Should throw BadRequestException when answerId is null")
    void testValidateAnswerWithNullAnswerId() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            service.validateAnswer(QUESTION_ID, null, EMAIL);
        });
        assertEquals("Please provide answer ID.", exception.getMessage());
        verify(repo, never()).findByQuizQuestionId(anyLong());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no quiz question answers are found for the provided question ID")
    void testValidateAnswerWithNoQuizQuestionAnswersFound() {
        when(repo.findByQuizQuestionId(QUESTION_ID)).thenReturn(Collections.emptyList());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.validateAnswer(QUESTION_ID, ANSWER_ID, EMAIL);
        });
        assertEquals("No data found for provided question and answer id.", exception.getMessage());
        verify(repo, times(1)).findByQuizQuestionId(QUESTION_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when the provided answer ID does not match any answer")
    void testValidateAnswerWithInvalidAnswerId() {
        QuizQuestionAnwser correctAnswer = QuizQuestionAnwser.builder()
                .id(2L)
                .isCorrectAnswer(false)
                .build();
        QuizQuestionAnwser anotherAnswer = QuizQuestionAnwser.builder()
                .id(3L)
                .isCorrectAnswer(true)
                .build();
        List<QuizQuestionAnwser> answers = List.of(correctAnswer, anotherAnswer);
        when(repo.findByQuizQuestionId(QUESTION_ID)).thenReturn(answers);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.validateAnswer(QUESTION_ID, ANSWER_ID, EMAIL);
        });

        assertEquals("Question doesn't have any answer by provided id.", exception.getMessage());
        verify(repo, times(1)).findByQuizQuestionId(QUESTION_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no correct answer is found for the question")
    void testValidateAnswerWhenNoCorrectAnswerExists() {
        QuizQuestionAnwser firstAnswer = QuizQuestionAnwser.builder()
                .id(1L)
                .isCorrectAnswer(false)
                .build();
        QuizQuestionAnwser secondAnswer = QuizQuestionAnwser.builder()
                .id(2L)
                .isCorrectAnswer(false)
                .build();
        List<QuizQuestionAnwser> answers = List.of(firstAnswer, secondAnswer);
        when(repo.findByQuizQuestionId(QUESTION_ID)).thenReturn(answers);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.validateAnswer(QUESTION_ID, ANSWER_ID, EMAIL);
        });
        assertEquals("Correct answer is not present for the question.", exception.getMessage());
        verify(repo, times(1)).findByQuizQuestionId(QUESTION_ID);
    }

    @Test
    @DisplayName("Should save a new quiz question answer successfully")
    void testSaveQuizQuestionAnswerSuccess() throws InternalServerException, BadRequestException {
        when(repo.save(any(QuizQuestionAnwser.class))).thenReturn(QuizTestData.quizQuestionAnwser());
        QuizQuestionAnwser savedAnswer = service.save(QuizTestData.quizQuestionAnwser());
        assertNotNull(savedAnswer);
        assertEquals("Sample Answer", savedAnswer.getAnswer());
        verify(repo, times(1)).save(QuizTestData.quizQuestionAnwser());
    }

    @Test
    @DisplayName("Should delete the quiz question answer when delete flag is true")
    void testDeleteQuizQuestionAnswer() throws InternalServerException, BadRequestException {
        QuizQuestionAnwser quizQuestionAnwser = QuizTestData.quizQuestionAnwser();
        quizQuestionAnwser.setId(1L);
        quizQuestionAnwser.setDelete(true);
        QuizQuestionAnwser result = service.save(quizQuestionAnwser);
        assertNull(result);
        verify(repo, times(1)).deleteById(quizQuestionAnwser.getId());
        verify(repo, never()).save(any(QuizQuestionAnwser.class));
    }

    @Test
    @DisplayName("Should throw InternalServerException when save operation fails")
    void testSaveQuizQuestionAnswerFailure() {
        when(repo.save(any(QuizQuestionAnwser.class))).thenThrow(new RuntimeException("Database error"));
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.save(QuizTestData.quizQuestionAnwser());
        });
        assertEquals("Quiz question answer" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).save(QuizTestData.quizQuestionAnwser());
    }

    @Test
    @DisplayName("Should handle null ID with delete flag correctly")
    void testSaveWithNullIdAndDeleteFlag() throws InternalServerException, BadRequestException {
        QuizQuestionAnwser quizQuestionAnwser = QuizTestData.quizQuestionAnwser();
        quizQuestionAnwser.setDelete(true);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            service.save(quizQuestionAnwser);
        });

        assertEquals("Answer id cannot be empty when delete", exception.getMessage());
    }

    @Test
    @DisplayName("Should save when delete flag is false and ID is non-null")
    void testSaveWithNonNullIdAndDeleteFlagFalse() throws InternalServerException, BadRequestException {
        QuizQuestionAnwser quizQuestionAnwser = QuizTestData.quizQuestionAnwser();
        quizQuestionAnwser.setId(2L);
        quizQuestionAnwser.setAnswer("Updated Answer");

        when(repo.save(any(QuizQuestionAnwser.class))).thenReturn(quizQuestionAnwser);
        QuizQuestionAnwser savedAnswer = service.save(quizQuestionAnwser);
        assertNotNull(savedAnswer);
        assertEquals("Updated Answer", savedAnswer.getAnswer());
        verify(repo, times(1)).save(quizQuestionAnwser);
        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should validate answers successfully")
    void testValidateAnswers_Success() throws BadRequestException {
        String email = "instructor@test.com";
        Long userId = 1L;
        Long quizId = 100L;
        Long questionId = 200L;
        Long answerId = 300L;

        User user = UserTestData.userData();
        user.setId(userId);
        user.setEmail(email);

        ValidationAnswerRequest request = new ValidationAnswerRequest();
        request.setQuestionId(questionId);
        request.setAnswerId(List.of(answerId));

        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setRandomQuestion(1);

        QuizAnswer correctAnswer = new QuizAnswer();
        correctAnswer.setAnswerId(answerId);
        correctAnswer.setAnswerText("Java is a programming language.");
        correctAnswer.setIsCorrect(true);

        QuizAnswer correctAnswer1 = new QuizAnswer();
        correctAnswer1.setAnswerId(answerId);
        correctAnswer1.setAnswerText("Java is a hardware component.");
        correctAnswer1.setIsCorrect(false);

        QuizAnswer correctAnswer2 = new QuizAnswer();
        correctAnswer2.setAnswerId(answerId);
        correctAnswer2.setAnswerText("Java is a tea type.");
        correctAnswer2.setIsCorrect(false);

        com.vinncorp.fast_learner.models.quiz.QuizQuestion quizQuestion = new com.vinncorp.fast_learner.models.quiz.QuizQuestion();
        quizQuestion.setId(questionId);
        quizQuestion.setQuestionText("What is Java?");
        quizQuestion.setExplanation("Java is a programming language.");
        quizQuestion.setQuestionType(QuestionType.SINGLE_CHOICE);
        quizQuestion.setQuiz(quiz);

        QuizQuestionAnwser quizQuestionAnswer = new QuizQuestionAnwser();
        quizQuestionAnswer.setId(answerId);
        quizQuestionAnswer.setAnswer("Java is a programming language.");
        quizQuestionAnswer.setCorrectAnswer(true);
        quizQuestionAnswer.setQuizQuestion(quizQuestion);

        QuizQuestionAnwser quizQuestionAnswer1 = new QuizQuestionAnwser();
        quizQuestionAnswer1.setId(answerId);
        quizQuestionAnswer1.setAnswer("Java is a hardware component.");
        quizQuestionAnswer1.setCorrectAnswer(false);
        quizQuestionAnswer1.setQuizQuestion(quizQuestion);

        QuizQuestionAnwser quizQuestionAnswer2 = new QuizQuestionAnwser();
        quizQuestionAnswer2.setId(answerId);
        quizQuestionAnswer2.setAnswer("Java is a tea type.");
        quizQuestionAnswer2.setCorrectAnswer(false);
        quizQuestionAnswer2.setQuizQuestion(quizQuestion);


        QuizAttempt existingAttempt = new QuizAttempt();
        existingAttempt.setId(999L);
        existingAttempt.setTotalAttemptCount(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(repo.findPassingCriteriaAndQuizIdByQuestionId(questionId)).thenReturn(50L);
        when(repo.findByQuizQuestionId(questionId)).thenReturn(List.of(quizQuestionAnswer, quizQuestionAnswer1, quizQuestionAnswer2));
        when(quizQuestionRepository.findByQuizIdAndNotInQuestionId(quizId, List.of(questionId))).thenReturn(Collections.emptyList());
        when(quizAttemptRepository.findByQuizAndUser(quizId, userId)).thenReturn(existingAttempt);
        when(quizAttemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Message<QuizAnswerResponse> result = service.validateAnswers(List.of(request), email);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Quiz Response fetch successfully", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(100.0, result.getData().getPercentage());
        assertEquals(1L, result.getData().getTotalCorrectAnswer());
    }

    @Test
    @DisplayName("Should return NOT_FOUND when instructor is not found")
    void testValidateAnswers_InstructorNotFound() throws BadRequestException {
        when(userRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        Message<QuizAnswerResponse> result = service.validateAnswers(List.of(new ValidationAnswerRequest()), "invalid@example.com");

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("Instructor not found with email: invalid@example.com", result.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when request is invalid")
    void testValidateAnswers_InvalidRequest_ThrowsException() {
        when(userRepository.findByEmail("any@email.com")).thenReturn(Optional.of(UserTestData.userData()));
        assertThrows(BadRequestException.class, () ->
                service.validateAnswers(Collections.emptyList(), "any@email.com")
        );
    }

    @Test
    @DisplayName("Should throw RuntimeException when passing criteria is not found")
    void testValidateAnswers_MissingPassingCriteria_ThrowsException() {
        ValidationAnswerRequest request = new ValidationAnswerRequest();
        request.setQuestionId(1L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));
        when(repo.findPassingCriteriaAndQuizIdByQuestionId(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                service.validateAnswers(List.of(request), "test@example.com")
        );
    }
}
