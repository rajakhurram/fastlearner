package com.vinncorp.fast_learner.controllers.user;

import com.vinncorp.fast_learner.dtos.user.ChangePasswordRequestDto;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.user.UserCreationRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.UserCreationResponse;
import com.vinncorp.fast_learner.response.user.UserResponse;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.enums.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.CHANGE_PASSWORD;

@RestController
@RequestMapping(APIUrls.USER_API)
@RequiredArgsConstructor
public class UserController {

    private final IUserService service;

    @PostMapping(APIUrls.CREATE_USER)
    public ResponseEntity<Message<UserCreationResponse>> addUser(@Valid @RequestBody UserCreationRequest user) throws EntityNotFoundException, InternalServerException {
        Message<UserCreationResponse> m = service.create(user);
        return ResponseEntity.ok().body(m);
    }

    @PostMapping(APIUrls.DEFINE_ROLE_FOR_A_USER)
    public ResponseEntity<Message<String>> defineRoleForAUser(@RequestParam String role, Principal principal)
            throws InternalServerException, EntityNotFoundException {
        Message<String> m = service.addRoleForUser(UserRole.valueOf(role), principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(CHANGE_PASSWORD)
    public ResponseEntity<Message<String>> changePassword(@Valid @RequestBody ChangePasswordRequestDto changeRequest, Principal principal) throws EntityNotFoundException, BadRequestException, InternalServerException{
        Message<String> m = service.changePassword(changeRequest,principal);
        return ResponseEntity.ok().body(m);
    }

    @GetMapping(APIUrls.GET_USER_DETAILS)
    public ResponseEntity<Message<UserResponse>> getUserDetails(Principal principal)
    {
        var m = service.getUserDetail(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
