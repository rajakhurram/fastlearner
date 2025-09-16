package com.vinncorp.fast_learner.mock.auth;

import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.request.auth.LocalAuthRequest;
import com.vinncorp.fast_learner.request.auth.LocalRegisterRequest;
import com.vinncorp.fast_learner.response.auth.JwtTokenResponse;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.services.auth.AuthenticationService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.email_template.EmailService;
import com.vinncorp.fast_learner.services.otp.AuthenticationOtpService;
import com.vinncorp.fast_learner.services.otp.OtpService;
import com.vinncorp.fast_learner.services.user.UserProfileService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.EmailTemplate;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class AuthenticationServiceTest {

    @Mock
    private ICourseService courseService;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthenticationOtpService authenticationOtpService;

    @InjectMocks
    private AuthenticationService authService;

    private LocalRegisterRequest localRegisterRequest;
    private AuthenticationOtp authenticationOtp;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        localRegisterRequest = LocalRegisterRequest.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password")
                .build();

        authenticationOtp = AuthenticationOtp.builder()
                .otp(123456)
                .localDateTime(LocalDateTime.now())
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        user = User.builder().id(1L).fullName("Test User").email("test@example.com").build();
    }

    @DisplayName("Logging in with correct data.")
    @Test
    public void localLogin_whenValidCredentials_thenReturnsTokenResponse() throws BadRequestException, EntityNotFoundException, AuthenticationException, InternalServerException {
        User user = UserTestData.userData();

        LocalAuthRequest request = new LocalAuthRequest();
        request.setEmail(user.getEmail());
        request.setPassword(user.getPassword());

        when(userService.findByEmail(request.getEmail().toLowerCase())).thenReturn(user);
        when(userService.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateJwtToken(request.getEmail().toLowerCase(), user)).thenReturn("jwtToken");
        when(jwtUtils.doGenerateRefreshToken(request.getEmail().toLowerCase(), user)).thenReturn("refreshToken");

        // Act
        TokenResponse response = authService.localLogin(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals(user.getFullName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole().getType(), response.getRole());
    }

    @DisplayName("Logging in with correct data.")
    @Test
    public void localLogin_whenUserNotFound_thenThrowsBadRequestException() throws EntityNotFoundException {
        // Arrange
        LocalAuthRequest request = new LocalAuthRequest();
        request.setEmail("invalid@invalid.com");
        request.setPassword("12345");

        when(userService.findByEmail(request.getEmail().toLowerCase())).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.localLogin(request));
    }

    @DisplayName("Logging in with invalid password.")
    @Test
    public void localLogin_whenPasswordDoesNotMatch_thenThrowsBadRequestException() throws EntityNotFoundException {
        // Arrange
        LocalAuthRequest request = new LocalAuthRequest();
        request.setEmail("naveedkauser@mailinator.com");
        request.setPassword("invalidpassword");

        User user = new User();
        user.setEmail("naveedkauser@mailinator.com");
        user.setPassword("encodedPassword"); // Assume this is the encoded password
        user.setActive(true);

        when(userService.findByEmail(request.getEmail().toLowerCase())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.localLogin(request));
    }

    @DisplayName("Refresh token for valid user.")
    @Test
    public void refreshToken_whenValidUser_thenReturnsJwtTokenResponse() throws EntityNotFoundException {
        // Arrange
        String token = "someOldToken";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("naveedkauser@mailinator.com");

        User user = new User();
        user.setEmail("naveedkauser@mailinator.com");
        user.setFullName("Naveed Kauser");

        when(userService.findByEmail(principal.getName())).thenReturn(user);
        when(jwtUtils.generateJwtToken(principal.getName(), user)).thenReturn("newJwtToken");
        when(jwtUtils.doGenerateRefreshToken(principal.getName(), user)).thenReturn("newRefreshToken");

        // Act
        JwtTokenResponse response = authService.refreshToken(token, principal);

        // Assert
        assertNotNull(response);
        assertEquals("newJwtToken", response.getToken());
        assertEquals("newRefreshToken", response.getRefresh_token());
    }

    @DisplayName("Refresh token for non-existent user.")
    @Test
    public void refreshToken_whenUserNotFound_thenThrowsEntityNotFoundException() throws EntityNotFoundException {
        // Arrange
        String token = "someOldToken";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("nonexistent@mailinator.com");

        when(userService.findByEmail(principal.getName())).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> authService.refreshToken(token, principal));
    }

    @DisplayName("Register new user successfully.")
    @Test
    public void localRegister_whenNewUser_thenReturnsTokenResponse() throws EntityAlreadyExistException, InternalServerException {
        // Arrange
        LocalRegisterRequest request = new LocalRegisterRequest();
        request.setName("Naveed Kauser");
        request.setEmail("naveedkauser@mailinator.com");
        request.setPassword("12345");

        try {
            when(userService.findByEmail(request.getEmail())).thenThrow(new EntityNotFoundException("User not found"));
        }catch (EntityNotFoundException e){
            log.info("The provided email is not exists in the system so the registration process will continue.");
        }

        User savedUser = User.builder()
                .fullName(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .provider(AuthProvider.LOCAL)
                .salesRaise(1.0)
                .isActive(true)
                .creationDate(new Date())
                .build();

        when(userService.save(any(User.class))).thenReturn(savedUser);

        String jwtToken = "jwtToken";
        String refreshToken = "refreshToken";
        when(jwtUtils.generateJwtToken(request.getEmail(), savedUser)).thenReturn(jwtToken);
        when(jwtUtils.doGenerateRefreshToken(request.getEmail(), savedUser)).thenReturn(refreshToken);

        // Act
        TokenResponse response = authService.localRegister(request);

        // Assert
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(savedUser.getFullName(), response.getName());
        assertEquals(savedUser.getEmail(), response.getEmail());

        // Verify that the userProfileService.createProfile() method was called
        verify(userProfileService, times(1)).createProfile(any(UserProfile.class), any(User.class));
    }

    @DisplayName("Register existing user throws EntityAlreadyExistException.")
    @Test
    public void localRegister_whenUserExists_thenThrowsEntityAlreadyExistException() throws EntityNotFoundException {
        // Arrange
        LocalRegisterRequest request = new LocalRegisterRequest();
        request.setName("Naveed Kauser");
        request.setEmail("naveedkauser@mailinator.com");
        request.setPassword("12345");

        User existingUser = new User();
        existingUser.setEmail(request.getEmail());

        when(userService.findByEmail(request.getEmail())).thenReturn(existingUser);

        // Act & Assert
        assertThrows(EntityAlreadyExistException.class, () -> authService.localRegister(request));
    }

    @DisplayName("Sending reset password link successfully.")
    @Test
    public void sendingLinkForResettingPassword_whenEmailValid_thenSuccess() throws EntityNotFoundException, MessagingException {
        // Arrange
        String email = "naveedkauser@mailinator.com";
        User user = new User();
        user.setEmail(email);
        user.setFullName("Naveed Kauser");

        when(userService.findByEmail(email)).thenReturn(user);
        when(otpService.createOtp(user)).thenReturn(123456);

        // Act
        Message<String> response = authService.sendingLinkForResettingPassword(email);

        // Assert
        assertNotNull(response);
        assertEquals("Password reset link sent successfully.", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());

        // Verify that the emailService.sendOtpEmailForResettingPassword() method was called
        verify(emailService, times(1)).sendOtpEmailForResettingPassword(user, 123456);
    }

    @DisplayName("Sending reset password link when user not found.")
    @Test
    public void sendingLinkForResettingPassword_whenUserNotFound_thenThrowsEntityNotFoundException() throws EntityNotFoundException {
        // Arrange
        String email = "naveedkauser@mailinator.com";

        when(userService.findByEmail(email)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, () -> authService.sendingLinkForResettingPassword(email));
    }

    @DisplayName("Sending reset password link when email service fails.")
    @Test
    public void sendingLinkForResettingPassword_whenEmailServiceFails_thenThrowsMessagingException() throws EntityNotFoundException, MessagingException {
        // Arrange
        String email = "naveedkauser@mailinator.com";
        User user = new User();
        user.setEmail(email);
        user.setFullName("Naveed Kauser");

        when(userService.findByEmail(email)).thenReturn(user);
        when(otpService.createOtp(user)).thenReturn(123456);
        doThrow(new MessagingException("Email service failed")).when(emailService).sendOtpEmailForResettingPassword(user, 123456);

        assertThrows(MessagingException.class, () -> authService.sendingLinkForResettingPassword(email));
    }

    @DisplayName("Sending authentication OTP successfully.")
    @Test
    public void authenticationOtp_whenEmailIsNew_thenSuccess() throws EntityAlreadyExistException {
        // Arrange
        LocalRegisterRequest request = new LocalRegisterRequest();
        request.setEmail("naveedkauser@mailinator.com");
        request.setName("Naveed Kauser");
        request.setPassword("password");

        when(userService.getUserByEmail(request.getEmail())).thenReturn(null);
        when(otpService.createAuthenticationOtp()).thenReturn(123456);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        AuthenticationOtp authenticationOtp = AuthenticationOtp.builder()
                .otp(123456)
                .localDateTime(LocalDateTime.now())
                .name(request.getName())
                .email(request.getEmail())
                .password("encodedPassword")
                .build();
        when(authenticationOtpService.saveAuthenticationOtp(any(AuthenticationOtp.class))).thenReturn(authenticationOtp);

        // Act
        Message<String> response = authService.authenticationOtp(request);

        // Assert
        assertNotNull(response);
        assertEquals("Authentication OTP sent successfully.", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());

        // Verify that the emailService.sendEmail() method was called
        verify(emailService, times(1)).sendEmail(
                eq(request.getEmail()),
                eq("SignUp Otp"),
                eq(EmailTemplate.FIRST_PART_OTP_EMAIL_TEMPLATE + "123456" + EmailTemplate.SECOND_PART_OTP_EMAIL_TEMPLATE),
                eq(true)
        );
    }

    @DisplayName("Sending authentication OTP when user already exists.")
    @Test
    public void authenticationOtp_whenUserAlreadyExists_thenThrowsEntityAlreadyExistException() {
        // Arrange
        LocalRegisterRequest request = new LocalRegisterRequest();
        request.setEmail("naveedkauser@mailinator.com");
        request.setName("Naveed Kauser");
        request.setPassword("password");

        User user = new User();
        user.setEmail(request.getEmail());

        when(userService.getUserByEmail(request.getEmail())).thenReturn(user);

        // Act & Assert
        assertThrows(EntityAlreadyExistException.class, () -> authService.authenticationOtp(request));
    }

    @DisplayName("Sending authentication OTP when email service fails.")
    @Test
    public void authenticationOtp_whenEmailServiceFails_thenThrowsMessagingException()  {
        // Arrange
        LocalRegisterRequest request = new LocalRegisterRequest();
        request.setEmail("naveedkauser@mailinator.com");
        request.setName("Naveed Kauser");
        request.setPassword("password");

        when(userService.getUserByEmail(request.getEmail())).thenReturn(null);
        when(otpService.createAuthenticationOtp()).thenReturn(123456);

        doThrow(new RuntimeException("Email service failed")).when(emailService).sendEmail(
                eq(request.getEmail()),
                eq("SignUp Otp"),
                eq(EmailTemplate.FIRST_PART_OTP_EMAIL_TEMPLATE + "123456" + EmailTemplate.SECOND_PART_OTP_EMAIL_TEMPLATE),
                eq(true)
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.authenticationOtp(request));
    }

    @DisplayName("OTP verification success")
    @Test
    void verifyAuthenticationOtp_whenOtpIsValid_thenReturnsTokenResponse() throws Exception {
        when(authenticationOtpService.verifyAuthenticationOtp("test@example.com", 123456)).thenReturn(authenticationOtp);
        when(userService.save(any(User.class))).thenReturn(user);
        when(jwtUtils.generateJwtToken(anyString(), any(User.class))).thenReturn("jwtToken");
        when(jwtUtils.doGenerateRefreshToken(anyString(), any(User.class))).thenReturn("refreshToken");

        TokenResponse response = authService.verifyAuthenticationOtp("test@example.com", 123456);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
    }

    @DisplayName("OTP verification when email or otp is not correct")
    @Test
    void verifyAuthenticationOtp_whenOtpIsExpired_thenThrowsBadRequestException() throws BadRequestException {
        when(authenticationOtpService.verifyAuthenticationOtp("test@example.com", 123456)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.verifyAuthenticationOtp("test@example.com", 123456);
        });

        assertEquals("OTP expired", exception.getMessage());
    }

    @DisplayName("OTP verification failed when user is already exists")
    @Test
    void verifyAuthenticationOtp_whenUserAlreadyExists_thenThrowsEntityAlreadyExistException() throws Exception {
        when(authenticationOtpService.verifyAuthenticationOtp("test@example.com", 123456)).thenReturn(authenticationOtp);
        when(userService.findByEmail(any())).thenReturn(user);

        assertThrows(EntityAlreadyExistException.class, () -> authService.verifyAuthenticationOtp("test@example.com", 123456));
    }

    @DisplayName("OTP verification error when user not saved in local register")
    @Test
    void verifyAuthenticationOtp_whenInternalServerException_thenThrowsInternalServerException() throws Exception {
        when(authenticationOtpService.verifyAuthenticationOtp("test@example.com", 123456)).thenReturn(authenticationOtp);
        doThrow(new InternalServerException("Internal server error")).when(userService).save(any(User.class));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            authService.verifyAuthenticationOtp("test@example.com", 123456);
        });

        assertEquals("Internal server error", exception.getMessage());
    }

    @DisplayName("Reset password success")
    @Test
    void resetPassword_whenValidInputs_thenReturnsSuccessMessage() throws Exception {
        // Arrange
        int otpValue = 123456;
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";
        String email = "test@example.com";

        when(userService.findByEmail(email)).thenReturn(user);
        when(otpService.verifyingOtp(user.getId(), otpValue)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userService.save(user)).thenReturn(user);

        // Act
        Message<String> actualMessage = authService.resetPassword(newPassword, email);

        // Assert
        assertNotNull(actualMessage);
        assertEquals(HttpStatus.OK.value(), actualMessage.getStatus());
        assertEquals(HttpStatus.OK.toString(), actualMessage.getCode());
        assertEquals("Password reset successful.", actualMessage.getMessage());
        assertEquals("Password reset successful.", actualMessage.getData());

        verify(userService, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userService, times(1)).save(user);
    }

    @DisplayName("Reset password with invalid email")
    @Test
    void resetPassword_whenEmailNotFound_thenThrowsEntityNotFoundException() throws Exception {
        // Arrange
        int otpValue = 123456;
        String newPassword = "newPassword";
        String email = "nonexistent@example.com";

        when(userService.findByEmail(email)).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            authService.resetPassword(newPassword, email);
        });

        assertEquals("User not found", exception.getMessage());

        verify(userService, times(1)).findByEmail(email);
        verify(otpService, times(0)).verifyingOtp(anyLong(), anyInt());
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userService, times(0)).save(any(User.class));
    }

