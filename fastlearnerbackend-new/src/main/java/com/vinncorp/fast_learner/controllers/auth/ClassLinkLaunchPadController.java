package com.vinncorp.fast_learner.controllers.auth;

import com.vinncorp.fast_learner.config.ClassLinkOAuth2FrontendRedirectUriBuilder;
import com.vinncorp.fast_learner.config.ClassLinkOAuth2Properties;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.services.auth.social_login.ClassLinkLaunchPadAuthService;
import com.vinncorp.fast_learner.services.auth.social_login.ClassLinkLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Partner Portal Launch URL endpoint.
 * <p>
 * ClassLink LaunchPad opens {@code /login/oauth2/code/classlink}:
 * <ul>
 *   <li>Without {@code code} → redirect user to ClassLink authorize</li>
 *   <li>With {@code code} → exchange token, create FastLearner JWT, redirect frontend</li>
 * </ul>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@ConditionalOnProperty(name = "classlink.oauth2.enabled", havingValue = "true")
public class ClassLinkLaunchPadController {

    private final ClassLinkOAuth2Properties properties;
    private final ClassLinkLaunchPadAuthService classLinkLaunchPadAuthService;
    private final ClassLinkLoginService classLinkLoginService;
    private final ClassLinkOAuth2FrontendRedirectUriBuilder redirectUriBuilder;

    @GetMapping("/login/oauth2/code/classlink")
    public RedirectView handleLaunchPad(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription) {

        if (StringUtils.hasText(error)) {
            log.error("ClassLink LaunchPad returned error={} description={}", error, errorDescription);
            return new RedirectView(redirectUriBuilder.buildErrorRedirectUrl("classlink_login_failed"));
        }

        if (!StringUtils.hasText(code)) {
            log.info("ClassLink LaunchPad opened Launch URL without code — starting authorize redirect.");
            return new RedirectView(buildAuthorizationUrl());
        }

        try {
            String redirectUri = resolveRedirectUri();
            Map<String, Object> profile =
                    classLinkLaunchPadAuthService.exchangeCodeAndLoadProfile(code, redirectUri);

            String nameAttribute = profile.containsKey("sub") ? "sub" : "email";
            OAuth2User oauth2User = new DefaultOAuth2User(
                    Collections.emptyList(),
                    profile,
                    nameAttribute);

            TokenResponse tokenResponse = classLinkLoginService.login(oauth2User);
            return new RedirectView(redirectUriBuilder.buildSuccessRedirectUrl(tokenResponse));
        } catch (Exception ex) {
            log.error("ClassLink LaunchPad login failed: {}", ex.getMessage(), ex);
            return new RedirectView(redirectUriBuilder.buildErrorRedirectUrl("classlink_login_failed"));
        }
    }

    private String buildAuthorizationUrl() {
        return UriComponentsBuilder
                .fromHttpUrl(properties.getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", resolveRedirectUri())
                .queryParam("scope", "openid profile email")
                .queryParam("state", UUID.randomUUID().toString())
                .encode()
                .build()
                .toUriString();
    }

    private String resolveRedirectUri() {
        if (StringUtils.hasText(properties.getRedirectUri())) {
            return properties.getRedirectUri();
        }
        return "http://localhost:8443/login/oauth2/code/classlink";
    }
}
