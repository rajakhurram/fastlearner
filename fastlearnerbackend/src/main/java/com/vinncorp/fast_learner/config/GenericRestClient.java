package com.vinncorp.fast_learner.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class GenericRestClient {

    private final RestTemplate restTemplate;

    @Value("${ai.grader.domain}")
    private String AI_GRADER_DOMAIN;

    @Value("${ai.grader.api.key}")
    private String AI_GRADER_API_KEY;

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

    public <T> T makeRequestForAIGrader(
            String url,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType
    ) throws InternalServerException {
        HttpEntity<Object> entity;
        if (requestBody instanceof HttpEntity<?>) {
            HttpEntity<?> originalEntity = (HttpEntity<?>) requestBody;
            HttpHeaders modifiableHeaders = new HttpHeaders();
            modifiableHeaders.putAll(originalEntity.getHeaders());
            modifiableHeaders.set("X-API-KEY", AI_GRADER_API_KEY);
            entity = new HttpEntity<>(originalEntity.getBody(), modifiableHeaders);
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", AI_GRADER_API_KEY);
            entity = new HttpEntity<>(requestBody, headers);
        }

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    AI_GRADER_DOMAIN + url,
                    method,
                    entity,
                    responseType
            );
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // 🔹 Try to parse the response body into a Message object
            try {
                ObjectMapper mapper = new ObjectMapper();
                Message<?> errorMessage = mapper.readValue(ex.getResponseBodyAsString(), Message.class);

                // If the caller expects a Message<T>, return it directly
                if (responseType.isAssignableFrom(Message.class)) {
                    return responseType.cast(errorMessage);
                }

                // Otherwise, wrap it into InternalServerException
                throw new InternalServerException(errorMessage.getMessage());
            } catch (Exception parseEx) {
                // Parsing failed, throw generic exception
                throw new InternalServerException("AI Grader Error: " + ex.getResponseBodyAsString());
            }

        } catch (ResourceAccessException ex) {
            throw new InternalServerException("Failed to connect to AI Grader service: " + ex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerException("Unexpected error during AI Grader request: " + ex.getMessage());
        }
    }

    public <T> T makeRequestForAIGrader(
            String url,
            HttpMethod method,
            Object requestBody,
            ParameterizedTypeReference<T> responseType
    ) throws InternalServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", AI_GRADER_API_KEY);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<T> response;

        try {
            response = restTemplate.exchange(
                    AI_GRADER_DOMAIN + url,
                    method,
                    entity,
                    responseType
            );
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }

        return response.getBody();
    }
    public <T> T makeRequestForAIGraderForMultiPart(
            String url,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType,
            boolean isMultipart // new flag
    ) throws InternalServerException {
        HttpEntity<Object> entity;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", AI_GRADER_API_KEY);

        if (isMultipart && requestBody instanceof MultiValueMap) {
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            entity = new HttpEntity<>(requestBody, headers);
        } else if (requestBody instanceof HttpEntity<?>) {
            HttpEntity<?> originalEntity = (HttpEntity<?>) requestBody;
            HttpHeaders modifiableHeaders = new HttpHeaders();
            modifiableHeaders.putAll(originalEntity.getHeaders());
            modifiableHeaders.set("X-API-KEY", AI_GRADER_API_KEY);
            entity = new HttpEntity<>(originalEntity.getBody(), modifiableHeaders);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            entity = new HttpEntity<>(requestBody, headers);
        }

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    AI_GRADER_DOMAIN + url,
                    method,
                    entity,
                    responseType
            );
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Message<?> errorMessage = mapper.readValue(ex.getResponseBodyAsString(), Message.class);
                if (responseType.isAssignableFrom(Message.class)) {
                    return responseType.cast(errorMessage);
                }
                throw new InternalServerException(errorMessage.getMessage());
            } catch (Exception parseEx) {
                throw new InternalServerException("AI Grader Error: " + ex.getResponseBodyAsString());
            }
        } catch (ResourceAccessException ex) {
            throw new InternalServerException("Failed to connect to AI Grader service: " + ex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerException("Unexpected error during AI Grader request: " + ex.getMessage());
        }
    }
}
