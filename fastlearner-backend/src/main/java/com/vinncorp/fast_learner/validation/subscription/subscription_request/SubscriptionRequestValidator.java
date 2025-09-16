package com.vinncorp.fast_learner.validation.subscription.subscription_request;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class SubscriptionRequestValidator implements ConstraintValidator<ValidSubscriptionRequest, SubscriptionRequest> {

    @Override
    public boolean isValid(SubscriptionRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null case
        }

        boolean isCouponProvided = StringUtils.isNotBlank(request.getCoupon());
        boolean isPaymentDetailProvided = request.getPaymentDetail() != null;
        boolean isSubscriptionIdProvided = request.getSubscriptionId() != null;

        // If coupon is NOT provided, paymentDetail must be present
        if (!isCouponProvided && !isPaymentDetailProvided) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Payment details are required when coupon is not provided.")
                    .addPropertyNode("paymentDetail").addConstraintViolation();
            return false;
        }

        // If coupon is NOT provided, subscriptionId must be present
        if (!isCouponProvided && !isSubscriptionIdProvided) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Subscription ID is required when coupon is not provided.")
                    .addPropertyNode("subscriptionId").addConstraintViolation();
            return false;
        }

        return true;
    }
}
