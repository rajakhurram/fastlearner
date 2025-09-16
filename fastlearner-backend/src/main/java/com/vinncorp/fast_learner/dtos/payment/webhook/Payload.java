package com.vinncorp.fast_learner.dtos.payment.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {
    private String entityName;
    private String id;
    private String name;
    private int amount;
    private String status;
    private Profile profile;
}
