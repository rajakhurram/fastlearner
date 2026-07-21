package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.notification.Notification;
import com.vinncorp.fast_learner.response.notification.NotificationWithPaginationResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface INotificationService {

    SseEmitter register(String email, String uniqueId) throws EntityNotFoundException;

    // This method can be called to send notifications
    void sendNotification(Notification message);

    void deleteEmitter(String email, String uniqueID) throws EntityNotFoundException;

    List<Notification> fetchTop5NotificationsByUser(Long receiverId);

    Message<NotificationWithPaginationResponse> fetchAllNotificationByPagination(String email, int pageNo, int pageSize)
            throws EntityNotFoundException, BadRequestException;

    void readANotification(List<Long> notificationId, String email) throws InternalServerException;

    void readAllNotificationByUser(String name) throws EntityNotFoundException, InternalServerException, BadRequestException;
}
