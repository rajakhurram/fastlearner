package com.vinncorp.fast_learner.dtos.payment.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {
    private int customerProfileId;
    private int customerPaymentProfileId;
    private int customerShippingAddressId;
}
