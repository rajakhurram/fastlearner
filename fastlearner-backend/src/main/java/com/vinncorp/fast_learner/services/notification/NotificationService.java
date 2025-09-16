package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.notification.Notification;
import com.vinncorp.fast_learner.repositories.notification.NotificationRepository;
import com.vinncorp.fast_learner.response.notification.NotificationWithPaginationResponse;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final long EMITTER_TIMEOUT = 9000000;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final NotificationRepository repo;
    private final IUserService userService;

    @Override
    public SseEmitter register(String email, String uniqueID) throws EntityNotFoundException {
        // Fetch previous notifications from database
        log.info("Fetching notifications for user: " + email + " with unique ID: " + uniqueID);
        User receiver = userService.findByEmail(email);
        if(!Objects.isNull(receiver)){
            SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
            emitters.put(receiver.getId() + "_" + uniqueID, emitter);

            List<Notification> fetchFromDb = fetchTop5NotificationsByUser(receiver.getId());

            try {
                emitter.send(SseEmitter.event().name("notification").data(fetchFromDb));
            } catch (IOException ex) {
                log.warn("ERROR: " + ex.getLocalizedMessage());
            }

            //emitter.onCompletion(() -> emitters.remove(receiver.getId() + "_" + uniqueID));
            emitter.onTimeout(() -> emitters.remove(receiver.getId() + "_" + uniqueID));

            return emitter;
        }
        throw new EntityNotFoundException("User not found.");
    }

    // This method can be called to send notifications
    // @Async
    @Override
    public void sendNotification(Notification message) {
        for (Long id : message.getReceiverIds()) {
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                String key = entry.getKey();
                SseEmitter emitter = entry.getValue();
                if (key.contains(id + "_") && emitter != null) {
                    try {
                        emitter.send(SseEmitter.event().name("notification").data(message));
                    } catch (Exception e) {
                        log.warn("Error sending notification to emitter: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void deleteEmitter(String email, String uniqueID) throws EntityNotFoundException {
        log.info("Removing emitter for user: " + email + " with unique ID: " + uniqueID);
        User receiver = userService.findByEmail(email);
        emitters.remove(receiver.getId() + "_" + uniqueID);
        log.info("Removed emitter for user: " + email + " with unique ID: " + uniqueID);
    }

    @Override
    public List<Notification> fetchTop5NotificationsByUser(Long receiverId) {
        log.info("Fetch top 5 notifications from database.");
        List<Notification> data = repo.findByReceiverId(receiverId);
        Long unreadNotification = repo.totalUnreadNotifications(receiverId);
        return data.stream().map(e -> {
            e.setTotalUnread(unreadNotification);
            return e;
        }).toList();
    }

    @Override
    public Message<NotificationWithPaginationResponse> fetchAllNotificationByPagination(String email, int pageNo, int pageSize)
            throws EntityNotFoundException, BadRequestException {
        log.info("Fetching all notifications with pagination...");
        if(pageNo < 0 || pageSize < 0){
            throw new BadRequestException("Page no or Page size cannot be negative.");
        }
        User receiver = userService.findByEmail(email);
        Page<Notification> data = repo.findByReceiverIdWithPagination(receiver.getId(), PageRequest.of(pageNo, pageSize));
        if (data.isEmpty()) {
            throw new EntityNotFoundException("No notification found for the logged in user.");
        }
        NotificationWithPaginationResponse response = NotificationWithPaginationResponse.builder()
                .notificationList(data.getContent())
                .pageNo(pageNo)
                .pageSize(pageSize)
                .noOfPages(data.getTotalPages())
                .noOfElements(data.getNumberOfElements())
                .build();

        return new Message<NotificationWithPaginationResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetching notification is successful.")
                .setData(response);

    }

    @Override
    public void readANotification(List<Long> notificationId, String email) throws InternalServerException {
        log.info("Marking the notification as read...");
        try {
            repo.readANotification(notificationId);
        } catch (Exception e) {
            throw new InternalServerException("A notification read functionality not performed.");
        }
    }

    @Override
    public void readAllNotificationByUser(String email) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Marking all notification as read...");
        if(Objects.isNull(email)){throw new BadRequestException("Email cannot be null");}
        User user = userService.findByEmail(email);
        try {
            repo.readAllNotificationByReceiverId(user.getId());
        } catch (Exception e) {
            throw new InternalServerException("All notification read functionality not performed.");
        }
    }
}
