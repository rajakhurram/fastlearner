package com.vinncorp.fast_learner.services.auth.social_login;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.services.authorize_net.AuthorizeNetSubscriptionService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import com.vinncorp.fast_learner.util.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassLinkLoginService {

    private final IUserService userService;
    private final IUserProfileService userProfileService;
    private final JwtUtils jwtUtils;
    private final AuthorizeNetSubscriptionService authorizeNetSubscriptionService;

    public TokenResponse login(OAuth2User oauth2User) throws InternalServerException, EntityNotFoundException {
        String email = extractEmail(oauth2User);
        String name = extractName(oauth2User);

        if (!StringUtils.hasText(email)) {
            throw new InternalServerException("ClassLink account does not include an email address.");
        }

        User user = registerUserIfNotAlreadyRegistered(email, name);
        authorizeNetSubscriptionService.freeSignUpSubscriptionNew(1L, user);

        try {
            user = userService.findByEmail(email);
        } catch (EntityNotFoundException e) {
            throw new InternalServerException("ClassLink user could not be loaded after registration.");
        }

        if (user.getRole() == null) {
            log.info("Assigning default STUDENT role for ClassLink user {}", email);
            userService.addRoleForUser(UserRole.STUDENT, email);
            user = userService.findByEmail(email);
        }

        user.setLoginTimestamp(new Date());
        user = userService.save(user);

        String jwtToken = jwtUtils.generateJwtToken(user.getEmail(), user);
        String refreshToken = jwtUtils.doGenerateRefreshToken(user.getEmail(), user);

        return TokenResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiredInSec((int) jwtUtils.getJwtExpirationMs() / 1000)
                .name(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() == null ? null : user.getRole().getType())
                .isSubscribed(user.isSubscribed())
                .build();
    }

    private User registerUserIfNotAlreadyRegistered(String email, String name) throws InternalServerException {
        try {
            return userService.findByEmail(email);
        } catch (EntityNotFoundException e) {
            log.info("ClassLink user not found in database, registering new user.");
        }

        User user = userService.save(User.builder()
                .email(email.toLowerCase())
                .fullName(StringUtils.hasText(name) ? name : email)
                .provider(AuthProvider.CLASSLINK)
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

    private String extractEmail(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        Object email = firstNonBlankAttribute(attributes, "email", "Email", "mail", "preferred_username");
        return email == null ? null : String.valueOf(email).trim().toLowerCase();
    }

    private String extractName(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        Object name = firstNonBlankAttribute(attributes, "name", "displayName", "full_name", "given_name");
        if (name != null) {
            return String.valueOf(name).trim();
        }
        Object givenName = attributes.get("given_name");
        Object familyName = attributes.get("family_name");
        if (givenName != null || familyName != null) {
            return String.format("%s %s",
                    givenName == null ? "" : givenName,
                    familyName == null ? "" : familyName).trim();
        }
        return oauth2User.getName();
    }

    private Object firstNonBlankAttribute(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return value;
            }
        }
        return null;
    }
}
