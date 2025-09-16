package com.vinncorp.fast_learner.mock.chat;

import com.vinncorp.fast_learner.models.chat.Chat;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.request.chat.CreateChatRequest;

public class ChatTestData {

    public static final Chat getChat() {
        return Chat.builder()
                .id(1L)
                .video(Video.builder().id(1L).build())
                .title("Test title")
                .time("21:33")
                .build();
    }

    public static final CreateChatRequest getValidCreateChatRequest() {
        return CreateChatRequest.builder()
                .courseId(1L)
                .topicId(1L)
                .question("What is programming")
                .time("21:22")
                .build();
    }
}
