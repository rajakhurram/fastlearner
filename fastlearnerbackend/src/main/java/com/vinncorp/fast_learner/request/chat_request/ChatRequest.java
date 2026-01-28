package com.vinncorp.fast_learner.request.chat_request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChatRequest {
    private String fileId;
    private String prompt;

}