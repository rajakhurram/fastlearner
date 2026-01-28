package com.vinncorp.fast_learner.mock.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.repositories.quiz.QuizRepository;
import com.vinncorp.fast_learner.services.quiz.QuizService;
import com.vinncorp.fast_learner.util.Message;
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

public class QuizServiceMockTest {

    private static Long QUIZ_ID = 1L;
    @InjectMocks
    QuizService quizService;
    @Mock
    QuizRepository repo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test delete quiz by ID - Success")
    void testDeleteQuizById_Success() throws Exception {
        doNothing().when(repo).deleteById(QUIZ_ID);
        Message<String> response = quizService.deleteQuizById(QUIZ_ID);
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Quiz is deleted successfully.", response.getData());
        verify(repo, times(1)).deleteById(QUIZ_ID);
    }

    @Test
    @DisplayName("Test delete quiz by ID - Handle repository delete error")
    void testDeleteQuizById_HandleDeleteError() {
        doThrow(new RuntimeException("Delete error")).when(repo).deleteById(QUIZ_ID);
        InternalServerException exception = assertThrows(InternalServerException.class, () -> quizService.deleteQuizById(QUIZ_ID));
        assertEquals("Quiz " + InternalServerException.NOT_DELETE_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).deleteById(QUIZ_ID);
    }

    @Test
    @DisplayName("Test save quiz - Success")
    void testSaveQuiz_Success() throws Exception {
        when(repo.save(QuizTestData.quizData())).thenReturn(QuizTestData.quizData());
        Quiz savedQuiz = quizService.save(QuizTestData.quizData());
        assertNotNull(savedQuiz);
        assertEquals(QuizTestData.quizData().getTitle(), savedQuiz.getTitle());
        verify(repo, times(1)).save(QuizTestData.quizData());
    }

    @Test
    @DisplayName("Test save quiz - Delete existing quiz")
    void testSaveQuiz_DeleteExistingQuiz() throws Exception {
        Quiz quiz = QuizTestData.quizData();
        quiz.setDelete(true);
        Quiz deletedQuiz = quizService.save(quiz);
        assertNull(deletedQuiz);
        verify(repo, times(1)).deleteById(quiz.getId());
        verify(repo, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Test save quiz - Handle repository save error")
    void testSaveQuiz_HandleSaveError() {
        when(repo.save(QuizTestData.quizData())).thenThrow(new RuntimeException("Database error"));
        InternalServerException exception = assertThrows(InternalServerException.class, () -> quizService.save(QuizTestData.quizData()));
        assertEquals("Quiz"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).save(QuizTestData.quizData());
    }

    @Test
    @DisplayName("Test save quiz - Handle repository delete error")
    void testSaveQuiz_HandleDeleteError() {
        Quiz quiz = QuizTestData.quizData();
        quiz.setDelete(true);
        doThrow(new RuntimeException("Delete error")).when(repo).deleteById(quiz.getId());
        InternalServerException exception = assertThrows(InternalServerException.class, () -> quizService.save(quiz));
        assertEquals("Quiz"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).deleteById(quiz.getId());
        verify(repo, never()).save(any(Quiz.class));
    }

}
