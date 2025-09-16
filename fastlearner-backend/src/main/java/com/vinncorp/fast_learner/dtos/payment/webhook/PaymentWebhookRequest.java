package com.vinncorp.fast_learner.dtos.payment.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWebhookRequest {
    private String notificationId;
    private String eventType;
    private String eventDate;
    private String webhookId;
    private Payload payload;
}
