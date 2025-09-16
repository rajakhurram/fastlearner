package com.vinncorp.fast_learner.models.subscription;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscription_validations")
public class SubscriptionValidations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "validation_name")
    private String validationName;

    @Column(name = "value")
    private Long value;

    @Column(name = "is_active")
    private Boolean isActive;


}
