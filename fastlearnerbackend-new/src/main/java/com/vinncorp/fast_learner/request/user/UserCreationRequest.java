package com.vinncorp.fast_learner.request.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {

    @NotBlank(message = "Full name should not empty.")
    private String fullName;

    @NotBlank(message = "Email should not empty.")
    @Email(message = "Please provide valid email.")
    private String email;

    @NotBlank(message = "Password should not empty.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
    private String password;

    @NotBlank(message = "Authentication provider should not be empty.")
    private String provider;

    @NotBlank(message = "Role should not be empty.")
    private String role;
}
