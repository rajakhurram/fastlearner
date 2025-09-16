package com.vinncorp.fast_learner.mock.otp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import com.vinncorp.fast_learner.models.otp.Otp;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.otp.AuthenticationRepository;
import com.vinncorp.fast_learner.repositories.otp.OtpRepository;
import com.vinncorp.fast_learner.services.otp.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Random;

public class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private AuthenticationRepository authenticationRepository;

    @Mock
    private TaskScheduler scheduler;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(otpService, "EXPIRY_TIME_FOR_PASSWORD_RESET_OTP", 300000L); // 5 minutes
    }

    @Test
    @DisplayName("Test createOtp - Successfully create and return OTP")
    void testCreateOtp_Success() {
        User user = UserTestData.userData();

        Otp savedOtp = OtpTestData.otp();

        when(otpRepository.save(any(Otp.class))).thenReturn(savedOtp);

        int otpValue = otpService.createOtp(user);

        assertEquals(otpValue, savedOtp.getValue());
        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(scheduler, times(1)).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    @DisplayName("Test verifyingOtp - Successfully verify OTP")
    void testVerifyingOtp_Success() throws BadRequestException {
        Otp otp = OtpTestData.otp();
        when(otpRepository.findByUserIdAndValue(1L, 1111)).thenReturn(otp);

        boolean result = otpService.verifyingOtp(1L, 1111);

        assertTrue(result);
        verify(otpRepository, times(1)).findByUserIdAndValue(1L, 1111);
        verify(otpRepository, times(1)).delete(otp);
    }

    @Test
    @DisplayName("Test verifyingOtp - Invalid OTP throws BadRequestException")
    void testVerifyingOtp_InvalidOtpThrowsException() {

        when(otpRepository.findByUserIdAndValue(1L, 111)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.verifyingOtp(1L, 1111);
        });

        assertEquals("Invalid OTP provided.", exception.getMessage());
        verify(otpRepository, times(1)).findByUserIdAndValue(1L, 1111);
        verify(otpRepository, never()).delete(any(Otp.class));
    }

    // TODO ERROR: Resolve below test method
    @Test
    @DisplayName("Test createAuthenticationOtp - Successfully create and return authentication OTP")
    void testCreateAuthenticationOtp_Success() {
        when(authenticationRepository.findByOtp(anyInt())).thenReturn(null);

        int otpValue = otpService.createAuthenticationOtp();

        assertTrue(otpValue >= 1000 && otpValue <= 9999);
        verify(authenticationRepository, times(1)).findByOtp(otpValue);
    }

    @Test
    @DisplayName("Test createAuthenticationOtp - OTP collision, retries until unique OTP is generated")
    void testCreateAuthenticationOtp_Collision() {
        // Arrange
        int firstOtp = 1234;
        int secondOtp = 5678;

        // Mocking the Random instance
        Random randomMock = mock(Random.class);
        when(randomMock.nextInt(anyInt())).thenReturn(firstOtp, secondOtp);

        when(authenticationRepository.findByOtp(any(Integer.class))).thenReturn(new AuthenticationOtp(), null);

        // Act
        int otpValue = otpService.createAuthenticationOtp();

        // Assert
        assertEquals(otpValue, otpValue);
        verify(authenticationRepository, times(2)).findByOtp(anyInt());
    }


}