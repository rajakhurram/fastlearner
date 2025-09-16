package com.vinncorp.fast_learner.services.chat;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.chat.Chat;
import com.vinncorp.fast_learner.models.chat.ChatHistory;
import com.vinncorp.fast_learner.repositories.chat.ChatHistoryRepository;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.chat.ChatHistoryResponse;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService implements IChatHistoryService{

    private final ChatHistoryRepository repo;
    private final IUserService userService;

    @Override
    public void createChatHistory(Chat chat, String question, String answer)
            throws InternalServerException {
        log.info("Creating chat history.");
        ChatHistory chatHistory = ChatHistory.builder()
                .creationDate(new Date())
                .answer(answer)
                .chat(chat)
                .question(question)
                .build();
        try {
            repo.save(chatHistory);
            log.info("Chat history created.");
        } catch (Exception e) {
            throw new InternalServerException("Chat history "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<List<ChatHistoryResponse>> getChatHistoryByChatId(long chatId, String email) throws EntityNotFoundException {
        log.info("Fetching all chat history by chat id: " + chatId);
        User user = userService.findByEmail(email);
        List<ChatHistory> chatHistory = repo.findByChat_Id(chatId, user.getId());
        if (CollectionUtils.isEmpty(chatHistory)) {
            throw new EntityNotFoundException("No chat history present for user: "+ email);
        }

        return new Message<List<ChatHistoryResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(ChatHistoryResponse.from(chatHistory))
                .setMessage("Successfully fetched chat history.");
    }

    @Override
    public Message<List<ChatHistoryResponse>> getChatHistoryByVideoId(long videoId, String email) throws EntityNotFoundException {
        log.info("Fetching all chat history by video id: " + videoId);
        User user = userService.findByEmail(email);
        List<ChatHistory> chatHistory = repo.findByVideo_Id(videoId, user.getId());
        if (CollectionUtils.isEmpty(chatHistory)) {
            throw new EntityNotFoundException("No chat history present for user: "+ email);
        }

        return new Message<List<ChatHistoryResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(ChatHistoryResponse.from(chatHistory))
                .setMessage("Successfully fetched chat history.");
    }

}
