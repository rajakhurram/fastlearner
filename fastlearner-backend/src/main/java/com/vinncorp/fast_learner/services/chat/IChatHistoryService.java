package com.vinncorp.fast_learner.services.chat;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.chat.Chat;
import com.vinncorp.fast_learner.response.chat.ChatHistoryResponse;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface IChatHistoryService {
    void createChatHistory(Chat chat, String question, String answer) throws InternalServerException;

    Message<List<ChatHistoryResponse>> getChatHistoryByChatId(long chatId, String name) throws EntityNotFoundException;

    Message<List<ChatHistoryResponse>> getChatHistoryByVideoId(long videoId, String name) throws EntityNotFoundException;

}
