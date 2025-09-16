package com.vinncorp.fast_learner.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalAuthRequest {

    @Email(message = "Email is not valid")
    private String email;

    @NotBlank(message = "Password should not be empty")
    private String password;
}
