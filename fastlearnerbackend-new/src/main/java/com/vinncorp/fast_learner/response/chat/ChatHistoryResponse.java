package com.vinncorp.fast_learner.response.chat;

import com.vinncorp.fast_learner.models.chat.ChatHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryResponse {

    private String question;
    private String answer;

    public static List<ChatHistoryResponse> from(List<ChatHistory> chatHistory) {
        return chatHistory.stream()
                .map(e -> ChatHistoryResponse.builder()
                        .answer(e.getAnswer())
                        .question(e.getQuestion())
                        .build())
                .toList();
    }
}
