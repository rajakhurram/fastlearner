package com.vinncorp.fast_learner.dtos.user;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDto {
    @NotBlank(message = "Old password must be given")
    private String oldPassword;

    @NotBlank(message = "New password must be given")
    private String newPassword;
}
