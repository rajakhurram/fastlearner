package com.vinncorp.fast_learner.request.subscription;

import lombok.Data;

@Data
public class ApiRequest {
    private MerchantAuthenticationType merchantAuthentication;
    private String clientId;
    private String refId;
}
