package com.vinncorp.fast_learner.request.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NotificationDeleteRequest {
    List<Long> notificationId;
}
