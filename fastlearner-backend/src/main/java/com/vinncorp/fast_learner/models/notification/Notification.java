package com.vinncorp.fast_learner.models.notification;

import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification")
public class Notification implements Serializable {

    @Transient
    private long VERSION = 4389479873973L;

    @Transient
    private Long totalUnread;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private NotificationType type;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(value = EnumType.STRING)
    private NotificationContentType contentType;

    @Column(name = "fullname")
    private String senderName;

    @Column(name = "sender_image_url")
    private String senderImageURL;

    @Column(name = "receiver_ids", columnDefinition = "bigint[]")
    private List<Long> receiverIds;

    @Column(name = "url")
    private String url;

    @Column(name = "creation_date", updatable = false)
    @CreatedDate
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "is_read")
    private boolean isRead = false;
    private Long redirectId;
}