//    @DisplayName("Reset password when otp verification failed")
//    @Test
//    void resetPassword_whenOtpInvalid_thenThrowsBadRequestException() throws Exception {
//        // Arrange
//        int otpValue = 123456;
//        String newPassword = "newPassword";
//        String email = "test@example.com";
//
//        when(userService.findByEmail(email)).thenReturn(user);
//        doThrow(new BadRequestException("Invalid OTP")).when(otpService).verifyingOtp(user.getId(), otpValue);
//
//        // Act & Assert
//        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
//            authService.resetPassword( newPassword, email);
//        });
//
//        assertEquals("Invalid OTP", exception.getMessage());
//
//        verify(userService, times(1)).findByEmail(email);
//        verify(otpService, times(1)).verifyingOtp(user.getId(), otpValue);
//        verify(passwordEncoder, times(0)).encode(anyString());
//        verify(userService, times(0)).save(any(User.class));
//    }

    @DisplayName("Reset password when saving new password failed to save")
    @Test
    void resetPassword_whenSaveFails_thenThrowsInternalServerException() throws Exception {
        // Arrange
//        int otpValue = 123456;
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";
        String email = "test@example.com";

        when(userService.findByEmail(email)).thenReturn(user);
//        when(otpService.verifyingOtp(user.getId(), otpValue)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userService.save(user)).thenThrow(new InternalServerException("Failed to save user"));

        // Act & Assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            authService.resetPassword( newPassword, email);
        });

        assertEquals("Failed to save user", exception.getMessage());

        verify(userService, times(1)).findByEmail(email);
