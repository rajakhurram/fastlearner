package com.vinncorp.fast_learner.services.payment.webhook;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.payment.webhook.PaymentWebhookRequest;

import java.util.Map;

public interface IPaymentWebhookService {

    // On cancelled, expired or suspended subscription we should have to change the subscription of the user to the
    // free plan and cancel the subscription.
    void subscriptionTermination(PaymentWebhookRequest request) throws EntityNotFoundException, BadRequestException, InternalServerException;

    void logging(String request);

    void paymentSubscription(Map<String, Object> webhookPayload) throws InternalServerException;
}
