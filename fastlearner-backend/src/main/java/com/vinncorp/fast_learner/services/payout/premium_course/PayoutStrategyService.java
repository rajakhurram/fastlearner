package com.vinncorp.fast_learner.services.payout.premium_course;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.net.RequestOptions;
import com.stripe.param.PayoutCreateParams;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Value;

public interface PayoutStrategyService {
    void executePayout(Course course, User user, String transId, String uuid, Coupon coupon) throws BadRequestException, InternalServerException;

    default String sendPayout(String stripeId, double amount) throws StripeException {
        long total = Math.round(amount * 100);
        PayoutCreateParams payoutParams = PayoutCreateParams.builder()
                .setAmount(total) // Amount in cents
                .setCurrency("usd") // Currency code, e.g., "usd"
                .setMethod(PayoutCreateParams.Method.STANDARD) // Payout method
                .build();

        // Initiate payout on behalf of the connected account
        var payout = Payout.create(payoutParams,
                RequestOptions.builder().setStripeAccount(stripeId).build());

        return payout.getId();
    }
}