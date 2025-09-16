package com.vinncorp.fast_learner.controllers.chat;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.chat.CreateChatRequest;
import com.vinncorp.fast_learner.response.chat.ChatContentResponse;
import com.vinncorp.fast_learner.response.chat.CreateChatResponse;
import com.vinncorp.fast_learner.services.chat.IChatService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.CHAT)
@RequiredArgsConstructor
public class ChatController {

    private final IChatService service;

    @GetMapping(APIUrls.GET_CHAT_CONTENTS)
    public ResponseEntity<Message<List<ChatContentResponse>>> fetchChatContent(
            @RequestParam Long courseId, Principal principal
    ) throws BadRequestException, EntityNotFoundException {
        Message<List<ChatContentResponse>> m = service.fetchChatContent(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.SEND_QUESTION)
    public ResponseEntity<Message<CreateChatResponse>> createChat(
            @Valid @RequestBody CreateChatRequest request, Principal principal)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        Message<CreateChatResponse> m = service.createChat(request, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_CHAT)
    public ResponseEntity<Message<String>> deleteChatById(@RequestParam("courseId") Long courseId, @RequestParam("chatId") Long chatId, Principal principal) throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = this.service.deleteChatById(courseId, chatId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
