package com.vinncorp.fast_learner.rabbitmq;

import com.vinncorp.fast_learner.models.notification.Notification;
import com.vinncorp.fast_learner.repositories.notification.NotificationRepository;
import com.vinncorp.fast_learner.services.notification.INotificationService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Constants.NotificationConstant;
import com.vinncorp.fast_learner.util.enums.ContentType;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQProducer {

    private static final String NOTIFICATION = "NOTIFICATION";
    private static final String NOTIFICATION_KEY = "NOTIFICATION-KEY";

    private final AmqpTemplate rabbitTemplate;
    private final NotificationRepository repo;
    private final IUserService userService;
    private final INotificationService notificationService;

    //@Async
    public void sendMessage(String courseName, String redirectURL, String studentEmail, Long instructorId,
                            ContentType courseContentType, NotificationContentType contentType, NotificationType notificationType, Long redirectId) {
        log.info("Sending " + notificationType.name() + " notification.");

        Tuple data = repo.findByEmail(studentEmail);
        Long id = (Long) data.get("id");
        String userFullName = (String) data.get("full_name");
        String imageUrl = (String) data.get("profile_picture");

        Notification notification = Notification.builder()
                .content(NotificationConstant.value(userFullName, courseName, courseContentType, notificationType))
                .contentType(contentType)
                .type(notificationType)
                .senderName(userFullName)
                .senderImageURL(imageUrl)
                .receiverIds(List.of(instructorId))
                .url(redirectURL)
                .creationDate(new Date())
                .isRead(false)
                .redirectId(redirectId)
                .build();

        try {
            notification = repo.save(notification);
        } catch (Exception e) {
            log.error("NOTIFICATION SAVING ERROR: " + e.getLocalizedMessage());
        }
        rabbitTemplate.convertAndSend("test", "testing-key", notification);
        log.info("Message sent: " + notification.getContent());
    }

    //@Async
    public void sendMessageToUsers(String courseTitle, Long instructorId, String redirectURL, ContentType courseContentType, NotificationContentType contentType, NotificationType notificationType, Long redirectId) {
        log.info("Sending " + notificationType.name() + " to users.");

        Tuple data = repo.findByInstructorId(instructorId);

        Long[] listOfIds = null;
        listOfIds = (Long[]) data.get("user_ids");

        if (Objects.isNull(listOfIds))
            return;

        String instructorName = (String) data.get("name");
        String imageUrl = (String) data.get("image_url");

        Notification notification = Notification.builder()
                .content(NotificationConstant.value(instructorName, courseTitle, courseContentType, notificationType))
                .contentType(contentType)
                .type(notificationType)
                .senderName(instructorName)
                .senderImageURL(imageUrl)
                .receiverIds(Arrays.stream(listOfIds).toList())
                .url(redirectURL)
                .creationDate(new Date())
                .isRead(false)
                .redirectId(redirectId)
                .build();
        try {
            notification = repo.save(notification);
        } catch (Exception e) {
            log.error("NOTIFICATION SAVING ERROR: " + e.getLocalizedMessage());
        }
        rabbitTemplate.convertAndSend("test", "testing-key", notification);
        log.info("Message sent: " + notification.getContent());
    }

    public void sendMessageToLoggedInUser(String courseTitle, Long receiverId, Long instructorId, String redirectURL, ContentType courseContentType, NotificationContentType contentType, NotificationType notificationType, Long redirectId) {
        log.info("Sending " + notificationType.name() + " to users.");

        String instructorName = "System";
        String imageUrl = null;
        if (Objects.nonNull(instructorId)) {
            Tuple tuple = repo.findByUserId(instructorId);
            instructorName = (String) tuple.get("full_name");
            imageUrl = (String) tuple.get("profile_picture");
        }

        Notification notification = Notification.builder()
                .content(NotificationConstant.value(instructorName, courseTitle, courseContentType, notificationType))
                .contentType(contentType)
                .type(notificationType)
                .senderName(instructorName)
                .senderImageURL(imageUrl)
                .receiverIds(Collections.singletonList(receiverId))
                .url(redirectURL)
                .creationDate(new Date())
                .isRead(false)
                .redirectId(redirectId)
                .build();
        try {
            notification = repo.save(notification);
        } catch (Exception e) {
            log.error("NOTIFICATION SAVING ERROR: " + e.getLocalizedMessage());
        }
        rabbitTemplate.convertAndSend("test", "testing-key", notification);
        log.info("Message sent: " + notification.getContent());
    }
}
