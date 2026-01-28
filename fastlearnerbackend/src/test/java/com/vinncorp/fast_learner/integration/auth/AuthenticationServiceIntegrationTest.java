package com.vinncorp.fast_learner.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.integration.user.UserIntegrationTestData;
import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import com.vinncorp.fast_learner.models.otp.Otp;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.otp.OtpRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.auth.LocalAuthRequest;
import com.vinncorp.fast_learner.request.auth.LocalRegisterRequest;
import com.vinncorp.fast_learner.request.auth.PasswordResettingRequest;
import com.vinncorp.fast_learner.services.auth.AuthenticationService;
import com.vinncorp.fast_learner.services.otp.IAuthenticationOtpService;
import com.vinncorp.fast_learner.services.otp.OtpService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.test_util.Constants;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Constants.Text;
import com.vinncorp.fast_learner.util.Message;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IAuthenticationOtpService authenticationOtpService;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authService;
    private String jwtToken;
    private String refreshToken;
    @Autowired
    private OtpService otpService;
    @Autowired
    private UserService userService;
    private static String AUTHENTICATION_MAIN = APIUrls.AUTHENTICATION_MAIN;
    private TokenUtils tokenUtils;
    private Integer otpValue;

    @Autowired
    private OtpRepository otpRepository;
    User user;
    LocalRegisterRequest localRegisterRequest;

    public AuthenticationServiceIntegrationTest() {
    }

    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
        refreshToken = TokenUtils.getRefreshToken(mockMvc);
    }

