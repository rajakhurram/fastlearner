package com.vinncorp.fast_learner.response.auth.social_login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TokenInfoResponse {
    @JsonProperty("aud")
    private String aud;

    @JsonProperty("iss")
    private String iss;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private String email_verified;
}
