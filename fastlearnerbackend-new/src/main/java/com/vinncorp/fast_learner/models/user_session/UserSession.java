package com.vinncorp.fast_learner.models.user_session;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_session")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private Long userId;
    private LocalDateTime createdAt;
    private Long courseId;
    private String courseUrl;
    private Double coursePrice;
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
}
