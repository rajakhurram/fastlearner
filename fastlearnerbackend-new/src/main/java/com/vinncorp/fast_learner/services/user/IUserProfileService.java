package com.vinncorp.fast_learner.services.user;

import com.stripe.model.PaymentMethod;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.user.UserProfileDto;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.UserProfileVisitDto;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.util.Message;

import java.security.Principal;
import java.util.List;

public interface IUserProfileService {
    void createProfile(UserProfile userProfile, User user) throws InternalServerException;

    Message<UserProfileDto> getProfile(String profileUrl, String email) throws EntityNotFoundException, InternalServerException;

    Message updateProfile(UserProfileDto userProfiledDto, Principal principal) throws EntityNotFoundException, InternalServerException;

    UserProfileVisitDto findNoOfUsersVisitedProfileBy(String period, Long instructorId) throws EntityNotFoundException;

    void addProfileVisit(Long profileId, User user) throws EntityNotFoundException, InternalServerException;

    UserProfile getUserProfile(Long userId) throws EntityNotFoundException;

    List<InstructorProfileSearchDto> getSearchInstructorProfiles(String input, int pageNo, int pageSize);

    Boolean disableSocialLinks(User user) throws InternalServerException, EntityNotFoundException;
}
