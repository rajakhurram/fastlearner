package com.vinncorp.fast_learner.config;

import com.vinncorp.fast_learner.response.auth.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds frontend redirect URLs for Angular hash routing ({@code /#/auth/sign-in}).
 */
@Component
public class ClassLinkOAuth2FrontendRedirectUriBuilder {

    private final ClassLinkOAuth2Properties classLinkOAuth2Properties;

    @Value("${frontend.domain.url}")
    private String frontendDomainUrl;

    public ClassLinkOAuth2FrontendRedirectUriBuilder(ClassLinkOAuth2Properties classLinkOAuth2Properties) {
        this.classLinkOAuth2Properties = classLinkOAuth2Properties;
    }

    public String buildSuccessRedirectUrl(TokenResponse tokenResponse) {
        String callbackPath = normalizeCallbackPath(classLinkOAuth2Properties.getCallbackPath());
        String query = "token=" + encode(tokenResponse.getToken())
                + "&refreshToken=" + encode(tokenResponse.getRefreshToken())
                + "&expiredInSec=" + tokenResponse.getExpiredInSec()
                + "&name=" + encode(tokenResponse.getName())
                + "&email=" + encode(tokenResponse.getEmail())
                + "&subscribed=" + tokenResponse.isSubscribed();
        if (StringUtils.hasText(tokenResponse.getRole())) {
            query += "&role=" + encode(tokenResponse.getRole());
        }
        return buildHashRouteRedirect(callbackPath, query);
    }

    public String buildErrorRedirectUrl(String error) {
        String callbackPath = normalizeCallbackPath(classLinkOAuth2Properties.getCallbackPath());
        return buildHashRouteRedirect(callbackPath, "error=" + encode(error));
    }

    private String buildHashRouteRedirect(String callbackPath, String query) {
        return UriComponentsBuilder
                .fromHttpUrl(trimTrailingSlash(frontendDomainUrl))
                .fragment(callbackPath + "?" + query)
                .build(false)
                .toUriString();
    }

    private String normalizeCallbackPath(String callbackPath) {
        if (!StringUtils.hasText(callbackPath)) {
            callbackPath = "/auth/sign-in";
        }
        if (!callbackPath.startsWith("/")) {
            callbackPath = "/" + callbackPath;
        }
        return callbackPath;
    }

    private String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String encode(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
