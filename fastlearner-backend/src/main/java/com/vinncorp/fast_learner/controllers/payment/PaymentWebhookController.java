package com.vinncorp.fast_learner.controllers.payment;

import com.vinncorp.fast_learner.dtos.payment.webhook.PaymentWebhookRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.payment.webhook.IPaymentWebhookService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(APIUrls.PAYMENT_GATEWAY_WEBHOOK)
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final IPaymentWebhookService service;

    @PostMapping(APIUrls.PAYMENT_GATEWAY_SUBSCRIPTION_TERMINATION_URL)
    public ResponseEntity<Object> subscriptionTerminationEvent(@RequestBody PaymentWebhookRequest request)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        service.subscriptionTermination(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(APIUrls.PAYMENT_GATEWAY_SUBSCRIPTION_PAYMENT)
    public ResponseEntity<Object> subscriptionPayment(@RequestBody Map<String, Object> webhookPayload) throws InternalServerException {
        service.paymentSubscription(webhookPayload);
        return ResponseEntity.ok().build();
    }

    @PostMapping(APIUrls.PAYMENT_GATEWAY_WEBHOOK_LOG)
    public ResponseEntity<Object> logging(@RequestBody String request) {

        service.logging(request);
        return ResponseEntity.ok().build();
    }

}
