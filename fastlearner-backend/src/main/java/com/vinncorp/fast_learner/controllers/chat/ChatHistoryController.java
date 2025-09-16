package com.vinncorp.fast_learner.controllers.chat;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.chat.ChatHistoryResponse;
import com.vinncorp.fast_learner.services.chat.IChatHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.CHAT_HISTORY)
@RequiredArgsConstructor
public class ChatHistoryController {

    private final IChatHistoryService service;

    @GetMapping(APIUrls.GET_CHAT_HISTORY_BY_CHAT)
    public ResponseEntity<Message<List<ChatHistoryResponse>>> getAllChatHistoryByChatId(
            @PathVariable long chatId, Principal principal)
            throws EntityNotFoundException {
        Message<List<ChatHistoryResponse>> m = service.getChatHistoryByChatId(chatId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_CHAT_HISTORY_BY_VIDEO)
    public ResponseEntity<Message<List<ChatHistoryResponse>>> getAllChatHistoryByVideoId(
            @PathVariable long videoId, Principal principal)
            throws EntityNotFoundException {
        Message<List<ChatHistoryResponse>> m = service.getChatHistoryByVideoId(videoId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
