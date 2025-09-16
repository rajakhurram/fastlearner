package com.vinncorp.fast_learner.response.notification;


import com.vinncorp.fast_learner.models.notification.Notification;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWithPaginationResponse {

    private List<Notification> notificationList;
    private int pageNo;
    private int pageSize;
    private long noOfPages;
    private long noOfElements;
}
