package com.vinncorp.fast_learner.dtos.authorize_net.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizeNetWebhookRequest {
    private String notificationId;
    private String eventType;
    private String eventDate;
    private String webhookId;
    private Payload payload;
}
