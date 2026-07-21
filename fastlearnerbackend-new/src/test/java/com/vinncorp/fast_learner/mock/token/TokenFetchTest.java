package com.vinncorp.fast_learner.mock.token;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.token.Token;
import com.vinncorp.fast_learner.repositories.token.TokenRepository;
import com.vinncorp.fast_learner.services.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class TokenFetchTest {


    @Mock
    private TokenRepository repo;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testFetchByToken_Success() {
        // Arrange
        String tokenString = "sampleToken";
        Token token = new Token();
        token.setToken(tokenString);

        when(repo.findByToken(tokenString)).thenReturn(token);

        // Act
        Token result = tokenService.fetchByToken(tokenString);

        // Assert
        assertNotNull(result, "Token should be found");
        assertEquals(tokenString, result.getToken(), "Token should match the input");
        verify(repo, times(1)).findByToken(tokenString);
    }

    @Test
    void testFetchByToken_TokenNotFound() {
        // Arrange
        String tokenString = "invalidToken";

        when(repo.findByToken(tokenString)).thenReturn(null);

        // Act
        Token result = tokenService.fetchByToken(tokenString);

        // Assert
        assertNull(result, "Token should not be found");
        verify(repo, times(1)).findByToken(tokenString);
    }

    @Test
    void testFetchByToken_Whitespace() {
        // Arrange
        String tokenString = "   sampleToken   ";
        Token token = new Token();
        token.setToken(tokenString.trim());

        when(repo.findByToken(tokenString.trim())).thenReturn(token);

        // Act
        Token result = tokenService.fetchByToken(tokenString.trim());

        // Assert
        assertNotNull(result, "Token should be found");
        assertEquals(tokenString.trim(), result.getToken(), "Token should match the input without whitespace");
        verify(repo, times(1)).findByToken(tokenString.trim());
    }



    @Test
    void testFetchByToken_NullInput() {
        Token result = tokenService.fetchByToken(null);

        assertNull(result, "Token should be null when input is null");
        verify(repo, never()).findByToken(anyString());
    }



    @Test
    void testFetchByToken_ExceptionThrown() {
        String tokenString = "sampleToken";

        when(repo.findByToken(tokenString)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            tokenService.fetchByToken(tokenString);
        }, "Exception should be thrown during fetch");
    }

    @Test
    void testExistsByToken_TokenExists() {
        String tokenString = "sampleToken";

        when(repo.existsByToken(tokenString)).thenReturn(true);

        boolean result = tokenService.existsByToke(tokenString);

        assertTrue(result, "Token should exist");
        verify(repo, times(1)).existsByToken(tokenString);
    }


    @Test
    void testExistsByToken_NullToken() {
        boolean result = tokenService.existsByToke(null);

        assertFalse(result, "Null token should return false");
        verify(repo, never()).existsByToken(anyString());
    }


    @Test
    void testExistsByToken_BlankToken() {
        boolean result = tokenService.existsByToke("   ");

        assertFalse(result, "Blank token should return false");
        verify(repo, never()).existsByToken(anyString());
    }
    @Test
    void testExistsByToken_TokenDoesNotExist() {
        String tokenString = "sampleToken";

        when(repo.existsByToken(tokenString)).thenReturn(false);

        boolean result = tokenService.existsByToke(tokenString);

        assertFalse(result, "Token should not exist");
        verify(repo, times(1)).existsByToken(tokenString);
    }


    @Test
    void testFetchTokenByUserId_Success() {
        Long userId = 1L;
        Token token = new Token();
        token.setToken("sampleToken");
        token.setCreatedBy(userId);

        when(repo.findByCreatedBy(userId)).thenReturn(token);

        Token result = tokenService.fetchTokenByUserId(userId);


        assertNotNull(result, "Token should be found");
        assertEquals("sampleToken", result.getToken(), "Token should match the expected token");
        assertEquals(userId, result.getCreatedBy(), "User ID should match the expected user ID");
        verify(repo, times(1)).findByCreatedBy(userId);
    }

    @Test
    void testFetchTokenByUserId_TokenNotFound() {

        Long userId = 1L;

        when(repo.findByCreatedBy(userId)).thenReturn(null);

        Token result = tokenService.fetchTokenByUserId(userId);

        assertNull(result, "Token should not be found for non-existent user ID");
        verify(repo, times(1)).findByCreatedBy(userId);
    }

    @Test
    void testFetchTokenByUserId_InvalidUserId() {
        // Arrange
        Long userId = null;

        Token result = tokenService.fetchTokenByUserId(userId);

        assertNull(result, "Token should not be found for invalid user ID");
        verify(repo, never()).findByCreatedBy(anyLong());
    }

    @Test
    void testSave_Success() {
        Token token = new Token();
        token.setToken("sampleToken");

        assertDoesNotThrow(() -> tokenService.save(token), "Save method should not throw exception on successful save");

        verify(repo, times(1)).save(token);
    }



    @Test
    void testSave_ExceptionThrown() {
        Token token = new Token();
        token.setToken("sampleToken");

        doThrow(new RuntimeException("Database error")).when(repo).save(any(Token.class));

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> tokenService.save(token),
                "Expected InternalServerException to be thrown");

        assertEquals("Token not updated successfully in the database.", thrown.getMessage(), "Exception message should match");
    }

}
