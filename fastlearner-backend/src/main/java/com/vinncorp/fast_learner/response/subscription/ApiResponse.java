package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.message.MessagesType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {

    private String refId;
    private MessagesType messages;
    private String sessionToken;
}
