package com.vinncorp.fast_learner.services.user;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.user.ChangePasswordRequestDto;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.user.UserCreationRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.UserCreationResponse;
import com.vinncorp.fast_learner.response.user.UserResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.UserRole;

import java.security.Principal;
import java.util.List;

public interface IUserService {
    Message<UserCreationResponse> create(UserCreationRequest user) throws EntityNotFoundException, InternalServerException;

    User findByEmail(String toLowerCase) throws EntityNotFoundException;

    User save(User build) throws InternalServerException;

    Message<String> addRoleForUser(UserRole valueOf, String name) throws EntityNotFoundException, InternalServerException;

    Message<String> changePassword(ChangePasswordRequestDto requestDto, Principal principal) throws EntityNotFoundException, BadRequestException, InternalServerException;

    User getUserByEmail(String email);

    List<User> findAllUsersNotLoggedInForTenDays();
    User findById(Long id) throws EntityNotFoundException;

    Message<UserResponse> getUserDetail(String name);



}
