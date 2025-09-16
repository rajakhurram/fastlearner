package com.vinncorp.fast_learner.services.auth.social_login;

import com.vinncorp.fast_learner.exception.AuthenticationException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.auth.TokenResponse;

public interface ISocialLoginService {

    TokenResponse login(String token, String CLIENT_ID, String userName) throws AuthenticationException, InternalServerException;
}
