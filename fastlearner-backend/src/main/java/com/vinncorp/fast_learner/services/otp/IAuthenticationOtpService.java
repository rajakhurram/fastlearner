package com.vinncorp.fast_learner.services.otp;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import com.vinncorp.fast_learner.util.Message;

public interface IAuthenticationOtpService {

    AuthenticationOtp saveAuthenticationOtp(AuthenticationOtp authenticationOtp);

    AuthenticationOtp verifyAuthenticationOtp(String email, int otp) throws BadRequestException;

    Message<String> resendAuthenticationOtp(String email, int otp) throws EntityNotFoundException;

}
