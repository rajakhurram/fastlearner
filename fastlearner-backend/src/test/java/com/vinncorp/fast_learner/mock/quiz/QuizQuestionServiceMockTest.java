package com.vinncorp.fast_learner.mock.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.repositories.quiz.QuizQuestionRepository;
import com.vinncorp.fast_learner.services.quiz.QuizQuestionService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class QuizQuestionServiceMockTest {

    @Mock
    private QuizQuestionRepository repo;

    @InjectMocks
    private QuizQuestionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test save quiz question - Success")
    void testSaveQuizQuestion_Success() throws Exception {
        when(repo.save(QuizTestData.quizQuestionData())).thenReturn(QuizTestData.quizQuestionData());
        QuizQuestion savedQuizQuestion = service.save(QuizTestData.quizQuestionData());
        assertNotNull(savedQuizQuestion);
        assertEquals(QuizTestData.quizQuestionData().getId(), savedQuizQuestion.getId());
        verify(repo, times(1)).save(QuizTestData.quizQuestionData());
    }

    @Test
    @DisplayName("Test save quiz question - Delete existing question")
    void testSaveQuizQuestion_DeleteExisting() throws Exception {
        QuizQuestion quizQuestion = QuizTestData.quizQuestionData();
        quizQuestion.setDelete(true);
        QuizQuestion result = service.save(quizQuestion);
        assertNull(result);
        verify(repo, times(1)).deleteById(quizQuestion.getId());
        verify(repo, never()).save(any(QuizQuestion.class));
    }

    @Test
    @DisplayName("Test save quiz question - Handle repository save error")
    void testSaveQuizQuestion_HandleSaveError() {
        when(repo.save(QuizTestData.quizQuestionData())).thenThrow(new RuntimeException("Save error"));
        InternalServerException exception = assertThrows(InternalServerException.class, () -> service.save(QuizTestData.quizQuestionData()));
        assertEquals("Quiz question"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).save(QuizTestData.quizQuestionData());
    }

    @Test
    @DisplayName("Test save quiz question - Handle repository delete error")
    void testSaveQuizQuestion_HandleDeleteError() {
        QuizQuestion quizQuestion = QuizTestData.quizQuestionData();
        quizQuestion.setDelete(true);
        doThrow(new RuntimeException("Delete error")).when(repo).deleteById(quizQuestion.getId());

        InternalServerException exception = assertThrows(InternalServerException.class, () -> service.save(quizQuestion));
        assertEquals("Quiz question"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).deleteById(quizQuestion.getId());
        verify(repo, never()).save(any(QuizQuestion.class));
    }

}
