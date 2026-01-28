package com.vinncorp.fast_learner.rabbitmq;

import com.vinncorp.fast_learner.models.notification.Notification;
import com.vinncorp.fast_learner.services.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {
    private static final String NOTIFICATION = "NOTIFICATION";
    private final INotificationService notificationService;

    @RabbitListener(queues = {"test"})
    public void receiveMessage(Notification message) {
        notificationService.sendNotification(message);
        log.info("Received Message: "+ message.getContent());
    }
}
