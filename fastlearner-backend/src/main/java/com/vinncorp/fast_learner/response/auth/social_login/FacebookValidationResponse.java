package com.vinncorp.fast_learner.response.auth.social_login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacebookValidationResponse {

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("is_valid")
    private boolean isValid;

    @JsonProperty("user_id")
    private String userId;
}
