package com.vinncorp.fast_learner.services.auth.social_login;

import com.vinncorp.fast_learner.config.ClassLinkOAuth2Properties;
import com.vinncorp.fast_learner.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles ClassLink LaunchPad-initiated SSO: exchange authorization code for tokens
 * without a prior Spring OAuth2 authorization request session.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "classlink.oauth2.enabled", havingValue = "true")
public class ClassLinkLaunchPadAuthService {

    private final ClassLinkOAuth2Properties properties;
    private final RestTemplate restTemplate;

    public Map<String, Object> exchangeCodeAndLoadProfile(String code, String redirectUri)
            throws InternalServerException {
        if (!StringUtils.hasText(code)) {
            throw new InternalServerException("ClassLink authorization code is missing.");
        }

        Map<String, Object> tokenResponse = exchangeAuthorizationCode(code, redirectUri);
        String accessToken = stringValue(tokenResponse.get("access_token"));
        if (!StringUtils.hasText(accessToken)) {
            throw new InternalServerException("ClassLink token response did not include access_token.");
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        Object idToken = tokenResponse.get("id_token");
        if (idToken != null) {
            profile.putAll(decodeJwtClaimsWithoutVerification(String.valueOf(idToken)));
        }

        if (!hasUsableEmail(profile)) {
            profile.putAll(fetchProfile(accessToken));
        }

        normalizeProfile(profile);

        if (!hasUsableEmail(profile)) {
            throw new InternalServerException("ClassLink profile does not include an email address.");
        }

        return profile;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchangeAuthorizationCode(String code, String redirectUri)
            throws InternalServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    properties.getTokenUri(),
                    HttpMethod.POST,
                    new HttpEntity<>(form, headers),
                    Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || body.isEmpty()) {
                throw new InternalServerException("Empty ClassLink token response.");
            }
            return body;
        } catch (InternalServerException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("ClassLink token exchange failed: {}", ex.getMessage(), ex);
            throw new InternalServerException("ClassLink token exchange failed: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchProfile(String accessToken) throws InternalServerException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<Map> response = restTemplate.exchange(
                    properties.getProfileUri(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);
            Map<String, Object> body = response.getBody();
            return body != null ? body : Collections.emptyMap();
        } catch (Exception ex) {
            log.error("ClassLink profile fetch failed: {}", ex.getMessage(), ex);
            throw new InternalServerException("ClassLink profile fetch failed: " + ex.getMessage());
        }
    }

    private Map<String, Object> decodeJwtClaimsWithoutVerification(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                return Collections.emptyMap();
            }
            String json = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception ex) {
            log.warn("Unable to decode ClassLink id_token claims: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private void normalizeProfile(Map<String, Object> attributes) {
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

    private boolean hasUsableEmail(Map<String, Object> attributes) {
        Object email = attributes.get("email");
        if (!hasText(email)) {
            email = attributes.get("Email");
        }
        return hasText(email);
    }

    private boolean hasText(Object value) {
        return value != null && StringUtils.hasText(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
