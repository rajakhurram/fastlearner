package com.vinncorp.fast_learner.controllers.user;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.user.UserDetails;
import com.vinncorp.fast_learner.services.user.IUserDetailsService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.USER_API)
@RequiredArgsConstructor
public class UserDetailsController {

    private final IUserDetailsService userDetailsService;


    @GetMapping(APIUrls.GET_USER_WELCOME_STATUS)
    public ResponseEntity<Message<UserDetails>> getUserWelcomeStatus(Principal principal) throws EntityNotFoundException {
        var m = userDetailsService.getUserWelcomeStatus(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