//    @Test
//    @DisplayName("Should return 200 OK and JWT tokens on successful social login with Google")
//    void socialAuthentication_ShouldReturnOk_WhenValidGoogleTokenProvided() throws Exception {
//        mockMvc.perform(post(AUTHENTICATION_MAIN + SOCIAL_LOGIN)
//                        .param("token", Constants.VALID_GOOGLE_TOKEN)
//                        .param("provider", Constants.VALID_PROVIDER)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").isNotEmpty())
//                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
//    }

    @Test
    @DisplayName("Should return interval server exception when an invalid token is provided")
    void socialAuthentication_ShouldReturnUnauthorized_WhenInvalidTokenProvided() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.SOCIAL_LOGIN)
                        .param("token", Constants.INVALID_TOKEN)
                        .param("provider", Constants.VALID_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when an unsupported provider is used")
    void socialAuthentication_ShouldReturnBadRequest_WhenUnsupportedProviderUsed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.SOCIAL_LOGIN)
                        .param("token", Constants.VALID_GOOGLE_TOKEN)
                        .param("provider", Constants.INVALID_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid authentication provider: " + Constants.INVALID_PROVIDER));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when required parameters are missing")
    void socialAuthentication_ShouldReturnBadRequest_WhenParametersAreMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.SOCIAL_LOGIN)
                        .param("token", Constants.VALID_GOOGLE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.SOCIAL_LOGIN)
                        .param("provider", Constants.VALID_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 OK and success message when OTP is successfully resent")
    void resendAuthenticationOtp_ShouldReturnOk_WhenOtpIsResentSuccessfully() throws Exception {
        user = UserIntegrationTestData.userData();
        user.setEmail(UserIntegrationTestData.getRandomEmail());
        otpValue = otpService.createOtp(user);
        AuthenticationOtp otpEntity = AuthenticationOtp.builder()
                .email(user.getEmail())
                .name(user.getFullName())
                .otp(otpValue)
                .localDateTime(LocalDateTime.now())
                .password(passwordEncoder.encode(Constants.PASSWORD))
                .build();
        authenticationOtpService.saveAuthenticationOtp(otpEntity);

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.RESEND_AUTHENTICATION_OTP)
                        .param("email", user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Authentication OTP resend successfully."));
    }

    @Test
    @DisplayName("Should return 404 Not Found when email is not registered")
    void resendAuthenticationOtp_ShouldReturnNotFound_WhenEmailIsNotRegistered() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.RESEND_AUTHENTICATION_OTP)
                        .param("email", Constants.INVALID_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email parameter is missing")
    void resendAuthenticationOtp_ShouldReturnBadRequest_WhenEmailParameterIsMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.RESEND_AUTHENTICATION_OTP)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Should return 200 OK and token response when OTP is successfully verified")
    void verifyAuthenticationOtp_ShouldReturnOk_WhenOtpIsVerifiedSuccessfully() throws Exception {
        user = UserIntegrationTestData.userData();
        user.setEmail(UserIntegrationTestData.getRandomEmail());
        otpValue = otpService.createOtp(user);
        AuthenticationOtp otpEntity = AuthenticationOtp.builder()
                .email(user.getEmail())
                .name(user.getFullName())
                .otp(otpValue)
                .localDateTime(LocalDateTime.now())
                .password(passwordEncoder.encode(user.getPassword()))
                .build();
        authenticationOtpService.saveAuthenticationOtp(otpEntity);

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.VERIFY_AUTHENTICATION_OTP)
                        .param("email", user.getEmail())
                        .param("otp", String.valueOf(otpValue))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when OTP is expired or incorrect")
    void verifyAuthenticationOtp_ShouldReturnBadRequest_WhenOtpIsExpiredOrIncorrect() throws Exception {
        user = UserIntegrationTestData.userData();
        user.setEmail(UserIntegrationTestData.getRandomEmail());
        otpValue = otpService.createOtp(user);
        AuthenticationOtp otpEntity = AuthenticationOtp.builder()
                .email(user.getEmail())
                .name(user.getFullName())
                .otp(otpValue)
                .localDateTime(LocalDateTime.now())
                .password(passwordEncoder.encode(user.getPassword()))
                .build();
        authenticationOtpService.saveAuthenticationOtp(otpEntity);

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.VERIFY_AUTHENTICATION_OTP)
                        .param("email", user.getEmail())
                        .param("otp", String.valueOf(Constants.INVALID_OTP))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email or OTP is missing")
    void verifyAuthenticationOtp_ShouldReturnBadRequest_WhenEmailOrOtpIsMissing() throws Exception {
        otpValue = otpService.createOtp(UserIntegrationTestData.userData());
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.VERIFY_AUTHENTICATION_OTP)
                        .param("email", Constants.EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.VERIFY_AUTHENTICATION_OTP)
                        .param("otp", String.valueOf(otpValue))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Should return 200 OK and success message when OTP is sent for new user registration")
    void authenticationOtp_ShouldReturnOk_WhenOtpIsSentForNewUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.AUTHENTICATION_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(UserIntegrationTestData.localRegisterRequest())))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Authentication OTP sent successfully."))
                .andExpect(jsonPath("$.data").value("Authentication OTP sent successfully."));
    }

    @Test
    @DisplayName("Should return 400 Conflict when user already exists")
    void authenticationOtp_ShouldReturnConflict_WhenUserAlreadyExists() throws Exception {
        localRegisterRequest = UserIntegrationTestData.localRegisterRequest();
        authService.localRegister(localRegisterRequest);
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.AUTHENTICATION_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(localRegisterRequest)))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("User already exists."));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid")
    void authenticationOtp_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        localRegisterRequest = UserIntegrationTestData.localRegisterRequest();
        localRegisterRequest.setEmail(Timestamp.from(Instant.now())+localRegisterRequest.getEmail());

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.AUTHENTICATION_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(localRegisterRequest)))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Email should be valid"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when name is missing")
    void authenticationOtp_ShouldReturnBadRequest_WhenNameIsMissing() throws Exception {
        localRegisterRequest = UserIntegrationTestData.localRegisterRequest();
        localRegisterRequest.setName("");
        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN + APIUrls.AUTHENTICATION_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(localRegisterRequest)))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Name should not be empty"));
    }

    @Test
    @DisplayName("Should reset password successfully when valid OTP and credentials are provided")
    void resetPassword_ShouldResetPassword_OnValidRequest() throws Exception {
        otpValue = otpService.createOtp(UserIntegrationTestData.userData());
        PasswordResettingRequest request = new PasswordResettingRequest();
        request.setValue(this.otpValue);
        request.setPassword(Constants.NEW_PASSWORD);
        request.setEmail(Constants.EMAIL);

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN+ APIUrls.RESET_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Password reset successful."))
                .andExpect(jsonPath("$.data").value("Password reset successful."));
        User updatedUser = userService.findByEmail(Constants.EMAIL);
        assertTrue(passwordEncoder.matches(Constants.NEW_PASSWORD, updatedUser.getPassword()));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when OTP is invalid")
    void resetPassword_ShouldReturnBadRequest_OnInvalidOtp() throws Exception {
        otpValue = otpService.createOtp(UserIntegrationTestData.userData());
        PasswordResettingRequest request = new PasswordResettingRequest();
        request.setValue(Constants.INVALID_OTP);
        request.setPassword(Constants.NEW_PASSWORD);
        request.setEmail(Constants.EMAIL);

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN+ APIUrls.RESET_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Invalid OTP provided."));
    }

    @Test
    @DisplayName("Should return 404 Not Found when user does not exist")
    void resetPassword_ShouldReturnNotFound_OnNonExistingUser() throws Exception {
        otpValue = otpService.createOtp(UserIntegrationTestData.userData());
        PasswordResettingRequest request = new PasswordResettingRequest();
        request.setValue(this.otpValue);
        request.setPassword(Constants.NEW_PASSWORD);
        request.setEmail(Constants.INVALID_EMAIL);

        mockMvc.perform(MockMvcRequestBuilders.post(AUTHENTICATION_MAIN+ APIUrls.RESET_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    @DisplayName("Refresh Token Successfully")
    void doRefreshToken_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUTHENTICATION_MAIN+ APIUrls.DO_REFRESH_TOKEN)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refresh_token").exists());
    }

    @Test
    @DisplayName("Fail Refresh Token - Invalid JWT Token")
    void doRefreshToken_InvalidToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUTHENTICATION_MAIN+ APIUrls.DO_REFRESH_TOKEN)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + Constants.INVALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(Text.INVALID_CREDENTIALS));
    }

    @DisplayName("Successful local login returns JWT token")
    @Test
    public void localLogin_whenValidCredentials_thenReturnsTokenResponse() throws Exception {

        LocalAuthRequest authRequest = new LocalAuthRequest();
        authRequest.setEmail("instructor1@mailinator.com");
        authRequest.setPassword("123456");

        mockMvc.perform(post("/auth/local-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"instructor1@mailinator.com\", \"password\": \"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty());
    }

    @DisplayName("Failed local login with invalid credentials")
    @Test
    public void localLogin_whenInvalidCredentials_thenReturnsUnauthorized() throws Exception {

        // Perform login with invalid credentials
        LocalAuthRequest authRequest = new LocalAuthRequest();
        authRequest.setEmail("instructor1@mailinator.com");
        authRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/local-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"instructor1@mailinator.com\", \"password\": \"wrongpassword\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Email or password is incorrect."));
    }

    @DisplayName("Register new user successfully")
    @Test
    public void localRegister_whenNewUser_thenReturnsTokenResponse() throws Exception {
        String uniqueSuffix = String.valueOf(new Random().nextInt(10000));
        String uniqueEmail = "instructor122+" + uniqueSuffix + "@mailinator.com";

        String jsonPayload = String.format(
                "{\"name\": \"wassay\", \"email\": \"%s\", \"password\": \"password\"}",
                uniqueEmail
        );

        ResultActions resultActions = mockMvc.perform(post("/auth/local-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));

        resultActions
                .andExpect(status().isOk());
    }

    @DisplayName("Register new user fails with invalid email")
    @Test
    public void localRegister_whenInvalidEmail_thenReturnsBadRequest() throws Exception {

        mockMvc.perform(post("/auth/local-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"wassay\", \"email\": \"invalid-email\", \"password\": \"password\"}"))
                .andExpect(status().isBadRequest()) // Expecting 400 Bad Request
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").doesNotExist()) // Token should not be present
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").doesNotExist()) // Refresh token should not be present
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").doesNotExist()) // Name should not be present
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").doesNotExist()) // Email should not be present
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscribed").doesNotExist()); // Subscribed should not be present
    }

    @DisplayName("Register existing user throws EntityAlreadyExistException")
    @Test
    public void localRegister_whenUserExists_thenThrowsEntityAlreadyExistException() throws Exception {

        // Attempt to register the same user again
        mockMvc.perform(post("/auth/local-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"instructor1@mailinator.com\", \"name\": \"Existing11 User\", \"password\": \"password\"}"))
                .andExpect(status().isBadRequest());  // Or any other status you use for entity already exists
    }

    @DisplayName("Sending reset password link successfully")
    @Test
    public void sendingLinkForResettingPassword_whenEmailValid_thenSuccess() throws Exception {

        // Request password reset link
        mockMvc.perform(post("/auth/send-link")
                        .param("email", Constants.EMAIL))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password reset link sent successfully."));
    }

    @DisplayName("Sending reset password link fails with non-existent email")
    @Test
    public void sendingLinkForResettingPassword_whenEmailInvalid_thenReturnsError() throws Exception {

        // Attempt to request password reset link with an invalid/non-existent email
        mockMvc.perform(post("/auth/send-link")
                        .param("email", "nonexistentuser@example.com"))
                .andExpect(status().isNotFound()) // Expecting 400 Bad Request or 404 Not Found
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not found.")); // Adjust based on your API's error message
    }

    @DisplayName("Successful logout")
    @Test
    public void doLogout_whenAuthenticated_thenReturnsOk() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/local-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"instructor1@mailinator.com\", \"password\": \"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseString);
        String token = jsonObject.getString("token");

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @DisplayName("Logout fails when not authenticated")
    @Test
    public void doLogout_whenNotAuthenticated_thenReturnsUnauthorized() throws Exception {

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isForbidden());
    }






}
