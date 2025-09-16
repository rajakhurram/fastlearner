package com.vinncorp.fast_learner.models.subscription;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_log")
public class SubscriptionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String customerProfileId;
    private String paymentProfileId;

    private String currentAuthSubscriptionId;
    private String prevAuthSubscriptionId;

    private Long currentSubscriptionId;
    private Long prevSubscriptionId;

    private Long userId;

    private Date createdAt;
}
