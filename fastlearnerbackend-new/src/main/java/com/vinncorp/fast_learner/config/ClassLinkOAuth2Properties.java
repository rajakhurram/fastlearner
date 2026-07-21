package com.vinncorp.fast_learner.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "classlink.oauth2")
public class ClassLinkOAuth2Properties {

    private boolean enabled = false;

    /**
     * Frontend route to receive JWT query params after successful ClassLink login.
     */
    private String callbackPath = "/auth/sign-in";

    private String clientId;

    private String clientSecret;

    /**
     * Explicit OIDC endpoints (avoids startup fetch to /.well-known/openid-configuration).
     */
    private String authorizationUri = "https://launchpad.classlink.com/oauth2/v2/auth";

    private String tokenUri = "https://launchpad.classlink.com/oauth2/v2/token";

    private String userInfoUri = "https://launchpad.classlink.com/oauth2/v2/userinfo";

    private String jwkSetUri = "https://launchpad.classlink.com/oauth2/v2/jwks";

    /**
     * ClassLink OneClick profile API (launchpad userinfo returns HTML, not JSON).
     */
    private String profileUri = "https://nodeapi.classlink.com/v2/my/info";

    /**
     * ClassLink portal has PKCE enabled by default for Partner-initiated flows.
     * LaunchPad-initiated callback exchanges code without PKCE verifier.
     */
    private boolean pkceEnabled = true;

    /**
     * Exact redirect_uri / Launch URL registered in ClassLink Partner Portal.
     * Example local: http://localhost:8443/login/oauth2/code/classlink
     */
    private String redirectUri = "http://localhost:8443/login/oauth2/code/classlink";
}
