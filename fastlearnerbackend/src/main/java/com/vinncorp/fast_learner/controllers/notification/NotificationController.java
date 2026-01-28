package com.vinncorp.fast_learner.controllers.notification;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.notification.NotificationDeleteRequest;
import com.vinncorp.fast_learner.response.notification.NotificationWithPaginationResponse;
import com.vinncorp.fast_learner.services.notification.NotificationService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;


@RestController
@RequestMapping(APIUrls.NOTIFICATION)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = APIUrls.FETCH_NOTIFICATION, consumes = MediaType.ALL_VALUE)
    public SseEmitter register(@PathVariable String timestamp, Principal principal) throws EntityNotFoundException {
       return notificationService.register(principal.getName(), timestamp);
    }

    @GetMapping(APIUrls.FETCH_ALL_NOTIFICATION_WITH_PAGINATION)
    public ResponseEntity<Message<NotificationWithPaginationResponse>> fetchAllNotifications(
            @RequestParam int pageNo, @RequestParam int pageSize, Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = notificationService.fetchAllNotificationByPagination(principal.getName(), pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_NOTIFICATION)
    public ResponseEntity<Object> deleteNotification(
            @NotNull(message = "Notification ids should not be null.")
            @RequestBody NotificationDeleteRequest notificationId,
            Principal principal) throws InternalServerException {
        notificationService.readANotification(notificationId.getNotificationId(), principal.getName());
        return ResponseEntity.ok(null);
    }
}
