package com.vinncorp.fast_learner.services.auth;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import com.vinncorp.fast_learner.models.otp.Otp;
import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.token.Token;
import com.vinncorp.fast_learner.repositories.token.TokenRepository;
import com.vinncorp.fast_learner.services.auth.social_login.ISocialLoginService;
import com.vinncorp.fast_learner.services.auth.social_login.SocialLoginFactory;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.email_template.IEmailService;
import com.vinncorp.fast_learner.services.role.IRoleService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription_permission.ISubscriptionPermissionService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.request.auth.LocalAuthRequest;
import com.vinncorp.fast_learner.request.auth.LocalRegisterRequest;
import com.vinncorp.fast_learner.response.auth.JwtTokenResponse;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.services.otp.IAuthenticationOtpService;
import com.vinncorp.fast_learner.services.otp.IOtpService;
import com.vinncorp.fast_learner.util.Constants.EmailTemplate;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService{

    @Value("${jwt.app.token.expiration.in.ms}")
    private long EXPIRATION_TIME_IN_MILLI;

    @Value("${google.client.id}")
    private String CLIENT_ID;

    @Value("${fastlearner.user}")
    private String FASTLEARNER_USER;

    @Value("${android.google.client.id}")
    private String ANDROID_CLIENT_ID;
    @Value("${ios.google.client.id}")
    private String IOS_CLIENT_ID;
    private final RestTemplate restTemplate;
    private final IUserService userService;
    private final IRoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final IOtpService otpService;
    private final IEmailService emailService;
    private final IUserProfileService userProfileService;
    private final IAuthenticationOtpService authenticationOtpService;
    private final ICourseService courseService;

    private final ISubscribedUserService subscribedUserService;
    @Autowired
    private TokenRepository tokenRepository;


    private final ISubscriptionPermissionService subscriptionPermissionService;

    @Override
    public TokenResponse socialLogin(String token, String provider, String clientType, String userName) throws AuthenticationException, InternalServerException, BadRequestException {
        if(Objects.isNull(token)){ throw new BadRequestException("Required request parameter 'token' is not present"); }

        if(Objects.isNull(provider)){ throw new BadRequestException("Required request parameter 'provider' is not present"); }

        if (!AuthProvider.isValidProvider(provider)) { throw new BadRequestException("Invalid authentication provider: " + provider); }

        String clientId=null;
       if (clientType!=null && !clientType.isEmpty()){

        if ("ios".equalsIgnoreCase(clientType)) {
            clientId = IOS_CLIENT_ID;
        } else if ("android".equalsIgnoreCase(clientType)) {
            clientId = ANDROID_CLIENT_ID;
        }
       }else {
           clientId=CLIENT_ID;
       }

        ISocialLoginService loginService = SocialLoginFactory.build(AuthProvider.valueOf(provider), restTemplate, userService, roleService, jwtUtils, userProfileService);
        return loginService.login(token, clientId, userName);
    }

    @Override
    public TokenResponse localLogin(LocalAuthRequest request) throws BadRequestException, ForbiddenException,EntityNotFoundException {
        log.info("Logging into system via local email and password.");
        User user = null;
        try {
            user = userService.findByEmail(request.getEmail().toLowerCase());
        } catch (EntityNotFoundException e) {
            log.info("Email or password is incorrect.");
            throw new BadRequestException("User does not Exist.");
        }
        if (!user.isActive()) {
            log.error("User account is disabled. Cannot proceed with login.");
            throw new ForbiddenException("Your account is disabled. Please contact FastLearner Support to enable your account.", "403_FORBIDDEN");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.info("Email or password is incorrect.");
            throw new BadRequestException("Email or password is incorrect.");
        }

        try {
            user.setLoginTimestamp(new Date());
            user=userService.save(user);
        } catch (InternalServerException e) {
            throw new RuntimeException(e);
        }

        // Generate JWT tokens
        String jwtToken = jwtUtils.generateJwtToken(request.getEmail().toLowerCase(), user);
        String refreshToken = jwtUtils.doGenerateRefreshToken(request.getEmail().toLowerCase(), user);

        return TokenResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiredInSec((int) EXPIRATION_TIME_IN_MILLI / 1000)
                .name(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() == null ? null : user.getRole().getType())
                .isSubscribed(user.isSubscribed())
                .age(user.getAge())
                .build();
    }

    @Override
    public JwtTokenResponse refreshToken(String token, Principal principal) throws EntityNotFoundException {
        log.info("Refresh token is called for user: "+principal.getName());
        JwtTokenResponse response = new JwtTokenResponse();
        User user = userService.findByEmail(principal.getName());
        response.setToken(jwtUtils.generateJwtToken(principal.getName(), user));
        response.setRefresh_token(jwtUtils.doGenerateRefreshToken(principal.getName(), user));
        log.info("Refresh token is called for user: "+principal.getName());
        return response;
    }

    @Transactional(rollbackFor = { InternalServerException.class, EntityAlreadyExistException.class })
    @Override
    public TokenResponse localRegister(LocalRegisterRequest request) throws InternalServerException, EntityAlreadyExistException {
        log.info("Logging into system via local email and password.");
        User savedUser = null;
        try {
            savedUser = userService.findByEmail(request.getEmail());
        } catch (EntityNotFoundException e) {}
        if (savedUser != null) {
            throw new EntityAlreadyExistException("User already exists.");
        }

        savedUser = userService.save(User.builder()
                        .fullName(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .provider(AuthProvider.LOCAL)
                        .salesRaise(1.0)
                        .loginTimestamp(new Date())
                        .isActive(true)
                        .creationDate(new Date())
                        .dob((request.getDob()))
                        .age(request.getAge())

                .build());

        UserProfile userProfile = new UserProfile();
        userProfile.setCreatedBy(savedUser.getId());
        userProfile.setCreationDate(new Date());

        userProfileService.createProfile(userProfile, savedUser);

        // Generate JWT tokens
        String jwtToken = jwtUtils.generateJwtToken(request.getEmail(), savedUser);
        String refreshToken = jwtUtils.doGenerateRefreshToken(request.getEmail(), savedUser);

        return TokenResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiredInSec((int) EXPIRATION_TIME_IN_MILLI / 1000)
                .name(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole() == null ? null : savedUser.getRole().getType())
                .isSubscribed(savedUser.isSubscribed())
                .dob((savedUser.getDob()))
                .age(savedUser.getAge())
                .build();
    }

    @Override
    public Message<String> sendingLinkForResettingPassword(String email) throws EntityNotFoundException, MessagingException {
        log.info("Resetting password for user with email: "+email);
        User user = userService.findByEmail(email);
        int otp = otpService.createOtp(user);
        emailService.sendOtpEmailForResettingPassword(user, otp);
        log.info("Otp generated for the user: "+user.getFullName());
        return new Message<String>()
                .setData("Password reset link sent successfully.")
                .setMessage("Password reset link sent successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    @Override
    public Message<String> sendLinkForResettingPassword(String email) throws EntityNotFoundException, MessagingException {
        log.info("Resetting password for user with email: "+email);
        User user = userService.findByEmail(email);
        int otp = otpService.createOtp(user);
        log.info("Otp generated for the user: "+user.getFullName()+" "+otp);
        emailService.sendEmail(
                user.getEmail(),
                "Resetting password",
                EmailTemplate.FIRST_PART_OTP_RESET_EMAIL_TEMPLATE + String.valueOf(otp) + EmailTemplate.SECOND_PART_OTP_EMAIL_TEMPLATE,
                true);
        log.info("Authentication OTP sent successfully: "+user.getFullName());

        return new Message<String>()
                .setData("Password reset link sent successfully.")
                .setMessage("Password reset link sent successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    @Override
    public Message<String> reSendLinkForResettingPassword(String email) throws EntityNotFoundException {
        log.info("Resetting password for user with email: "+email);
        User user = userService.findByEmail(email);
        if (user!=null) {
            Boolean isUserOtpDeleted = otpService.findByUserId(user);
            if (isUserOtpDeleted == null || !isUserOtpDeleted) {
               log.info("Otp already deleted by Scheduler");
            }
            int otp = otpService.createOtp(user);
            log.info("Otp generated for the user: " + user.getFullName());
            emailService.sendEmail(
                    user.getEmail(),
                    "Resetting password",
                    EmailTemplate.FIRST_PART_OTP_RESET_EMAIL_TEMPLATE + String.valueOf(otp) + EmailTemplate.SECOND_PART_OTP_EMAIL_TEMPLATE,
                    true);
            log.info("Authentication OTP sent successfully: " + user.getFullName());
        }else {
            return new Message<String>()
                    .setData("User Does not exist")
                    .setMessage("Incorrect Email")
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString());
        }
        return new Message<String>()
                .setData("Password reset link sent successfully.")
                .setMessage("Password reset link sent successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    @Override
    public Message<String> authenticationOtp(LocalRegisterRequest request) throws EntityAlreadyExistException {
        if(Objects.isNull(this.userService.getUserByEmail(request.getEmail()))){
            int otp = otpService.createAuthenticationOtp();
            this.emailService.sendEmail(
                    request.getEmail(),
                    "SignUp Otp",
                    EmailTemplate.FIRST_PART_OTP_EMAIL_TEMPLATE + String.valueOf(otp) + EmailTemplate.SECOND_PART_OTP_EMAIL_TEMPLATE,
                    true);
            log.info("Authentication OTP sent successfully: "+request.getName());
            AuthenticationOtp authenticationOtp = authenticationOtpService.saveAuthenticationOtp(AuthenticationOtp.builder()
                    .otp(otp)
                    .localDateTime(LocalDateTime.now())
                    .name(request.getName())
                    .email(request.getEmail())
//                    .password(passwordEncoder.encode(request
//                    .getPassword()))
                    .password(request.getPassword())
                    .build());

            return new Message<String>()
                    .setData("Authentication OTP sent successfully.")
                    .setMessage("Authentication OTP sent successfully.")
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString());
        }else{
            throw new EntityAlreadyExistException("User already exists.");
        }
    }

    @Override
    public TokenResponse verifyAuthenticationOtp(String email, int otp) throws BadRequestException, EntityAlreadyExistException, InternalServerException {

        if(Objects.isNull(email)){throw new BadRequestException("email cannot be null");}
        if(Objects.isNull(otp)){throw new BadRequestException("otp cannot be null");}
        AuthenticationOtp authenticate = this.authenticationOtpService.verifyAuthenticationOtp(email, otp);
        if(!Objects.isNull(authenticate)){
            return this.localRegister(LocalRegisterRequest.builder()
                    .email(authenticate.getEmail())
                    .name(authenticate.getName())
                    .password(authenticate.getPassword())
                    .build());
        }else {
            throw new BadRequestException("OTP expired");
        }
    }

    @Override
    public Message<String> resendAuthenticationOtp(String email) throws EntityNotFoundException, BadRequestException {
        if(Objects.isNull(email)){throw new BadRequestException("email cannot be null");}
        int otp = otpService.createAuthenticationOtp();
        return this.authenticationOtpService.resendAuthenticationOtp(email, otp);
    }

    @Override
    public Message<String> verifyingOtp(String email, int otp) throws BadRequestException, EntityNotFoundException {

        if (Objects.isNull(email)) {
            log.warn("Email is null, throwing BadRequestException.");
            throw new BadRequestException("Email cannot be null");
        }

        if (Objects.isNull(otp)) {
            log.warn("OTP is null, throwing BadRequestException.");
            throw new BadRequestException("OTP cannot be null");
        }

        log.info("Attempting to verify OTP for email: {}", email);
        Boolean isOtpVerified = otpService.verifyOtp(email, otp);

        if (isOtpVerified) {
            log.info("OTP verification successful for email: {}", email);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString())
                    .setMessage("Verification successful.")
                    .setData("Verified OTP");
        } else {
            log.warn("OTP expired or invalid for email: {}", email);
            throw new BadRequestException("OTP expired or invalid");
        }
    }

    @Override
    public Message<String> resetPassword( String password, String email) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Resetting password.");
        User user = userService.findByEmail(email);
//        otpService.verifyingOtp(user.getId(), value);
        // reset new password
        user.setPassword(passwordEncoder.encode(password));
        userService.save(user);
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Password reset successful.")
                .setData("Password reset successful.");
    }



    //Task executed at 12 PM every day.
//    @Scheduled(cron = "0 0 12 * * ?")
    public void notifyUserAfterTenDaysOfInactivity(){
        log.info("Checking for users who have not logged in for 10 days.");
        List<User> userList;
        try {
//            userList=userService.findAllUsersNotLoggedInForTenDays();
//            if (!userList.isEmpty()){
//                log.info("All users notified successfully.");
//            } else {
//                log.info("No users found who have been inactive for 10 days.");
//            }
        }catch (Exception e){
            log.error("ERROR: "+e.getLocalizedMessage());
        }

    }
    @Override
    public Message<String> handleUserAccount(String email, String action) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Handling user account action: {}", action);

        User user = userService.findByEmail(email);
        if (Objects.isNull(user)) {
            log.error("User not found for email: {}", email);
            throw new EntityNotFoundException("User not found with email: " + email);
        }

        switch (action.toLowerCase()) {
            case "disable":
                user.setActive(false);
                userService.save(user);
                log.info("User account disabled for email: {}", email);
                return new Message<String>()
                        .setStatus(HttpStatus.OK.value())
                        .setCode(HttpStatus.OK.toString())
                        .setMessage("User account de-activated successfully.")
                        .setData("User account de-activated successfully.");

            case "delete":
                user.setEmail("anonymous-" + user.getId());
                user.setFullName("anounymous" +user.getId());

                user.setActive(false);
                user = userService.save(user);
                List<Course> courses = courseService.findByInstructorIdAndCourseStatus(user, CourseStatus.PUBLISHED);
                if (courses.isEmpty()) {
                    log.error("No courses found for instructor ID: {}", user.getId());
                }
                User staticUser = userService.findByEmail(FASTLEARNER_USER);
                for(Course course : courses){
                    course.setCreatedBy(staticUser.getId());
                }

                this.courseService.saveAll(courses);
                if (user.getId() != null) {
                    boolean isUserProfileSaved = userProfileService.disableSocialLinks(user);
                    if (!isUserProfileSaved) {
                        log.error("Failed to disable social links for user ID: {}", user.getId());
                        return new Message<String>()
                                .setStatus(HttpStatus.OK.value())
                                .setCode(HttpStatus.OK.toString())
                                .setMessage("User account not deleted.")
                                .setData("Failed to disable social links.");
                    }
                    log.info("User account deleted for email: {}", email);
                    return new Message<String>()
                            .setStatus(HttpStatus.OK.value())
                            .setCode(HttpStatus.OK.toString())
                            .setMessage("User account deleted successfully.")
                            .setData("User account deleted successfully.");
                } else {
                    return new Message<String>()
                            .setStatus(HttpStatus.OK.value())
                            .setCode(HttpStatus.OK.toString())
                            .setMessage("User account not deleted.")
                            .setData("User account not deleted.");
                }

            default:
                log.error("Invalid action: {}", action);
                throw new BadRequestException("Invalid action. Allowed values are 'disable' or 'delete'.");
        }
    }

    @Override
    public Message tokenValidation(String token, Principal principal) throws EntityNotFoundException {
        if (token == null || token.isEmpty()) {
            throw new EntityNotFoundException("Token is missing");
        }

        // Validate token
        if (!jwtUtils.validateToken(token)) {
            throw new EntityNotFoundException("Invalid or expired token");
        }

        return new Message<>()
                .setMessage("Token is valid")
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value());
    }


}
