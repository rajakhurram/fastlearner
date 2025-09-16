package com.vinncorp.fast_learner.services.auth.social_login;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.AuthenticationException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.role.IRoleService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.response.auth.social_login.TokenInfoResponse;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Slf4j
@Service
public class GoogleLoginService implements ISocialLoginService {
    private String CLIENT_ID;
    private String GOOGLE_VERIFICATION_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=";
    private String GOOGLE_ISSUER_URL = "https://accounts.google.com";

    private final RestTemplate restTemplate;
    private final IUserService userService;
    private final IRoleService roleService;
    private final JwtUtils jwtUtils;
    private final IUserProfileService userProfileService;

    public GoogleLoginService(RestTemplate restTemplate, IUserService userService,
                              IRoleService roleService, JwtUtils jwtUtils,
                              IUserProfileService userProfileService) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.roleService = roleService;
        this.jwtUtils = jwtUtils;
        this.userProfileService = userProfileService;
    }

    @Override
    public TokenResponse login(String token, String CLIENT_ID, String userName) throws AuthenticationException, InternalServerException {
        String tokenInfoUrl = GOOGLE_VERIFICATION_URL + token;
        this.CLIENT_ID = CLIENT_ID;

        // Send a GET request to Google's token info endpoint to verify the ID token
        try {
//            GoogleTokenInfoResponse response = restTemplate.getForObject(tokenInfoUrl, GoogleTokenInfoResponse.class);
            ResponseEntity<TokenInfoResponse> responseEntity = restTemplate.exchange(
                    tokenInfoUrl, HttpMethod.GET, null, TokenInfoResponse.class);
            TokenInfoResponse response = responseEntity.getBody();
            // Check if the ID token is valid and intended for your app
            if (response == null || !CLIENT_ID.equals(response.getAud()) || !GOOGLE_ISSUER_URL.equals(response.getIss())) {
                log.error("Authentication failed");
                throw new AuthenticationException("Authentication failed");
            }
            User savedUser = registerUserIfNotAlreadyRegistered(response);

            // Generate JWT tokens
            String jwtToken = jwtUtils.generateJwtToken(savedUser.getEmail(), savedUser);
            String refreshToken = jwtUtils.doGenerateRefreshToken(savedUser.getEmail(), savedUser);

            return TokenResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .expiredInSec((int) jwtUtils.getJwtExpirationMs() / 1000)
                    .name(savedUser.getFullName())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole() == null ? null : savedUser.getRole().getType())
                    .isSubscribed(savedUser.isSubscribed())
                    .build();
        }catch(Exception e) {
            throw new InternalServerException("Authentication failed");
        }
    }

    public User registerUserIfNotAlreadyRegistered(TokenInfoResponse userInfo) throws InternalServerException {
        User user = null;
        try {
            user = userService.findByEmail(userInfo.getEmail());
            return user;
        } catch (EntityNotFoundException e) {
            log.info("User not found in database, Registering new user.");
        }
        user = userService.save(User.builder()
                        .email(userInfo.getEmail())
                        .fullName(userInfo.getName())
                        .provider(AuthProvider.GOOGLE)
                        .salesRaise(1.0)
                        .isActive(true)
                        .loginTimestamp(new Date())
                        .creationDate(new Date())
                .build());

        UserProfile userProfile = new UserProfile();
        userProfile.setCreationDate(new Date());
        userProfile.setCreatedBy(user.getId());
        userProfileService.createProfile(userProfile, user);

        return user;
    }
}
