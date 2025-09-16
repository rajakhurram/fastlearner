package com.vinncorp.fast_learner.controllers.user;


import com.vinncorp.fast_learner.dtos.user.UserProfileDto;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Objects;

@RestController
@RequestMapping(APIUrls.USER_PROFILE_API)
@RequiredArgsConstructor
public class UserProfileController {

    private final IUserProfileService service;


    @GetMapping(APIUrls.GET_USER_PROFILE_API)
    public ResponseEntity<Message<UserProfileDto>> getUserProfile(
            @RequestParam(required = false) String profileUrl,  Principal principal)
            throws EntityNotFoundException, InternalServerException {
        Message<UserProfileDto> m = service.getProfile(profileUrl, Objects.nonNull(principal) ? principal.getName() : null);
        return  ResponseEntity.ok().body(m);
    }

    @PostMapping(APIUrls.USER_PROFILE_UPDATE_API)
    public ResponseEntity<Message> updateUserProfile(@Valid @RequestBody UserProfileDto userProfile, Principal principal)
            throws EntityNotFoundException, InternalServerException {
        Message m = service.updateProfile(userProfile, principal);
        return  ResponseEntity.ok().body(m);
    }
}
