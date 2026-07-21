package com.vinncorp.fast_learner.request.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResettingRequest {

    private int value;
    private String password;
    private String email;
}
