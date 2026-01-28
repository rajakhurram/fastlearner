package com.vinncorp.fast_learner.validation.subscription.subscription_request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SubscriptionRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSubscriptionRequest {
    String message() default "Invalid subscription request: If coupon is provided, paymentDetail must be null. Otherwise, paymentDetail is required.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
