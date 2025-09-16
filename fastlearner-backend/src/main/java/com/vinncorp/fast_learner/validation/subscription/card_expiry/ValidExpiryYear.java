package com.vinncorp.fast_learner.validation.subscription.card_expiry;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ExpiryYearValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExpiryYear {
    String message() default "Expiry year must be the current year or later.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
