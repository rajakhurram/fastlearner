package com.vinncorp.fast_learner.services.auth.social_login;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.services.role.IRoleService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import org.springframework.web.client.RestTemplate;

public class SocialLoginFactory {

    public static ISocialLoginService build(AuthProvider authProvider,
                                            RestTemplate restTemplate,
                                            IUserService userService,
                                            IRoleService roleService,
                                            JwtUtils jwtUtils,
                                            IUserProfileService userProfileService) {
        switch (authProvider) {
            case GOOGLE:
                return new GoogleLoginService(restTemplate, userService, roleService, jwtUtils, userProfileService);
            case APPLE:
                return new AppleLoginService(restTemplate, userService, roleService, jwtUtils, userProfileService);
        }
        return null;
    }
}
