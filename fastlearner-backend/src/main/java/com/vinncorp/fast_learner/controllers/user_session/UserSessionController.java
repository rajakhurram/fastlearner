package com.vinncorp.fast_learner.controllers.user_session;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.affiliate.CreateAffiliateReq;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.response.user_session.UserSessionResponse;
import com.vinncorp.fast_learner.services.user_session.IUserSessionService;
import com.vinncorp.fast_learner.services.user_session.UserSessionService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.USER_SESSION)
@RequiredArgsConstructor
public class UserSessionController {

    private final IUserSessionService service;


    @PostMapping(APIUrls.CREATE_SESSION)
    public ResponseEntity<?> createSessionId(@RequestParam(required = false) Long subscriptionId,
                                             @RequestParam(required = false) Long courseId,
                                             Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = this.service.createSessionId(subscriptionId, courseId, principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.USER_SESSION_TOKEN)
    public ResponseEntity<Message<UserSessionResponse>> generateTokenAgainstSessionId(@RequestParam String sessionId) throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = this.service.generateTokenAgainstSessionId(sessionId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
