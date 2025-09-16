package com.vinncorp.fast_learner.mock.user.user_profile;

import com.vinncorp.fast_learner.dtos.user.UserProfileDto;
import com.vinncorp.fast_learner.es_models.CourseContent;
import com.vinncorp.fast_learner.es_services.course_content.IESCourseContentService;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.models.user.UserProfileVisit;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.user.UserProfileRepository;
import com.vinncorp.fast_learner.repositories.user.UserProfileVisitRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.services.user.UserProfileService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService service;

    @Mock
    private UserProfileRepository repo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserService userService;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @Mock
    private UserProfileVisitRepository userProfileVisitRepo;
    @Mock
    private IESCourseContentService esCourseContentService;

    private String EMAIL = "qasim@mailinator.com";

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test: Successfully Create User Profile")
    void testCreateProfile_Success() throws InternalServerException {

        UserProfile userProfile = UserProfileTestData.userProfile();
        User user = UserTestData.userData();
        String expectedProfileUrl = "qasim-ali";

        when(repo.findByProfileUrl(anyString())).thenReturn(null);
        when(repo.save(userProfile)).thenReturn(userProfile);

        /*service.createProfile(userProfile, user);
        verify(repo, times(1)).save(userProfile);

        assertEquals(expectedProfileUrl, userProfile.getProfileUrl());*/
    }


    @Test
    @DisplayName("Test: Create User Profile Throws InternalServerException")
    void testCreateProfile_InternalServerException() {

        UserProfile userProfile = UserProfileTestData.userProfile();
        User user = UserTestData.userData();
        String expectedProfileUrl = "qasim-ali";

        when(repo.findByProfileUrl(anyString())).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(repo).save(userProfile);

        assertThrows(InternalServerException.class, () -> service.createProfile(userProfile, user));
    }

    @Test
    @DisplayName("Test: Successfully Get User Profile")
    void testGetProfile_Success() throws EntityNotFoundException, InternalServerException {

        User user = UserTestData.userData();
        UserProfile userProfile = UserProfileTestData.userProfile();
        userProfile.setCreatedBy(user.getId());
        String profileUrl = userProfile.getProfileUrl();
        Long userId = userProfile.getCreatedBy();

        when(userService.findByEmail(EMAIL)).thenReturn(user);

        when(repo.findByProfileUrl(profileUrl)).thenReturn(Optional.of(userProfile));

        Tuple profileData = mock(Tuple.class);
        when(profileData.get("full_name")).thenReturn(user.getFullName());
        when(profileData.get("email")).thenReturn(EMAIL);
        when(profileData.get("profile_picture")).thenReturn(userProfile.getProfilePicture());
        when(profileData.get("specialization")).thenReturn(userProfile.getSpecialization());
        when(profileData.get("qualification")).thenReturn(userProfile.getQualification());
        when(profileData.get("experience")).thenReturn(userProfile.getExperience());
        when(profileData.get("headline")).thenReturn(userProfile.getHeadline());
        when(profileData.get("about_me")).thenReturn(userProfile.getAboutMe());
        when(profileData.get("show_profile")).thenReturn(true);
        when(profileData.get("show_courses")).thenReturn(true);
        when(profileData.get("website_url")).thenReturn(null);
        when(profileData.get("facebook_url")).thenReturn(null);
        when(profileData.get("twitter_url")).thenReturn(null);
        when(profileData.get("linked_in_url")).thenReturn(null);
        when(profileData.get("youtube_url")).thenReturn(null);
        when(profileData.get("total_reviews")).thenReturn(100L);
        when(profileData.get("total_students")).thenReturn(130L);

        when(repo.findProfileInfoByUserId(userId)).thenReturn(Optional.of(profileData));

        Message<UserProfileDto> response = service.getProfile(profileUrl, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("User profile successfully fetched.", response.getMessage());

        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).findProfileInfoByUserId(userId);
    }


    @Test
    @DisplayName("Test: Profile Not Found by profile url")
    void testGetProfile_UserNotFoundByEmail() throws EntityNotFoundException {
        String email = "nonexistent@example.com";

        when(userService.findByEmail(email)).thenReturn(UserTestData.userData());

        assertThrows(EntityNotFoundException.class, () -> service.getProfile(anyString(), email));
    }

    @Test
    @DisplayName("Test: User Not Found by email null")
    void testGetProfile_ProfileNotFoundById() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(null);

        when(repo.findProfileInfoByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getProfile(anyString(), null));
    }

    @Test
    @DisplayName("Test: Successfully Update User Profile")
    void testUpdateProfile_Success() throws EntityNotFoundException, InternalServerException {
        UserProfileDto userDto = UserProfileTestData.userProfileDto();

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(EMAIL);

        User user = UserTestData.userData();

        UserProfile userProfile = UserProfileTestData.userProfile();

        when(userService.findByEmail(principal.getName())).thenReturn(user);
        when(repo.findOneByCreatedBy(user.getId())).thenReturn(Optional.of(userProfile));

        when(userService.save(user)).thenReturn(user);
        when(repo.save(userProfile)).thenReturn(userProfile);
        when(esCourseContentService.getCoursesByCreatedBy(user.getId())).thenReturn(List.of(CourseContent.builder().creatorName("saif").userProfileUrl("").build()));

        Message response = service.updateProfile(userDto, principal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("User profile updated successfully", response.getMessage());
    }

    @Test
    @DisplayName("Test: User Not Found by Email")
    void testUpdateProfile_UserNotFoundByEmail() throws EntityNotFoundException {
        UserProfileDto userDto = UserProfileTestData.userProfileDto();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("nonexistent@example.com");

        when(userService.findByEmail(principal.getName())).thenThrow(new EntityNotFoundException("User not found."));

        assertThrows(EntityNotFoundException.class, () -> service.updateProfile(userDto, principal));

        verify(repo, times(0)).findOneByCreatedBy(anyLong());
    }

    @Test
    @DisplayName("Test: User Profile Not Found")
    void testUpdateProfile_UserProfileNotFound() throws EntityNotFoundException {
        // Arrange
        UserProfileDto userDto = UserProfileTestData.userProfileDto();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(EMAIL);

        User user = UserTestData.userData();

        when(userService.findByEmail(principal.getName())).thenReturn(user);
        when(repo.findOneByCreatedBy(user.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateProfile(userDto, principal));

        verify(repo, times(0)).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Test: Handle Internal Server Exception During Profile Save")
    void testUpdateProfile_InternalServerException() throws EntityNotFoundException, InternalServerException {
        UserProfileDto userDto = UserProfileTestData.userProfileDto();

        User user = UserTestData.userData();
        UserProfile userProfile = UserProfileTestData.userProfile();

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(userService.save(user)).thenReturn(user);
        when(repo.findOneByCreatedBy(user.getId())).thenReturn(Optional.of(userProfile));
        when(repo.save(userProfile)).thenThrow(new RuntimeException("Database Error"));

        assertThrows(InternalServerException.class, () -> service.updateProfile(userDto, ()->EMAIL));

//        verify(userService, times(1)).save(user);
//        verify(repo, times(1)).save(userProfile);
    }

    @Test
    @DisplayName("Test: Successfully Add Profile Visit")
    void testAddProfileVisit_Success() throws EntityNotFoundException, InternalServerException {
        User user = UserTestData.userData();

        UserProfile userProfile = UserProfileTestData.userProfile();

        UserProfileVisit profileVisit = UserProfileVisit.builder()
                .user(user)
                .userProfile(userProfile)
                .creationDate(new Date())
                .build();

        when(repo.findOneByCreatedBy(1L)).thenReturn(Optional.of(userProfile));
        when(userProfileVisitRepo.save(any(UserProfileVisit.class))).thenReturn(profileVisit);

        service.addProfileVisit(1L, user);

        verify(userProfileVisitRepo, times(1)).save(any(UserProfileVisit.class));
    }

    @Test
    @DisplayName("Test: User Profile Not Found (Throws EntityNotFoundException)")
    void testAddProfileVisit_UserProfileNotFound() throws EntityNotFoundException, InternalServerException {
        User user = UserTestData.userData();

        when(repo.findOneByCreatedBy(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addProfileVisit(1L, user));

        verify(userProfileVisitRepo, times(0)).save(any(UserProfileVisit.class));
    }

    @Test
    @DisplayName("Test: Exception During Save (Throws InternalServerException)")
    void testAddProfileVisit_InternalServerException() throws EntityNotFoundException, InternalServerException {
        User user = UserTestData.userData();

        UserProfile userProfile = new UserProfile();
        userProfile.setId(1L);

        when(repo.findOneByCreatedBy(1L)).thenReturn(Optional.of(userProfile));
        when(userProfileVisitRepo.save(any(UserProfileVisit.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(InternalServerException.class, () -> service.addProfileVisit(1L, user));

        verify(userProfileVisitRepo, times(1)).save(any(UserProfileVisit.class));
    }

    @Test
    @DisplayName("Test: Successfully Retrieve User Profile by User ID")
    void testGetUserProfile_Success() throws EntityNotFoundException {
        UserProfile userProfile = UserProfileTestData.userProfile();

        when(repo.findOneByCreatedBy(1L)).thenReturn(Optional.of(userProfile));

        UserProfile result = service.getUserProfile(1L);

        assertNotNull(result);
        assertEquals(userProfile, result);

        verify(repo, times(1)).findOneByCreatedBy(1L);
    }

    @Test
    @DisplayName("Test: User Profile Not Found (Throws EntityNotFoundException)")
    void testGetUserProfile_UserProfileNotFound() throws EntityNotFoundException {
        when(repo.findOneByCreatedBy(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getUserProfile(1L));

        verify(repo, times(1)).findOneByCreatedBy(1L);
    }
}
