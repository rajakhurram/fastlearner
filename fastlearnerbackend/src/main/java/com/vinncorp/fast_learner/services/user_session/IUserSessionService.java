package com.vinncorp.fast_learner.services.user_session;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.affiliate.CreateAffiliateReq;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.response.user_session.UserSessionResponse;
import com.vinncorp.fast_learner.util.Message;

import java.security.Principal;

public interface IUserSessionService {
    Message createSessionId(Long subscriptionId, Long courseId, Principal principal) throws EntityNotFoundException, BadRequestException;
    Message<UserSessionResponse> generateTokenAgainstSessionId(String sessionId) throws BadRequestException, EntityNotFoundException, InternalServerException;
}
