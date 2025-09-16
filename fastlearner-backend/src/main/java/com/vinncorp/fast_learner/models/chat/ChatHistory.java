package com.vinncorp.fast_learner.models.chat;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_history")
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "question", columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Column(name = "created_date", updatable = false)
    @Temporal(TIMESTAMP)
    protected Date creationDate;
}
