package com.vinncorp.fast_learner.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ClassLink's launchpad /oauth2/v2/userinfo returns HTML, not OIDC JSON.
 * Load profile data from the OneClick REST API instead.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "classlink.oauth2.enabled", havingValue = "true")
public class ClassLinkOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final ClassLinkOAuth2Properties classLinkOAuth2Properties;
    private final RestTemplate restTemplate;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OidcIdToken idToken = userRequest.getIdToken();
            if (idToken == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_id_token"),
                        "ClassLink did not return an id_token.");
            }

            Map<String, Object> attributes = new LinkedHashMap<>(idToken.getClaims());
            if (!hasUsableEmail(attributes)) {
                attributes.putAll(fetchProfile(userRequest.getAccessToken().getTokenValue()));
            }
            normalizeAttributes(attributes);

            if (!hasUsableEmail(attributes)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_user_info_response"),
                        "ClassLink user profile does not include an email address.");
            }

            OidcUserInfo userInfo = new OidcUserInfo(attributes);
            return new DefaultOidcUser(
                    AuthorityUtils.createAuthorityList("ROLE_USER"),
                    idToken,
                    userInfo);
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to load ClassLink user profile: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info_response"),
                    "Failed to load ClassLink user profile",
                    ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchProfile(String accessToken) {
        String profileUri = classLinkOAuth2Properties.getProfileUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                profileUri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        Map<String, Object> body = response.getBody();
        return body != null ? body : Collections.emptyMap();
    }

    private boolean hasUsableEmail(Map<String, Object> attributes) {
        Object email = firstNonBlank(attributes, "email", "Email", "mail");
        return email != null && StringUtils.hasText(String.valueOf(email));
    }

    private void normalizeAttributes(Map<String, Object> attributes) {
        copyIfMissing(attributes, "email", "Email");
        copyIfMissing(attributes, "name", "DisplayName");
        if (!attributes.containsKey("sub") && attributes.containsKey("UserId")) {
            attributes.put("sub", String.valueOf(attributes.get("UserId")));
        }
    }

    private void copyIfMissing(Map<String, Object> target, String standardKey, String classLinkKey) {
        if (!hasText(target.get(standardKey)) && hasText(target.get(classLinkKey))) {
            target.put(standardKey, target.get(classLinkKey));
        }
    }

    private Object firstNonBlank(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(Object value) {
        return value != null && StringUtils.hasText(String.valueOf(value));
    }
}