//        verify(otpService, times(1)).verifyingOtp(user.getId(), otpValue);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userService, times(1)).save(user);
    }



    @Test
    @DisplayName("Verify OTP - Valid Input, OTP Verified")
    void testVerifyingOtp_ValidInput_OtpVerified() throws BadRequestException, EntityNotFoundException {
        // Arrange
        String email = "test@example.com";
        int otp = 123456;

        // Mock the behavior of otpService to return true for a valid OTP
        when(otpService.verifyOtp(email, otp)).thenReturn(true);

        // Act
        Message<String> result = authService.verifyingOtp(email, otp);

        // Assert
        assertEquals(HttpStatus.OK.value(), result.getStatus(), "Expected HTTP status 200 for successful verification.");
        assertEquals(HttpStatus.OK.toString(), result.getCode(), "Expected HTTP code 200 for successful verification.");
        assertEquals("Verification successful.", result.getMessage(), "Expected success message.");
        assertEquals("Verified OTP", result.getData(), "Expected data to confirm OTP verification.");
    }

    @Test
    @DisplayName("Verify OTP - Invalid OTP, Throws BadRequestException")
    void testVerifyingOtp_InvalidOtp_ThrowsBadRequestException() throws BadRequestException, EntityNotFoundException {
        // Arrange
        String email = "test@example.com";
        int otp = 654321;

        // Mock the behavior of otpService to return false for an invalid OTP
        when(otpService.verifyOtp(email, otp)).thenReturn(false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.verifyingOtp(email, otp);
        });

        // Assert the exception message
        assertEquals("OTP expired or invalid", exception.getMessage(), "Expected specific error message.");


        assertEquals(HttpStatus.BAD_REQUEST.value(), 400, "Expected status code 400 for BadRequestException.");

    }

    @Test
    @DisplayName("Verify OTP - Valid Input, OTP Verified with Mock")
    void testVerifyingOtp_ValidInput_OtpVerifiedWithMock() throws EntityNotFoundException, BadRequestException {
        // Arrange
        String email = "test@example.com";
        int otp = 123456;

        // Mock the behavior of otpService to return true for a valid OTP
        when(otpService.verifyOtp(email, otp)).thenReturn(true);

        // Act
        Message<String> result = authService.verifyingOtp(email, otp);

        // Assert
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.toString(), result.getCode());
        assertEquals("Verification successful.", result.getMessage());
        assertEquals("Verified OTP", result.getData());
    }

    @Test
    @DisplayName("Verify OTP - Non-existent User, Throws BadRequestException")
    void testVerifyingOtp_UserNotFound_ThrowsBadRequestException() throws EntityNotFoundException {
        // Arrange
        String email = "nonexistent@example.com";
        int otp = 123456;

        // Mock the behavior of otpService to return false since the user does not exist
        when(otpService.verifyOtp(email, otp)).thenReturn(false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.verifyingOtp(email, otp);
        });

        // Assert the exception message
        assertEquals("OTP expired or invalid", exception.getMessage(), "Expected specific error message.");
    }

    @Test
    @DisplayName("Verify OTP - Valid Input, OTP Not Verified Due to Invalid OTP")
    void testVerifyingOtp_ValidInput_InvalidOtp() throws BadRequestException, EntityNotFoundException {
        // Arrange
        String email = "test@example.com";
        int otp = 111111;

        // Mock the behavior of otpService to return false for this OTP
        when(otpService.verifyOtp(email, otp)).thenReturn(false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.verifyingOtp(email, otp);
        });

        // Assert
        assertEquals("OTP expired or invalid", exception.getMessage());
    }


    @Test
    void testReSendLinkForResettingPassword_UserExists_Success() throws EntityNotFoundException {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(otpService.findByUserId(user)).thenReturn(false);
        when(otpService.createOtp(user)).thenReturn(123456); // Mock OTP creation

        // Act
        Message<String> response = authService.reSendLinkForResettingPassword("test@example.com");

        // Assert
        assertEquals("Password reset link sent successfully.", response.getData());
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(emailService, times(1)).sendEmail(
                eq("test@example.com"),
                eq("Resetting password"),
                contains("123456"),
                eq(true)
        );
    }

    @Test
    void testReSendLinkForResettingPassword_UserDoesNotExist() throws EntityNotFoundException {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(null);

        // Act
        Message<String> response = authService.reSendLinkForResettingPassword("test@example.com");

        // Assert
        assertEquals("User Does not exist", response.getData());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }


    @Test
    void testReSendLinkForResettingPassword_EmailServiceFailure() throws EntityNotFoundException {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(otpService.findByUserId(user)).thenReturn(false);
        when(otpService.createOtp(user)).thenReturn(123456);
        doThrow(new RuntimeException("Email service failed")).when(emailService).sendEmail(anyString(), anyString(), anyString(), anyBoolean());

        // Act and Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.reSendLinkForResettingPassword("test@example.com");
        });

        assertEquals("Email service failed", exception.getMessage());
    }

    @Test
    void testHandleUserAccount_Disable() throws Exception {
        String email = "test@example.com";
        String action = "disable";

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setActive(true);

        Mockito.when(userService.findByEmail(email)).thenReturn(mockUser);
        Mockito.when(userService.save(mockUser)).thenReturn(mockUser);

        Message<String> response = authService.handleUserAccount(email, action);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User account de-activated successfully.", response.getMessage());
        assertFalse(mockUser.isActive());

        Mockito.verify(userService, Mockito.times(1)).save(mockUser);
    }

    @Test
    void testHandleUserAccount_Delete() throws Exception {
        String email = "test@example.com";
        String action = "delete";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setActive(true);

        Mockito.when(userService.findByEmail(any())).thenReturn(mockUser);
        Mockito.when(userService.save(Mockito.any(User.class))).thenReturn(mockUser);
        Mockito.when(userProfileService.disableSocialLinks(Mockito.any(User.class))).thenReturn(true);
        Mockito.when(courseService.findByInstructorIdAndCourseStatus(any(), any())).thenReturn(Collections.singletonList(CourseTestData.courseData()));
        Message<String> response = authService.handleUserAccount(email, action);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User account deleted successfully.", response.getMessage());
        assertFalse(mockUser.isActive());
        assertTrue(mockUser.getEmail().startsWith("anonymous-"));

        Mockito.verify(userService, Mockito.times(1)).save(mockUser);
        Mockito.verify(userProfileService, Mockito.times(1)).disableSocialLinks(mockUser);
    }


    @Test
    void testHandleUserAccount_InvalidAction() throws EntityNotFoundException, InternalServerException {
        String email = "test@example.com";
        String action = "invalid";

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setActive(true);

        Mockito.when(userService.findByEmail(email)).thenReturn(mockUser);

        assertThrows(BadRequestException.class, () -> authService.handleUserAccount(email, action));

        Mockito.verify(userService, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void testHandleUserAccount_Disable_Success() throws Exception {
        String email = "test@example.com";
        String action = "disable";

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setActive(true);

        Mockito.when(userService.findByEmail(email)).thenReturn(mockUser);
        Mockito.when(userService.save(mockUser)).thenReturn(mockUser);

        Message<String> response = authService.handleUserAccount(email, action);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User account de-activated successfully.", response.getMessage());
        assertFalse(mockUser.isActive());

        Mockito.verify(userService, Mockito.times(1)).save(mockUser);
    }

    @Test
    void testHandleUserAccount_Delete_Success() throws Exception {
        String email = "test@example.com";
        String action = "delete";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setActive(true);

        Mockito.when(userService.findByEmail(any())).thenReturn(mockUser);
        Mockito.when(userService.save(Mockito.any(User.class))).thenReturn(mockUser);
        Mockito.when(userProfileService.disableSocialLinks(Mockito.any(User.class))).thenReturn(true);

        Message<String> response = authService.handleUserAccount(email, action);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User account deleted successfully.", response.getMessage());
        assertFalse(mockUser.isActive());
        assertTrue(mockUser.getEmail().startsWith("anonymous-"));

        Mockito.verify(userService, Mockito.times(1)).save(mockUser);
        Mockito.verify(userProfileService, Mockito.times(1)).disableSocialLinks(mockUser);
    }

    @Test
    void testHandleUserAccount_UserNotFound() throws Exception {
        String email = "nonexistent@example.com";
        String action = "disable";

        Mockito.when(userService.findByEmail(email)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> authService.handleUserAccount(email, action));

        Mockito.verify(userService, Mockito.times(1)).findByEmail(email);
        Mockito.verify(userService, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void testHandleUserAccount_SaveUserFailure() throws Exception {
        String email = "test@example.com";
        String action = "disable";

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setActive(true);

        Mockito.when(userService.findByEmail(email)).thenReturn(mockUser);
        Mockito.when(userService.save(mockUser)).thenThrow(new InternalServerException("Internal server error while saving user data."));

        assertThrows(InternalServerException.class, () -> authService.handleUserAccount(email, action));

        Mockito.verify(userService, Mockito.times(1)).save(mockUser);
    }

    @Test
    void testHandleUserAccount_DeleteSocialLinksFailure() throws Exception {
        String email = "test@example.com";
        String action = "delete";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setActive(true);

        Mockito.when(userService.findByEmail(any())).thenReturn(mockUser);
        Mockito.when(userService.save(Mockito.any(User.class))).thenReturn(mockUser);
        Mockito.when(userProfileService.disableSocialLinks(Mockito.any(User.class))).thenReturn(false);

        Message<String> response = authService.handleUserAccount(email, action);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User account not deleted.", response.getMessage());

        Mockito.verify(userService, Mockito.times(1)).save(mockUser);
        Mockito.verify(userProfileService, Mockito.times(1)).disableSocialLinks(mockUser);
    }



}