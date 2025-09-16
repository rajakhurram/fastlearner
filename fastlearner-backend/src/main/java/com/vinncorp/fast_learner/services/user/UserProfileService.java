package com.vinncorp.fast_learner.services.user;

import com.vinncorp.fast_learner.dtos.user.UserProfileDto;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.UserProfileVisitDto;
import com.vinncorp.fast_learner.es_models.CourseContent;
import com.vinncorp.fast_learner.es_services.course_content.IESCourseContentService;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.user.UserProfileRepository;
import com.vinncorp.fast_learner.repositories.user.UserProfileVisitRepository;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.models.user.UserProfileVisit;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.LogMessage;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService implements IUserProfileService {

    private final UserProfileRepository repo;
    private final UserProfileVisitRepository userProfileVisitRepo;
    private final IUserService userService;
    private final RabbitMQProducer rabbitMQProducer;
    private final IESCourseContentService esCourseContentService;

    @Override
    public void createProfile(UserProfile userProfile, User user) throws InternalServerException {
        log.info("Creating user profile.");
        try {
            userProfile.setProfileUrl(this.generateProfileUrl(user.getFullName()));
            repo.save(userProfile);
        } catch (Exception e) {
            throw new InternalServerException("User profile " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<UserProfileDto> getProfile(String profileUrl, String email) throws EntityNotFoundException, InternalServerException {
        log.info("Get user profile.");

        User user = null;
        UserProfile userProfile = null;
        Long userId = null;

        if(Objects.nonNull(email)){
            user = userService.findByEmail(email);
            userId = user.getId();
        }
        if(Objects.nonNull(profileUrl)){
            userProfile = this.repo.findByProfileUrl(profileUrl).orElseThrow(() -> new EntityNotFoundException("User profile not found"));
            userId = userProfile.getCreatedBy();
        }


        Tuple profileData = repo.findProfileInfoByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User profile not found by provided id."));
        UserProfileDto userProfileDto = UserProfileDto.fromTuple(profileData);

        if(Objects.nonNull(user) && Objects.nonNull(user.getId()) && !Objects.equals(userId, user.getId())) {
            addProfileVisit(userId, user);
            rabbitMQProducer.sendMessage("", "user/profile?url="+userProfileDto.getUserProfileUrl(), user.getEmail(),
                    userId, null, NotificationContentType.TEXT, NotificationType.PROFILE_VISIT, user.getId());
        }
        return new Message<UserProfileDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("User profile successfully fetched.")
                .setData(userProfileDto);
    }

    @Override
    public Message updateProfile(UserProfileDto userDto, Principal principal) throws EntityNotFoundException, InternalServerException {
        log.info("Update user profile.");

        //get logged in user
        User user = userService.findByEmail(principal.getName());

        //get logged in user profile
        UserProfile userProfile = repo.findOneByCreatedBy(user.getId()).orElseThrow(() -> new EntityNotFoundException("User Profile " + LogMessage.NOT_EXIST));

        user.setFullName(userDto.getFullName());

        //saved user
        user = userService.save(user);

        //saved profile
        userProfile = UserProfile.mapToUser(userDto, user, userProfile);


        try {
            this.updateESProfileData(user, userProfile);
            repo.save(userProfile);
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("User" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        return new Message()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("User profile updated successfully");
    }

    void updateESProfileData(User user, UserProfile userProfile) throws IOException {
        List<CourseContent> courseContents = this.esCourseContentService.getCoursesByCreatedBy(user.getId());
        for(CourseContent c: courseContents){
            c.setUserPictureUrl(userProfile.getProfilePicture());
            c.setCreatorName(user.getFullName());
        }
        this.esCourseContentService.save(courseContents);
    }

    @Override
    public UserProfileVisitDto findNoOfUsersVisitedProfileBy(String period, Long instructorId) throws EntityNotFoundException {
        log.info("Fetching no of users visited profile");
        UserProfile userProfile = getProfileByUserId(instructorId);
        List<Tuple> data = userProfileVisitRepo.findMonthlyOrYearlyProfileVisitors(period, userProfile.getId());
        if (CollectionUtils.isEmpty(data))
            return null;
        return UserProfileVisitDto.from(data);
    }

    @Override
    public void addProfileVisit(Long instructorId, User user) throws EntityNotFoundException, InternalServerException {
        log.info("Adding profile visit for email: " + user.getEmail());
        UserProfile userProfile = getProfileByUserId(instructorId);

        UserProfileVisit profileVisits = UserProfileVisit.builder()
                .user(user)
                .userProfile(userProfile)
                .creationDate(new Date())
                .build();
        try {
            userProfileVisitRepo.save(profileVisits);
        } catch (Exception e) {
            throw new InternalServerException("User profile visit" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    private UserProfile getProfileByUserId(Long userId) throws EntityNotFoundException {
        log.info("Fetching user profile by id: " + userId);
        return repo.findOneByCreatedBy(userId)
                .orElseThrow(() -> new EntityNotFoundException("User profile not found by provided id."));
    }

    @Override
    public UserProfile getUserProfile(Long userId) throws EntityNotFoundException {
        log.info("Fetch user profile by user id.");
        return repo.findOneByCreatedBy(userId).orElseThrow(() -> new EntityNotFoundException("No user profile found for user."));
    }

    public List<InstructorProfileSearchDto> getSearchInstructorProfiles(String input, int pageNo, int pageSize) {
        String searchTerm = "%" + input.toLowerCase() + "%";
        return repo.getSearchInstructorProfiles(searchTerm, PageRequest.of(pageNo, pageSize)).getContent();
    }

    private String generateProfileUrl(String fullName) throws EntityNotFoundException, InternalServerException {
        log.info("Generating profile url");
        try {
            String noSpecialChars = fullName.replaceAll("[^a-zA-Z0-9\\s]", "");
            String profileUrl = noSpecialChars.trim().replaceAll("\\s+", "-").toLowerCase();
            Boolean urlUnique = false;
            int counter = 0;
            while(!urlUnique){
                String finalUrl = profileUrl + (counter == 0 ? "" : "-" + counter);
                if(this.repo.findByProfileUrl(finalUrl).isEmpty()){
                    urlUnique = true;
                    profileUrl = finalUrl;
                }
                counter++;
            }
            return profileUrl;
        }catch(Exception e){
            throw new InternalServerException("Error while generating profile url");
        }
    }

    @Override
    public Boolean disableSocialLinks(User user) throws InternalServerException, EntityNotFoundException {
        log.info("Update user profile.");

        //get logged in user profile
        UserProfile userProfile = repo.findOneByCreatedBy(user.getId()).orElseThrow(() -> new EntityNotFoundException("User Profile " + LogMessage.NOT_EXIST));

        userProfile.setProfileUrl(null);
        userProfile.setLinkedInUrl(null);
        userProfile.setFacebookUrl(null);
        userProfile.setYoutubeUrl(null);
        userProfile.setTwitterUrl(null);
        userProfile.setWebsiteUrl(null);

        try {
            //this.updateESProfileData(user, userProfile);
            userProfile = repo.save(userProfile);
            if (userProfile != null){
                return true;
            }

        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("User" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        return false;
    }
}
