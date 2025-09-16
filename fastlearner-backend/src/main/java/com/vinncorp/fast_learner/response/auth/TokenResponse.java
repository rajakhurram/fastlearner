package com.vinncorp.fast_learner.response.auth;

import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String token;
    private String refreshToken;
    private int expiredInSec;
    private String email;
    private String name;
    private String role;
    private boolean isSubscribed = false;
    private Integer age;
    @Past(message = "Date of birth must be in the past")

    private LocalDate dob;
}
