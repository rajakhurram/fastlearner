package com.vinncorp.fast_learner.models.permission;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class SubscriptionUrlPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Subscription subscription;
    @ManyToOne
    private UrlPermission urlPermission;
    @Enumerated(value = EnumType.STRING)
    private GenericStatus status;
}
