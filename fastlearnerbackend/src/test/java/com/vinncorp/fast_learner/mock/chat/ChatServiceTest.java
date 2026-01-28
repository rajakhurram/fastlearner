package com.vinncorp.fast_learner.mock.chat;

import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.topic.TopicTestData;
import com.vinncorp.fast_learner.mock.video.VideoTestData;
import com.vinncorp.fast_learner.models.chat.Chat;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.repositories.chat.ChatRepository;
import com.vinncorp.fast_learner.request.chat.CreateChatRequest;
import com.vinncorp.fast_learner.response.chat.ChatContentResponse;
import com.vinncorp.fast_learner.response.chat.CreateChatResponse;
import com.vinncorp.fast_learner.services.chat.ChatHistoryService;
import com.vinncorp.fast_learner.services.chat.ChatService;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.services.topic.TopicService;
import com.vinncorp.fast_learner.services.video.VideoService;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private SubscribedUserService subscribedUserService;

    @Mock
    private ChatRepository repo;

    @Mock
    private TopicService topicService;

    @Mock
    private VideoService videoService;

    @Mock
    private ChatHistoryService chatHistoryService;

    @InjectMocks
    private ChatService chatService;

    private String email;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        email = "test@mail.com";

        ReflectionTestUtils.setField(chatService, "TRANSCRIPT_GENERATION_AUTH_KEY", "kljiogj43ioihjrjg");
        ReflectionTestUtils.setField(chatService, "CHAT_API_URL", "http://localhost:8008/api/v1");
    }

    @DisplayName("Create chat when provided valid data")
    @Test
    void testCreateChat_Success() throws Exception {
        CreateChatRequest request = ChatTestData.getValidCreateChatRequest();

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        Topic topic = TopicTestData.topicData();

        Video video = VideoTestData.video();

        Chat savedChat = new Chat();

        when(enrollmentService.isEnrolled(any(), any())).thenReturn(true);
        when(subscribedUserService.findByUser(any())).thenReturn(subscribedUser);
        when(topicService.getTopicById(any())).thenReturn(topic);
        when(videoService.getVideoByTopicId(any())).thenReturn(video);
        Message m = new Message<String>();
        Map<String, String> data = new LinkedHashMap<>();
        data.put("answer", "Here is the answer of your question.");
        m.setStatus(HttpStatus.OK.value());
        m.setData(data);
        ResponseEntity<Message> responseEntity = new ResponseEntity<>(m, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Message.class)))
                .thenReturn(responseEntity);

        when(repo.findByTimeAndVideo_IdAndCreatedBy(any(), any(), any())).thenReturn(null);
        when(repo.save(any(Chat.class))).thenReturn(savedChat);

        Message<CreateChatResponse> response = chatService.createChat(request, email);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Chat service responded successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("Here is the answer of your question.", response.getData().getAnswer());
        assertEquals("What is programming", response.getData().getQuestion());

        verify(repo).save(any(Chat.class));
        verify(chatHistoryService).createChatHistory(any(Chat.class), any(), any());
    }

    @DisplayName("Create chat when user is not enrolled")
    @Test
    void testCreateChat_NotEnrolled() throws EntityNotFoundException {
        CreateChatRequest request = ChatTestData.getValidCreateChatRequest();

        when(enrollmentService.isEnrolled(any(), any())).thenReturn(false);

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            chatService.createChat(request, email);
        });

        assertEquals("You are not enrolled in this course please enrolled in the course first.", thrown.getMessage());
        verify(subscribedUserService, never()).findByUser(any());
    }

    @DisplayName("Create chat when user has not subscribed a plan")
    @Test
    void testCreateChat_NoPlanSubscribed() throws EntityNotFoundException {
        CreateChatRequest request = ChatTestData.getValidCreateChatRequest();

        when(enrollmentService.isEnrolled(any(), any())).thenReturn(true);
        when(subscribedUserService.findByUser(any())).thenReturn(null);

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            chatService.createChat(request, email);
        });

        assertEquals("No plan is subscribed by user: " + email, thrown.getMessage());
        verify(topicService, never()).getTopicById(any());
    }

    @DisplayName("Create chat when topic not found")
    @Test
    void testCreateChat_TopicNotFound() throws EntityNotFoundException {
        CreateChatRequest request = ChatTestData.getValidCreateChatRequest();

        when(enrollmentService.isEnrolled(any(), any())).thenReturn(true);
        when(subscribedUserService.findByUser(any())).thenReturn(new SubscribedUser());
        when(topicService.getTopicById(anyLong())).thenThrow(new EntityNotFoundException("Topic not found"));

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            chatService.createChat(request, email);
        });

        assertEquals("Topic not found", thrown.getMessage());
        verify(videoService, never()).getVideoByTopicId(any());
    }

    @DisplayName("Create chat when chat API is not available")
    @Test
    void testCreateChat_ChatAPIServiceError() throws Exception {
        CreateChatRequest request = ChatTestData.getValidCreateChatRequest();

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        Topic topic = TopicTestData.topicData();

        Video video = VideoTestData.video();

        when(enrollmentService.isEnrolled(any(), any())).thenReturn(true);
        when(subscribedUserService.findByUser(any())).thenReturn(subscribedUser);
        when(topicService.getTopicById(any())).thenReturn(topic);
        when(videoService.getVideoByTopicId(any())).thenReturn(video);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Message.class)))
                .thenThrow(new RuntimeException("Chat api service is not working."));

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            chatService.createChat(request, email);
        });

        assertEquals("Chat api service is not working.", thrown.getMessage());
        verify(repo, never()).save(any(Chat.class));
    }

    @DisplayName("Fetch chat content with valid data")
    @Test
    public void testFetchChatContent_Success() throws Exception {
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        List<Tuple> rawData = new ArrayList<>(); // Populate with some mock data
        Tuple tuple = mock(Tuple.class);
        rawData.add(tuple);

        when(enrollmentService.isEnrolled(1L, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllByCourseId(1L, subscribedUser.getUser().getId())).thenReturn(rawData);

        Object obj = 1;
        when(tuple.get("section_id")).thenReturn(obj);
        when(tuple.get("section_name")).thenReturn("Section 1");
        when(tuple.get("sequence_number")).thenReturn(obj);

        when(tuple.get("topic_id")).thenReturn(obj);
        when(tuple.get("name")).thenReturn("Section 1");
        when(tuple.get("topic_seq")).thenReturn(obj);
        when(tuple.get("video_id")).thenReturn(obj);
        when(tuple.get("chat_id")).thenReturn(obj);
        when(tuple.get("times")).thenReturn("11:44");
        when(tuple.get("title")).thenReturn("Section 1");


        // Act
        Message<List<ChatContentResponse>> response = chatService.fetchChatContent(1L, email);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertNotNull(response.getData());
        assertEquals("Fetching all chat contents for logged in user.", response.getMessage());
    }

    @DisplayName("Fetch chat content with invalid user")
    @Test
    public void testFetchChatContent_UserNotEnrolled() {
        when(enrollmentService.isEnrolled(1L, email)).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            chatService.fetchChatContent(1L, email);
        });
    }

    @DisplayName("Fetch chat content with no active subscription")
    @Test
    public void testFetchChatContent_NoActiveSubscription() throws EntityNotFoundException {

        when(enrollmentService.isEnrolled(1L, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            chatService.fetchChatContent(1L, email);
        });
    }

    @DisplayName("Fetch chat content when no chat content found")
    @Test
    public void testFetchChatContent_NoChatContentsFound() throws EntityNotFoundException {
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        when(enrollmentService.isEnrolled(1L, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllByCourseId(1L, subscribedUser.getUser().getId())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            chatService.fetchChatContent(1L, email);
        });
    }
}
