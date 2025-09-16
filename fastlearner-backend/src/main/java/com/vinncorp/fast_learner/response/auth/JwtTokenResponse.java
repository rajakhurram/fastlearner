package com.vinncorp.fast_learner.response.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenResponse {

    private String token;
    private String refresh_token;
}
