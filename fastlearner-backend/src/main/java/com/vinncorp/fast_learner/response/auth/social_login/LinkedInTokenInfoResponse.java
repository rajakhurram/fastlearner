package com.vinncorp.fast_learner.response.auth.social_login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedInTokenInfoResponse {

    @JsonProperty("email_verified")
    private String emailVerified;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("picture")
    private String picture;
}
