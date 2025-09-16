package com.vinncorp.fast_learner.services.chat;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.chat.ChatContentResponse;
import com.vinncorp.fast_learner.request.chat.CreateChatRequest;
import com.vinncorp.fast_learner.response.chat.CreateChatResponse;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface IChatService {
    Message<CreateChatResponse> createChat(CreateChatRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<List<ChatContentResponse>> fetchChatContent(Long courseId, String email)
            throws BadRequestException, EntityNotFoundException;

    Message<String> deleteChatById(Long courseId, Long chatId, String email) throws InternalServerException, EntityNotFoundException, BadRequestException;
}
