package com.vinncorp.fast_learner.request.contact_us;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactUsRequest {
    @NotEmpty(message = "full name should not be empty.")
    private String fullName;

    @NotEmpty(message = "email should not be empty.")
    @Email(message = "Please provide appropriate email address.")
    private String email;

    @NotEmpty(message = "phone number should not be empty.")
    private String phoneNumber;

    private String description;
}
