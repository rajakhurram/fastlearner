package com.vinncorp.fast_learner.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(name = "classlink.oauth2.enabled", havingValue = "true")
public class ClassLinkOAuth2Configuration {

    @Bean
    @Primary
    public ClientRegistrationRepository classLinkClientRegistrationRepository(ClassLinkOAuth2Properties properties) {
        if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
            throw new IllegalStateException(
                    "ClassLink OAuth is enabled but classlink.oauth2.client-id or client-secret is missing.");
        }

        String redirectUri = StringUtils.hasText(properties.getRedirectUri())
                ? properties.getRedirectUri()
                : "{baseUrl}/login/oauth2/code/{registrationId}";

        ClientRegistration registration = ClientRegistration.withRegistrationId("classlink")
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope("openid", "profile", "email")
                .authorizationUri(properties.getAuthorizationUri())
                .tokenUri(properties.getTokenUri())
                .userInfoUri(properties.getUserInfoUri())
                .jwkSetUri(properties.getJwkSetUri())
                .userNameAttributeName("sub")
                .clientName("ClassLink")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}
