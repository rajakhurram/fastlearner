package com.vinncorp.fast_learner.config;

import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.services.auth.social_login.ClassLinkLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassLinkOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ClassLinkLoginService classLinkLoginService;
    private final ClassLinkOAuth2FrontendRedirectUriBuilder redirectUriBuilder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            TokenResponse tokenResponse = classLinkLoginService.login(oauth2User);
            response.sendRedirect(redirectUriBuilder.buildSuccessRedirectUrl(tokenResponse));
        } catch (Exception e) {
            log.error("ClassLink OAuth login failed: {}", e.getMessage(), e);
            response.sendRedirect(redirectUriBuilder.buildErrorRedirectUrl("classlink_login_failed"));
        }
    }
}
