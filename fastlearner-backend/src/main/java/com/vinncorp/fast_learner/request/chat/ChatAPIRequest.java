package com.vinncorp.fast_learner.request.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatAPIRequest {

    private String transcript;
    private String question;
}
