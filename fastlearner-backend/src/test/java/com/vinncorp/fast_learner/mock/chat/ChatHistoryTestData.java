package com.vinncorp.fast_learner.mock.chat;

import com.vinncorp.fast_learner.models.chat.ChatHistory;
import com.vinncorp.fast_learner.response.chat.ChatHistoryResponse;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatHistoryTestData {

    public static ChatHistory getChatHistory() {
        return ChatHistory.builder()
                .id(1L)
                .question("Here is the question")
                .answer("Here is the answer")
                .creationDate(new Date())
                .chat(ChatTestData.getChat())
                .build();
    }

    public static ChatHistory getChatHistory1() {
        return ChatHistory.builder()
                .id(1L)
                .chat(ChatTestData.getChat())
                .answer("Here is the answer 1")
                .question("Here is the question 1")
                .creationDate(new Date())
                .build();
    }

    public static ChatHistory getChatHistory2() {
        return ChatHistory.builder()
                .id(2L)
                .chat(ChatTestData.getChat())
                .answer("Here is the answer 2")
                .question("Here is the question 2")
                .creationDate(new Date())
                .build();
    }
    public static List<ChatHistoryResponse> getListOfChatHistoryResponse() {
        ChatHistory chatHistory1 = ChatHistory.builder()
                .id(1L)
                .chat(ChatTestData.getChat())
                .answer("Here is the answer 1")
                .question("Here is the question 1")
                .creationDate(new Date())
                .build();

        ChatHistory chatHistory2 = ChatHistory.builder()
                .id(2L)
                .chat(ChatTestData.getChat())
                .answer("Here is the answer 2")
                .question("Here is the question 2")
                .creationDate(new Date())
                .build();

        return ChatHistoryResponse.from(Arrays.asList(chatHistory1, chatHistory2));
    }


}
