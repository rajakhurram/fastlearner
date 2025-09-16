package com.vinncorp.fast_learner.config;

import com.vinncorp.fast_learner.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GenericRestClient {

    private final RestTemplate restTemplate;

    @Value("${fl.payment.api.gateway.key}")
    private String API_KEY;

    @Value("${fl.payment.api.gateway.domain}")
    private String PAYMENT_GATEWAY_DOMAIN;

    public GenericRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T makeRequest(
            String url,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType
    ) throws InternalServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<T> response;

        try {
            response = restTemplate.exchange(
                    PAYMENT_GATEWAY_DOMAIN + url,
                    method,
                    entity,
                    responseType
            );
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }

        return response.getBody();
    }
}
