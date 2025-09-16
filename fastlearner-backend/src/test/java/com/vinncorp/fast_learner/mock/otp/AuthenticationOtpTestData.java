package com.vinncorp.fast_learner.mock.otp;

import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;

import java.time.LocalDateTime;

public class AuthenticationOtpTestData {
    public static AuthenticationOtp authenticationOtp() {
        return AuthenticationOtp.builder()
                .id(1L)
                .email("test@email.com")
                .name("Test Name")
                .otp(1111)
                .localDateTime(LocalDateTime.now())
                .subscribeNewsletter(false)
                .build();
    }
}
