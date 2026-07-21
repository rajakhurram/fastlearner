package com.vinncorp.fast_learner.mock.article;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.article.Article;
import com.vinncorp.fast_learner.repositories.article.ArticleRepository;
import com.vinncorp.fast_learner.services.article.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ArticleServiceTest {

    @Mock
    private ArticleRepository repo;

    @InjectMocks
    private ArticleService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Article saved successfully with valid data")
    void testSave_Success() throws InternalServerException, IOException {
        Article article = ArticleTestData.article();

        when(repo.save(any(Article.class))).thenReturn(article);

        Article savedArticle = service.save(article);

        assertNotNull(savedArticle);
        assertEquals(article.getId(), savedArticle.getId());
        verify(repo, times(1)).save(article);
        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Article deletion when delete flag is true")
    void testSave_DeleteArticle() throws InternalServerException, IOException {
        Article article = ArticleTestData.article();
        article.setDelete(true);

        Article result = service.save(article);

        assertNull(result);
        verify(repo, times(1)).deleteById(article.getId());
        verify(repo, never()).save(any(Article.class));
    }

    @Test
    @DisplayName("Article saved without ID")
    void testSave_WithoutId() throws InternalServerException, IOException {
        Article article = ArticleTestData.article();
        article.setId(null);
        when(repo.save(any(Article.class))).thenReturn(article);

        Article savedArticle = service.save(article);

        assertNotNull(savedArticle);
        verify(repo, times(1)).save(article);
        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Article save with null delete flag")
    void testSave_WithNullDeleteFlag() throws InternalServerException, IOException {
        Article article = ArticleTestData.article();
        article.setDelete(null);
        when(repo.save(any(Article.class))).thenReturn(article);

        Article savedArticle = service.save(article);

        assertNotNull(savedArticle);
        verify(repo, times(1)).save(article);
        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Article saving with database error")
    void testSave_ThrowsInternalServerException() throws IOException {
        Article article = ArticleTestData.article();
        when(repo.save(any(Article.class))).thenThrow(new RuntimeException("Database error"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> service.save(article));
        assertEquals("Article cannot be saved due to database error.", exception.getMessage());
        verify(repo, times(1)).save(article);
    }
}
