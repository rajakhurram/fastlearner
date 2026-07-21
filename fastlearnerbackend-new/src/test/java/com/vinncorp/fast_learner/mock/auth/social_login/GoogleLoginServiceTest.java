package com.vinncorp.fast_learner.mock.auth.social_login;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.response.auth.social_login.TokenInfoResponse;
import com.vinncorp.fast_learner.services.auth.social_login.GoogleLoginService;
import com.vinncorp.fast_learner.services.role.RoleService;
import com.vinncorp.fast_learner.services.user.UserProfileService;
import com.vinncorp.fast_learner.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GoogleLoginServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private GoogleLoginService googleLoginService;

    private TokenInfoResponse googleTokenInfoResponse;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        googleTokenInfoResponse = new TokenInfoResponse();
        googleTokenInfoResponse.setAud("test-client-id");
        googleTokenInfoResponse.setIss("https://accounts.google.com");
        googleTokenInfoResponse.setEmail("test@example.com");
        googleTokenInfoResponse.setName("Test User");

        mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("Test User");
        mockUser.setRole(null);
        mockUser.setSubscribed(true);
    }

    // TODO RESOLVE: Fix test
    @Test
    @DisplayName("Should successfully login and return token response")
    void testLogin_Success() throws Exception {
        when(jwtUtils.generateJwtToken(anyString(), any(User.class))).thenReturn("jwt-token");
        when(jwtUtils.doGenerateRefreshToken(anyString(), any(User.class))).thenReturn("refresh-token");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000L); // 1 hour in milliseconds

        when(restTemplate.exchange(any(String.class),
                eq(HttpMethod.GET),
                isNull(),
                eq(TokenInfoResponse.class)))
                .thenReturn(ResponseEntity.ok(googleTokenInfoResponse));

        when(userService.findByEmail(anyString())).thenReturn(mockUser);

        doNothing().when(userProfileService).createProfile(any(UserProfile.class), any(User.class));

        TokenResponse response = googleLoginService.login("valid-token", "test-client-id", anyString());

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(3600, response.getExpiredInSec());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.isSubscribed());
        assertNull(response.getRole());
    }

    @Test
    @DisplayName("Should throw AuthenticationException when token is invalid")
    void testLogin_InvalidToken() {
        // Mock RestTemplate to return null
        when(restTemplate.getForObject(anyString(), eq(TokenInfoResponse.class)))
                .thenReturn(null);

        InternalServerException thrown = assertThrows(InternalServerException.class, () ->
                googleLoginService.login("invalid-token", "test-client-id", anyString()));

        assertEquals("Authentication failed", thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw AuthenticationException when CLIENT_ID is incorrect")
    void testLogin_IncorrectClientId() {
        // Mock RestTemplate
        when(restTemplate.getForObject(anyString(), eq(TokenInfoResponse.class)))
                .thenReturn(googleTokenInfoResponse);

        InternalServerException thrown = assertThrows(InternalServerException.class, () ->
                googleLoginService.login("valid-token", "wrong-client-id", anyString()));

        assertEquals("Authentication failed", thrown.getMessage());
    }

    @Test
    @DisplayName("Should register user if not already registered")
    void testRegisterUserIfNotAlreadyRegistered_UserNotFound() throws InternalServerException, EntityNotFoundException {
        when(userService.findByEmail(anyString())).thenThrow(new EntityNotFoundException("User not found"));

        when(userService.save(any(User.class))).thenReturn(mockUser);

        doNothing().when(userProfileService).createProfile(any(UserProfile.class), any(User.class));

        User user = googleLoginService.registerUserIfNotAlreadyRegistered(googleTokenInfoResponse);

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
    }

    @Test
    @DisplayName("Should return existing user if already registered")
    void testRegisterUserIfAlreadyRegistered() throws InternalServerException, EntityNotFoundException {
        when(userService.findByEmail(anyString())).thenReturn(mockUser);

        User user = googleLoginService.registerUserIfNotAlreadyRegistered(googleTokenInfoResponse);

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        verify(userProfileService, never()).createProfile(any(UserProfile.class), any(User.class));
    }

    @Test
    @DisplayName("Should throw InternalServerException when user profile creation fails")
    void testRegisterUserIfNotAlreadyRegistered_UserProfileCreationFails() throws InternalServerException, EntityNotFoundException {
        when(userService.findByEmail(anyString())).thenThrow(new EntityNotFoundException("User not found"));

        when(userService.save(any(User.class))).thenReturn(mockUser);

        doThrow(new InternalServerException("Failed to create user profile")).when(userProfileService).createProfile(any(UserProfile.class), any(User.class));

        InternalServerException thrown = assertThrows(InternalServerException.class, () ->
                googleLoginService.registerUserIfNotAlreadyRegistered(googleTokenInfoResponse));

        assertEquals("Failed to create user profile", thrown.getMessage());
    }
}
