package com.vinncorp.fast_learner.services.auth;

import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.request.auth.LocalAuthRequest;
import com.vinncorp.fast_learner.request.auth.LocalRegisterRequest;
import com.vinncorp.fast_learner.response.auth.JwtTokenResponse;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.util.Message;
import jakarta.mail.MessagingException;

import java.security.Principal;

public interface  IAuthenticationService {
    TokenResponse socialLogin(String token, String provider,String clientType, String userName) throws AuthenticationException, InternalServerException, BadRequestException;

    TokenResponse localLogin(LocalAuthRequest request) throws AuthenticationException, BadRequestException, EntityNotFoundException;

    JwtTokenResponse refreshToken(String token, Principal principal) throws EntityNotFoundException;

    TokenResponse localRegister(LocalRegisterRequest request) throws InternalServerException, EntityAlreadyExistException;

    Message<String> resetPassword( String password, String email) throws EntityNotFoundException, BadRequestException, InternalServerException;

    Message<String> sendingLinkForResettingPassword(String email) throws EntityNotFoundException, MessagingException;

    Message<String> sendLinkForResettingPassword(String email) throws EntityNotFoundException, MessagingException;

    Message<String> authenticationOtp(LocalRegisterRequest request) throws EntityNotFoundException, EntityAlreadyExistException;

    TokenResponse verifyAuthenticationOtp(String email, int otp) throws BadRequestException, EntityAlreadyExistException, InternalServerException;

    Message<String> resendAuthenticationOtp(String email) throws EntityNotFoundException, BadRequestException;

    Message<String> verifyingOtp(String email, int otp) throws BadRequestException, EntityNotFoundException;

    Message<String> reSendLinkForResettingPassword(String email) throws EntityNotFoundException;

    Message<String> handleUserAccount(String email, String action) throws EntityNotFoundException, BadRequestException, InternalServerException;

    Message tokenValidation(String parseJwt, Principal principal) throws EntityNotFoundException;
}
