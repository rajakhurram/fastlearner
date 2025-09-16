package com.vinncorp.fast_learner.mock.chat;

import com.vinncorp.fast_learner.models.chat.Chat;
import com.vinncorp.fast_learner.models.chat.ChatHistory;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.chat.ChatHistoryRepository;
import com.vinncorp.fast_learner.services.chat.ChatHistoryService;
import com.vinncorp.fast_learner.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChatHistoryServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ChatHistoryRepository repo;

    @InjectMocks
    private ChatHistoryService service;

    String question;
    String answer;
    String email;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        question = "What is AI?";
        answer = "AI stands for Artificial Intelligence.";
        email = "user@example.com";
    }

    @Test
    @DisplayName("Create chat history with valid data")
    public void testCreateChatHistory_Success() throws InternalServerException {
        Chat chat = ChatTestData.getChat();
        ChatHistory chatHistory = ChatHistoryTestData.getChatHistory();

        when(repo.save(any(ChatHistory.class))).thenReturn(chatHistory);

        service.createChatHistory(chat, question, answer);

        verify(repo, times(1)).save(any(ChatHistory.class));
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("Create chat history when db issue occurred")
    public void testCreateChatHistory_InternalServerException() {
        Chat chat = ChatTestData.getChat();

        when(repo.save(any(ChatHistory.class))).thenThrow(new RuntimeException("Database error"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.createChatHistory(chat, question, answer);
        });

        assertEquals("Chat history "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
        verify(repo, times(1)).save(any(ChatHistory.class));
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("Successfully retrieve chat history by chatId")
    public void testGetChatHistoryByChatId_Success() throws EntityNotFoundException {
        User user = new User();
        List<ChatHistory> chatHistoryList = Arrays.asList(
                ChatHistoryTestData.getChatHistory1(),
                ChatHistoryTestData.getChatHistory2()
        );

        // Mock the dependencies
        when(userService.findByEmail(email)).thenReturn(user);
        when(repo.findByChat_Id(1L, user.getId())).thenReturn(chatHistoryList);
        //when(ChatHistoryResponse.from(chatHistoryList)).thenReturn(ChatHistoryTestData.getListOfChatHistoryResponse());

        var response = service.getChatHistoryByChatId(1L, email);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully fetched chat history.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
    }

    @Test
    @DisplayName("Throw EntityNotFoundException when no chat history is found")
    public void testGetChatHistoryByChatId_NoChatHistory() throws EntityNotFoundException {
        User user = new User();

        when(userService.findByEmail(email)).thenReturn(user);
        when(repo.findByChat_Id(1L, user.getId())).thenReturn(Collections.emptyList());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.getChatHistoryByChatId(1L, email);
        });

        assertEquals("No chat history present for user: " + email, exception.getMessage());
    }

    @Test
    @DisplayName("Throw EntityNotFoundException when user is not found")
    public void testGetChatHistoryByChatId_UserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(email)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.getChatHistoryByChatId(1L, email);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Throw EntityNotFoundException when repository returns null")
    public void testGetChatHistoryByChatId_NullResponse() throws EntityNotFoundException {
        User user = new User();

        when(userService.findByEmail(email)).thenReturn(user);
        when(repo.findByChat_Id(1L, user.getId())).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.getChatHistoryByChatId(1L, email);
        });

        assertEquals("No chat history present for user: " + email, exception.getMessage());
    }
}
