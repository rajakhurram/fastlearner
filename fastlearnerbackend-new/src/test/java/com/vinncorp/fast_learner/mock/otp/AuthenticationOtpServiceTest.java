package com.vinncorp.fast_learner.mock.otp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.repositories.otp.AuthenticationRepository;
import com.vinncorp.fast_learner.services.otp.AuthenticationOtpService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class AuthenticationOtpServiceTest {

    @Mock
    private AuthenticationRepository authenticationRepository;

    @InjectMocks
    private AuthenticationOtpService authenticationOtpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAuthenticationOtp_NewOtp() {
        AuthenticationOtp authenticationOtp = AuthenticationOtpTestData.authenticationOtp();
        when(authenticationRepository.findByEmail(authenticationOtp.getEmail())).thenReturn(null);
        when(authenticationRepository.save(authenticationOtp)).thenReturn(authenticationOtp);

        AuthenticationOtp result = authenticationOtpService.saveAuthenticationOtp(authenticationOtp);

        assertNotNull(result);
        assertEquals(authenticationOtp, result);
        verify(authenticationRepository).save(authenticationOtp);
    }

    @Test
    void testSaveAuthenticationOtp_ExistingOtp() {
        AuthenticationOtp existingOtp = AuthenticationOtpTestData.authenticationOtp();

        AuthenticationOtp newOtp = AuthenticationOtpTestData.authenticationOtp();
        newOtp.setEmail("updated@example.com");

        when(authenticationRepository.findByEmail(newOtp.getEmail())).thenReturn(existingOtp);
        when(authenticationRepository.save(newOtp)).thenReturn(newOtp);

        AuthenticationOtp result = authenticationOtpService.saveAuthenticationOtp(newOtp);

        assertNotNull(result);
        assertEquals(newOtp, result);
        verify(authenticationRepository).deleteById(existingOtp.getId());
        verify(authenticationRepository).save(newOtp);
    }

    @Test
    void testVerifyAuthenticationOtp_Success() throws BadRequestException {
        AuthenticationOtp authenticationOtp = AuthenticationOtpTestData.authenticationOtp();

        when(authenticationRepository.findByEmail(authenticationOtp.getEmail())).thenReturn(authenticationOtp);

        AuthenticationOtp result = authenticationOtpService.verifyAuthenticationOtp(authenticationOtp.getEmail(), authenticationOtp.getOtp());

        assertNotNull(result);
        assertEquals(authenticationOtp, result);
    }

    @Test
    void testVerifyAuthenticationOtp_InvalidOtp() {
        AuthenticationOtp authenticationOtp = AuthenticationOtpTestData.authenticationOtp();

        when(authenticationRepository.findByEmail(authenticationOtp.getEmail())).thenReturn(authenticationOtp);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authenticationOtpService.verifyAuthenticationOtp(authenticationOtp.getEmail(), 1212);
        });
        assertEquals("Invalid OTP", exception.getMessage());
    }

    @Test
    void testVerifyAuthenticationOtp_OtpExpired() throws BadRequestException {
        AuthenticationOtp authenticationOtp = AuthenticationOtpTestData.authenticationOtp();
        authenticationOtp.setLocalDateTime(LocalDateTime.of(2024, 8, 15, 11, 50));

        when(authenticationRepository.findByEmail(authenticationOtp.getEmail())).thenReturn(authenticationOtp);

        AuthenticationOtp result = authenticationOtpService.verifyAuthenticationOtp(authenticationOtp.getEmail(), authenticationOtp.getOtp());

        assertNull(result);
    }

    @Test
    void testResendAuthenticationOtp_Success() throws EntityNotFoundException {
        String email = "test@example.com";
        int otp = 123456;
        AuthenticationOtp existingOtp = AuthenticationOtpTestData.authenticationOtp();

        when(authenticationRepository.findByEmail(email)).thenReturn(existingOtp);
        when(authenticationRepository.save(any(AuthenticationOtp.class))).thenReturn(existingOtp);

        Message<String> response = authenticationOtpService.resendAuthenticationOtp(email, otp);

        assertNotNull(response);
        assertEquals("Authentication OTP resend successfully.", response.getData());
        assertEquals("Authentication OTP resend successfully.", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        verify(authenticationRepository).save(existingOtp);
    }

    @Test
    void testResendAuthenticationOtp_UserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        int otp = 123456;

        when(authenticationRepository.findByEmail(email)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            authenticationOtpService.resendAuthenticationOtp(email, otp);
        });

        assertEquals("User ot found: " + email, thrown.getMessage());
        verify(authenticationRepository, never()).save(any(AuthenticationOtp.class));
    }
}