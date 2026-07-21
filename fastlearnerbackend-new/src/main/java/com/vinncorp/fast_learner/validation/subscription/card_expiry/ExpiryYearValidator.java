package com.vinncorp.fast_learner.validation.subscription.card_expiry;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

public class ExpiryYearValidator implements ConstraintValidator<ValidExpiryYear, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false; // Null or empty values are invalid
        }

        try {
            int expiryYear = Integer.parseInt(value);
            int currentYear = Year.now().getValue();

            // Below subscription is doing because we get last two digit of expiry year
            currentYear = currentYear - 2000;
            return expiryYear >= currentYear; // Valid if expiryYear is current or later
        } catch (NumberFormatException e) {
            return false; // Invalid if not a valid number
        }
    }
}
