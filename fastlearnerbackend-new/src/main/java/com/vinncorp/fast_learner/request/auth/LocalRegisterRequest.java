package com.vinncorp.fast_learner.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalRegisterRequest {

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Name should not be empty")
    private String name;

    @NotBlank(message = "Password should not be empty")
    private String password;

    private boolean subscribeNewsletter = true;
    private Integer age;
    @Past(message = "Date of birth must be in the past")

    private LocalDate dob;
}
