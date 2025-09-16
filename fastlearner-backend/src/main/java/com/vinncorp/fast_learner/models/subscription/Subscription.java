package com.vinncorp.fast_learner.models.subscription;

import com.vinncorp.fast_learner.util.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private double price;
    private int duration;
    private String durationInWord;
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type")
    private PlanType planType;
    private boolean isActive = true;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String contentHeading;
}