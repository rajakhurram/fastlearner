package com.vinncorp.fast_learner.mock.token;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.token.Token;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.token.TokenRepository;
import com.vinncorp.fast_learner.services.token.TokenService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.vinncorp.fast_learner.services.user.IUserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Mock
    private TokenRepository repo;

    @Mock
    private IUserService userService;

    @InjectMocks
    private TokenService tokenService;


    @Test
    void testCreate_Success() throws EntityNotFoundException, InternalServerException {
        MockitoAnnotations.openMocks(this);

        String username = "user@example.com";
        String tokenString = "sampleToken";

        User user = new User();
        user.setId(1L);
        user.setEmail(username);

        when(userService.findByEmail(username)).thenReturn(user);
        when(repo.save(any(Token.class))).thenReturn(new Token());

        assertDoesNotThrow(() -> tokenService.create(username, tokenString));

        verify(userService, times(1)).findByEmail(username);
        verify(repo, times(1)).save(any(Token.class));
    }

    @Test
    void testCreate_UserNotFound() throws EntityNotFoundException {
        MockitoAnnotations.openMocks(this);

        String username = "user@example.com";
        String tokenString = "sampleToken";

        when(userService.findByEmail(username)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            tokenService.create(username, tokenString);
        });

        assertEquals("User not found", thrown.getMessage());

        verify(userService, times(1)).findByEmail(username);
        verify(repo, never()).save(any(Token.class));
    }

    @Test
    void testCreate_InternalServerError() throws EntityNotFoundException {
        MockitoAnnotations.openMocks(this);

        String username = "user@example.com";
        String tokenString = "sampleToken";

        User user = new User();
        user.setId(1L);
        user.setEmail(username);

        when(userService.findByEmail(username)).thenReturn(user);
        doThrow(new RuntimeException("Database error")).when(repo).save(any(Token.class));

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            tokenService.create(username, tokenString);
        });

        assertEquals("Token not saved in database for user: " + username, thrown.getMessage());

        verify(userService, times(1)).findByEmail(username);
        verify(repo, times(1)).save(any(Token.class));
    }

    @Test
    void testCreate_TokenWithEmptyString() throws EntityNotFoundException, InternalServerException {
        MockitoAnnotations.openMocks(this);

        String username = "user@example.com";
        String tokenString = "";

        User user = new User();
        user.setId(1L);
        user.setEmail(username);

        when(userService.findByEmail(username)).thenReturn(user);
        when(repo.save(any(Token.class))).thenReturn(new Token());

        assertDoesNotThrow(() -> tokenService.create(username, tokenString));

        verify(userService, times(1)).findByEmail(username);
        verify(repo, times(1)).save(any(Token.class));
    }


    @Test
    void testCreate_NullUsername() throws EntityNotFoundException {
        MockitoAnnotations.openMocks(this);

        String username = null; // Null username
        String tokenString = "sampleToken";

        NullPointerException thrown = assertThrows(NullPointerException.class, () -> {
            tokenService.create(username, tokenString);
        });

        verify(userService, never()).findByEmail(anyString());
        verify(repo, never()).save(any(Token.class));
    }


}
