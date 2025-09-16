package com.vinncorp.fast_learner.dtos.payment.payment_profile;


import com.vinncorp.fast_learner.validation.subscription.card_expiry.ValidExpiryYear;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProfileDetailRequest {

    private Long id;

    @NotNull(message = "First name is required.")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters.")
    private String firstName;

    @NotNull(message = "Last name is required.")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters.")
    private String lastName;

    @NotNull(message = "Card number is required.")
    @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13 to 19 digits long.")
    private String cardNumber;

    @NotNull(message = "Expiry month is required.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expiry month must be a valid month (01 to 12).")
    private String expiryMonth;

    @NotNull(message = "Expiry year is required.")
    @Pattern(regexp = "^[0-9]{2}$", message = "Expiry year must be exactly two digits.")
    @ValidExpiryYear
    private String expiryYear;

    @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits long.")
    private String cvv;

    @NotNull(message = "Is save should not be null")
    private Boolean isSave = false;
}
