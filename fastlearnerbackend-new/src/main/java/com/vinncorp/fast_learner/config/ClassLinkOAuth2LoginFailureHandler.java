package com.vinncorp.fast_learner.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassLinkOAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ClassLinkOAuth2FrontendRedirectUriBuilder redirectUriBuilder;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.error(
                "ClassLink OAuth authentication failed: {} | callback params: error={}, error_description={}, code_present={}, state={}",
                exception.getMessage(),
                request.getParameter("error"),
                request.getParameter("error_description"),
                request.getParameter("code") != null,
                request.getParameter("state"),
                exception);

        response.sendRedirect(redirectUriBuilder.buildErrorRedirectUrl("classlink_login_failed"));
    }
}
