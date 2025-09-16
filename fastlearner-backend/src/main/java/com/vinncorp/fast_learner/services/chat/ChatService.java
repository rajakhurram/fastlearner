package com.vinncorp.fast_learner.services.chat;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.chat.Chat;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.chat.ChatRepository;
import com.vinncorp.fast_learner.request.chat.ChatAPIRequest;
import com.vinncorp.fast_learner.request.chat.CreateChatRequest;
import com.vinncorp.fast_learner.response.chat.ChatContentResponse;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.response.chat.CreateChatResponse;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static com.vinncorp.fast_learner.exception.InternalServerException.NOT_DELETE_INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements IChatService{

    @Value("${chat.api.url}")
    private String CHAT_API_URL;

    @Value("${transcript.generation.auth-key}")
    private String TRANSCRIPT_GENERATION_AUTH_KEY;

    private final RestTemplate restTemplate;
    private final ChatRepository repo;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;
    private final ITopicService topicService;
    private final IVideoService videoService;
    private final IChatHistoryService chatHistoryService;
    private final IUserService userService;

    /**
     * First we have to validate the user is allowed for this course
     * Then check whether the topic by time is already exists in the
     * If YES then add chat history in the table
     * Else we have tor create questionDetail
     * */
    @Override
    public Message<CreateChatResponse> createChat(CreateChatRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Creating chat for user: "+ email);
        if (!enrollmentService.isEnrolled(request.getCourseId(), email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }
        Topic topic = topicService.getTopicById(request.getTopicId());
        Video video = videoService.getVideoByTopicId(topic.getId());

        Message m = getAnswerFromChatAPIService(video.getTranscribe(), request.getQuestion());

        String answer = (String)((LinkedHashMap<?, ?>) m.getData()).get("answer");

        Chat chat = save(request, subscribedUser, video);

        chatHistoryService.createChatHistory(chat, request.getQuestion(), answer);

        return new Message<CreateChatResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Chat service responded successfully.")
                .setData(CreateChatResponse.builder().answer(answer).question(request.getQuestion()).build());
    }

    private Chat save(CreateChatRequest request, SubscribedUser subscribedUser, Video video) throws InternalServerException {
        Chat savedChat = repo.findByTimeAndVideo_IdAndCreatedBy(request.getTime(), video.getId(), subscribedUser.getUser().getId());
        if (Objects.isNull(savedChat)) {
            savedChat = Chat.builder()
                    .time(request.getTime())
                    .title(request.getQuestion())
                    .video(video)
                    .build();
            try {
                savedChat.setCreatedBy(subscribedUser.getUser().getId());
                savedChat.setCreationDate(new Date());
                savedChat = repo.save(savedChat);
                log.info("Chat is saved.");
            } catch (Exception e) {
                throw new InternalServerException("Chat "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
            }
        }
        return savedChat;
    }

    private Message<String> getAnswerFromChatAPIService(String transcript, String question) throws InternalServerException, BadRequestException {
        log.info("Fetching chat answer from chat api service.");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", TRANSCRIPT_GENERATION_AUTH_KEY);

        ChatAPIRequest chatAPIRequest = new ChatAPIRequest();
        chatAPIRequest.setQuestion(question);
        chatAPIRequest.setTranscript(transcript);

        HttpEntity<ChatAPIRequest> requestEntity = new HttpEntity<>(chatAPIRequest, headers);

        ResponseEntity<Message> response = null;
        try {
            response = restTemplate.exchange(
                    CHAT_API_URL, HttpMethod.POST, requestEntity, Message.class);
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            if(e.getLocalizedMessage().contains("Error code: 400"))
                throw new BadRequestException("Please write appropriate question in the video context.");
            throw new InternalServerException("Chat api service is not working.");
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        throw new InternalServerException("Chat api service is not working.");
    }

    @Override
    public Message<List<ChatContentResponse>> fetchChatContent(Long courseId, String email)
            throws BadRequestException, EntityNotFoundException {
        log.info("Fetching chat contents for a course.");
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        List<Tuple> rawData = repo.findAllByCourseId(courseId, subscribedUser.getUser().getId());
        if (CollectionUtils.isEmpty(rawData)) {
            throw new EntityNotFoundException("No chat contents are created.");
        }
        return new Message<List<ChatContentResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(ChatContentResponse.convertToChatContentResponse(rawData))
                .setMessage("Fetching all chat contents for logged in user.");
    }

    @Override
    public Message<String> deleteChatById(Long courseId, Long chatId, String email) throws InternalServerException, EntityNotFoundException, BadRequestException {

        User user = this.userService.findByEmail(email);

        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }

        Chat chat = this.repo.findById(chatId)
                .orElseThrow(() -> new BadRequestException("Chat not found"));

        if (!chat.getCreatedBy().equals(user.getId())) {
            throw new BadRequestException("You don't have rights to delete this chat");
        }
        try {
            this.repo.deleteById(chatId);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setData(null)
                    .setMessage("Deleted this chat successfully");
        } catch (Exception ex) {
            throw new InternalServerException(NOT_DELETE_INTERNAL_SERVER_ERROR);
        }
    }
}
