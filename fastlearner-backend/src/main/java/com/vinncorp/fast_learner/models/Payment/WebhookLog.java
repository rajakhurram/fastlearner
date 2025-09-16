package com.vinncorp.fast_learner.models.Payment;

import com.vinncorp.fast_learner.models.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "webhook_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookLog extends Auditable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
