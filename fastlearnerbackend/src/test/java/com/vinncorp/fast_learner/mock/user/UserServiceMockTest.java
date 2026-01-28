package com.vinncorp.fast_learner.mock.user;

import com.vinncorp.fast_learner.dtos.user.ChangePasswordRequestDto;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.user.UserCreationRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.UserCreationResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorPaginatedResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorResponse;
import com.vinncorp.fast_learner.services.auth.AuthenticationService;
import com.vinncorp.fast_learner.services.email_template.IEmailService;
import com.vinncorp.fast_learner.services.role.RoleService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.UserRole;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceMockTest {

    private static final String DEFAULT_SUPPORT_EMAIL = "support-fastlearner@mailinator.com";
    @Value("${support.email:" + DEFAULT_SUPPORT_EMAIL + "}")
    private String SUPPORT_EMAIL;
    private static String EMAIL = "qasim@mailinator.com";
    private static String INVALID_EMAIL = "invalid-email";
    private static String PASSWORD = "no-password";
    private static String OLD_PASSWORD = "no-password";
    private static String NEW_PASSWORD = "new-password";
    private static String ENCODED_PASSWORD = "encodedNewPassword";
    @Mock
    private UserRepository repository;
    @InjectMocks
    private UserService userService;
    @Mock
    RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private IEmailService emailService;


    private UserCreationRequest validRequest;
    User user;
    private Principal principal;
    private ChangePasswordRequestDto requestDto;

    @Mock
    private IUserService userIService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        validRequest = UserCreationRequest.builder()
                .fullName("John Doe")
                .email("qasim@mailinator.com")
                .password("Password123")
                .provider("LOCAL")
                .role(UserRole.STUDENT.toString())
                .build();
        user = UserTestData.userData();

        principal = mock(Principal.class);
        requestDto = new ChangePasswordRequestDto();
        requestDto.setOldPassword(OLD_PASSWORD);
        requestDto.setNewPassword(NEW_PASSWORD);

        when(principal.getName()).thenReturn(user.getEmail());
    }

    @Test
    @DisplayName("Should return user when email exists")
    void getUserByEmail_Success() {
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        User foundUser = userService.getUserByEmail(user.getEmail());
        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getFullName(), foundUser.getFullName());
        verify(repository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Should return null when email does not exist")
    void getUserByEmail_NotFound() {
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        User foundUser = userService.getUserByEmail(user.getEmail());
        assertNull(foundUser);
        verify(repository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Should change password successfully when current password matches")
    void changePassword_Success() throws EntityNotFoundException, BadRequestException, InternalServerException {
        when(repository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(repository.save(user)).thenReturn(user);
        Message<String> response = userService.changePassword(requestDto, principal);
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Password changed successfully", response.getMessage());
        verify(repository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw BadRequestException when current password does not match")
    void changePassword_Failure_IncorrectCurrentPassword() {
        when(repository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).thenReturn(false);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.changePassword(requestDto, principal);
        });
        assertEquals("Current password does not match", exception.getMessage());
        verify(repository, never()).save(user);
    }

    @Test
    @DisplayName("Should throw InternalServerException when save operation fails")
    void changePassword_Failure_SaveError() throws EntityNotFoundException, BadRequestException {
        when(repository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(repository.save(user)).thenThrow(new RuntimeException("Database error"));
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            userService.changePassword(requestDto, principal);
        });
        assertEquals("User" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should save user successfully")
    void saveUser_Success() throws InternalServerException {
        user.setRole(null);
        when(repository.save(user)).thenReturn(user);
        User savedUser = userService.save(user);
        assertNotNull(savedUser);
        assertEquals(UserTestData.userData().getEmail(), savedUser.getEmail());
        assertEquals(UserTestData.userData().getFullName(), savedUser.getFullName());
        verify(repository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw InternalServerException when save operation fails")
    void saveUser_Failure_ThrowsInternalServerException() {
        user.setRole(null);
        when(repository.save(user)).thenThrow(new RuntimeException("Database error"));
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            userService.save(user);
        });
        assertEquals("User" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should fetch user successfully by valid email")
    void findByEmail_Success() throws EntityNotFoundException {
        when(repository.findByEmail(EMAIL.trim().toLowerCase())).thenReturn(Optional.of(UserTestData.userData()));
        User user = userService.findByEmail(EMAIL);
        assertEquals(EMAIL, user.getEmail());
        assertEquals(UserTestData.userData().getFullName(), user.getFullName());
        verify(repository, times(1)).findByEmail(EMAIL.trim().toLowerCase());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not found")
    void findByEmail_UserNotFound_ThrowsEntityNotFoundException() {
        when(repository.findByEmail(INVALID_EMAIL.trim().toLowerCase())).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.findByEmail(INVALID_EMAIL);
        });
        assertEquals("User not found.", exception.getMessage());
        verify(repository, times(1)).findByEmail(INVALID_EMAIL.trim().toLowerCase());
    }

    @Test
    @DisplayName("Should create user successfully with valid input")
    void createUser_Success() throws EntityNotFoundException, InternalServerException {
        when(roleService.getByType(UserTestData.userData().getEmail())).thenReturn(RoleTestData.roleData(UserTestData.userData().getRole().toString()));
        when(repository.save(any(User.class))).thenReturn(UserTestData.userData());
        Message<UserCreationResponse> response = userService.create(validRequest);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User created successfully.", response.getMessage());
        assertEquals(validRequest.getEmail(), response.getData().getEmail());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when role is invalid")
    void createUser_InvalidRole_ThrowsEntityNotFoundException() throws EntityNotFoundException {
        when(roleService.getByType(validRequest.getRole())).thenThrow(new EntityNotFoundException("Role not found."));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.create(validRequest);
        });
        assertEquals("Role not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when error occurs during save")
    void createUser_SaveError_ThrowsInternalServerException() throws EntityNotFoundException {
        when(roleService.getByType(validRequest.getRole())).thenReturn(RoleTestData.roleData(UserRole.STUDENT.toString()));
        when(repository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            userService.create(validRequest);
        });
        assertEquals("Error occurred on server side.", exception.getMessage());
    }

    @Test
    @DisplayName("Should return one authority when role is STUDENT")
    void getAuthorities_StudentRole_ReturnsOneAuthority() {
        Collection<GrantedAuthority> authorities = userService.getAuthorities(String.valueOf(UserRole.STUDENT));
        assertEquals(1, authorities.size());
        assertEquals(UserRole.STUDENT.toString(), authorities.iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Should return one authority when role is INSTRUCTOR")
    void getAuthorities_InstructorRole_ReturnsOneAuthority() {
        Collection<GrantedAuthority> authorities = userService.getAuthorities(UserRole.INSTRUCTOR.toString());
        assertEquals(1, authorities.size());
        assertEquals(UserRole.INSTRUCTOR.toString(), authorities.iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Should return empty authorities when role is invalid (e.g., ADMIN)")
    void getAuthorities_InvalidRole_ReturnsEmptyAuthorities() {
        Collection<GrantedAuthority> authorities = userService.getAuthorities("ADMIN");
        assertEquals(0, authorities.size());
    }

    @Test
    @DisplayName("Should return empty authorities when role is null")
    void getAuthorities_NullRole_ReturnsEmptyAuthorities() {
        Collection<GrantedAuthority> authorities = userService.getAuthorities(null);
        assertEquals(0, authorities.size());
    }

    @Test
    @DisplayName("Should return empty authorities when role is an empty string")
    void getAuthorities_EmptyStringRole_ReturnsEmptyAuthorities() {
        Collection<GrantedAuthority> authorities = userService.getAuthorities("");
        assertEquals(0, authorities.size());
    }

    @Test
    @DisplayName("Should load user details successfully when user is found with a role")
    void loadUserByUsername_UserFoundWithRole_ReturnsUserDetails() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.ofNullable(UserTestData.userData()));
        UserDetails userDetails = userService.loadUserByUsername(EMAIL);
        assertEquals(EMAIL, userDetails.getUsername());
        assertEquals(PASSWORD, userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    @DisplayName("Should load user details successfully when user is found without a role")
    void loadUserByUsername_UserFoundWithoutRole_ReturnsUserDetailsWithoutAuthorities() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(UserTestData.userData().builder().role(null).build()));
        UserDetails userDetails = userService.loadUserByUsername(EMAIL);
        assertEquals(EMAIL, userDetails.getUsername());
        assertEquals(PASSWORD, userDetails.getPassword());
        assertEquals(0, userDetails.getAuthorities().size()); // No authorities since no role is assigned
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user is not found")
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(EMAIL));
    }

    @Test
    @DisplayName("Should return UserDetails with 'no-password' when user's password is null")
    void loadUserByUsername_UserFoundWithNullPassword_ReturnsUserDetailsWithNoPassword() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.ofNullable(UserTestData.userData().builder().password(null).build()));
        UserDetails userDetails = userService.loadUserByUsername(EMAIL);
        assertEquals(EMAIL, userDetails.getUsername());
        assertEquals(PASSWORD, userDetails.getPassword()); // Default password
    }

    @Test
    @DisplayName("Should send emails to users who have not logged in for 10 days")
    void notifyUserAfterTenDaysOfInactivity_UsersFound_SendsEmails() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .email("user@mailinator.com")
                .fullName("User One")
                .build();
        User user2 = User.builder()
                .id(2L)
                .email("user@mailinator.com")
                .fullName("User Two")
                .build();
        List<User> inactiveUsers = Arrays.asList(user1, user2);

        when(userIService.findAllUsersNotLoggedInForTenDays()).thenReturn(inactiveUsers);

        // Act
        authenticationService.notifyUserAfterTenDaysOfInactivity();

        // Assert
        /*verify(emailService, times(1)).sendEmail(eq(DEFAULT_SUPPORT_EMAIL), eq("Login Reminder"),
                argThat(body -> body.contains("User One")), eq(true));
        verify(emailService, times(1)).sendEmail(eq(DEFAULT_SUPPORT_EMAIL), eq("Login Reminder"),
                argThat(body -> body.contains("User Two")), eq(true));*/
    }

    @Test
    @DisplayName("Should not send emails when no inactive users are found")
    void notifyUserAfterTenDaysOfInactivity_NoUsersFound_DoesNotSendEmails() {
        // Arrange
        when(userIService.findAllUsersNotLoggedInForTenDays()).thenReturn(Arrays.asList());

        // Act
        authenticationService.notifyUserAfterTenDaysOfInactivity();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("Should handle exception and not send emails")
    void notifyUserAfterTenDaysOfInactivity_ExceptionThrown_DoesNotSendEmails() {
        // Arrange
        when(userIService.findAllUsersNotLoggedInForTenDays()).thenThrow(new RuntimeException("Database error"));

        // Act
        authenticationService.notifyUserAfterTenDaysOfInactivity();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean());
    }



    @Test
    @DisplayName("Should return InstructorPaginatedResponse with correct data")
    public void testGetAllTopInstructorSuccess() {
        // Given
        Pageable pageable = mock(Pageable.class);
        Tuple mockTuple = mock(Tuple.class);

        // Prepare mock tuple data
        when(mockTuple.get("course_id")).thenReturn(101L);
        when(mockTuple.get("full_name")).thenReturn("John Doe");
        when(mockTuple.get("specialization")).thenReturn("Computer Science");
        when(mockTuple.get("profile_url")).thenReturn("https://profile.url");
        when(mockTuple.get("user_id")).thenReturn(1L);
        when(mockTuple.get("profile_picture")).thenReturn("https://profile.picture.url");
        when(mockTuple.get("about_me")).thenReturn("I am an instructor.");

        List<Tuple> tupleList = new ArrayList<>();
        tupleList.add(mockTuple);

        Page<Tuple> page = new PageImpl<>(tupleList, pageable, 1); // Create mock page

        // Mock repository call
        when(repository.findAllTopInstructor(pageable)).thenReturn(page);

        // When
        Message<InstructorPaginatedResponse> response = userService.getAllTopInstructor(pageable);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());

        InstructorPaginatedResponse paginatedResponse = response.getData();
        assertEquals(1, paginatedResponse.getTotalElements());
        assertEquals(1, paginatedResponse.getPages());
        assertEquals(1, paginatedResponse.getData().size());

        InstructorResponse instructor = paginatedResponse.getData().get(0);
        assertEquals(101L, instructor.getCourseId());
        assertEquals("John Doe", instructor.getFullName());
        assertEquals("Computer Science", instructor.getSpecialization());
        assertEquals("https://profile.url", instructor.getUserProfileUrl());
        assertEquals(1L, instructor.getUserId());
        assertEquals("https://profile.picture.url", instructor.getProfilePicture());
        assertEquals("I am an instructor.", instructor.getAboutMe());
    }

    @Test
    @DisplayName("Should return error message when exception occurs in repository")
    public void testGetAllTopInstructorFailure() {
        // Given
        Pageable pageable = mock(Pageable.class);

        // Mock repository to throw an exception
        when(repository.findAllTopInstructor(pageable)).thenThrow(new RuntimeException("Database Error"));

        // When
        Message<InstructorPaginatedResponse> response = userService.getAllTopInstructor(pageable);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("An error occurred while top instructor.", response.getMessage());
    }

    @Test
    @DisplayName("Should return empty list when no instructors are found")
    public void testGetAllTopInstructorNoData() {
        // Given
        Pageable pageable = mock(Pageable.class);

        // Mock empty Page<Tuple>
        Page<Tuple> emptyPage = Page.empty(pageable);

        // Mock repository to return empty page
        when(repository.findAllTopInstructor(pageable)).thenReturn(emptyPage);

        // When
        Message<InstructorPaginatedResponse> response = userService.getAllTopInstructor(pageable);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());

        InstructorPaginatedResponse paginatedResponse = response.getData();
        assertEquals(0, paginatedResponse.getTotalElements());
        assertEquals(0, paginatedResponse.getPageSize());
        assertTrue(paginatedResponse.getData().isEmpty());
    }


    @Test
    @DisplayName("Should correctly paginate when handling large data set")
    public void testGetAllTopInstructorLargeDataSet() {
        // Given
        Pageable pageable = mock(Pageable.class);
        when(pageable.getPageNumber()).thenReturn(0); // First page
        when(pageable.getPageSize()).thenReturn(10); // Page size of 10

        // Create a list of 100 mocked Tuple objects
        List<Tuple> largeTupleList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Tuple mockTuple = mock(Tuple.class);
            when(mockTuple.get("course_id")).thenReturn((long) i);
            when(mockTuple.get("full_name")).thenReturn("Instructor " + i);
            when(mockTuple.get("specialization")).thenReturn("Specialization " + i);
            when(mockTuple.get("profile_url")).thenReturn("https://profile.url/" + i);
            when(mockTuple.get("user_id")).thenReturn((long) i);
            when(mockTuple.get("profile_picture")).thenReturn("https://profile.picture.url/" + i);
            when(mockTuple.get("about_me")).thenReturn("About instructor " + i);
            largeTupleList.add(mockTuple);
        }

        // Create a Page object that reflects pagination
        Page<Tuple> page = new PageImpl<>(largeTupleList.subList(0, 10), pageable, 100); // Only first 10 for the page

        // Mock repository call
        when(repository.findAllTopInstructor(pageable)).thenReturn(page);

        // When
        Message<InstructorPaginatedResponse> response = userService.getAllTopInstructor(pageable);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        InstructorPaginatedResponse paginatedResponse = response.getData();
        assertEquals(100, paginatedResponse.getTotalElements());
        assertEquals(10, paginatedResponse.getPageSize());
        assertEquals(10, paginatedResponse.getData().size()); // Ensure only 10 results returned for first page
    }


}

