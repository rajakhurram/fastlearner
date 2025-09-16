package com.vinncorp.fast_learner.services.otp;


import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.otp.Otp;
import com.vinncorp.fast_learner.models.user.User;

public interface IOtpService {
    int createOtp(User user);

    boolean verifyingOtp(long id, int value) throws BadRequestException;

    int createAuthenticationOtp();

    Boolean verifyOtp(String email, int otp) throws EntityNotFoundException;

    Boolean findByUserId(User user);
}
