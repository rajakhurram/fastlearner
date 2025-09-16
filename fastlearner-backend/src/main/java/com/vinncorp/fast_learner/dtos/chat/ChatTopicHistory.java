package com.vinncorp.fast_learner.dtos.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatTopicHistory {
    private Long chatId;
    private String time;
    private String title;
}
