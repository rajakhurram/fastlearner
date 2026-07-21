package com.vinncorp.fast_learner.models.permission;


import com.vinncorp.fast_learner.models.subscription.Subscription;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_permission")
public class SubscriptionPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
    @ManyToOne
    @JoinColumn(name = "permission_id")
    private Permission permission;
    private Boolean isActive;

}
