package com.vinncorp.fast_learner.dtos.chat;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatTopic {
    private Long topicId;
    private Long sequence;
    private String topicName;
    private Long videoId;
    private List<ChatTopicHistory> chatTopicHistory;
}
