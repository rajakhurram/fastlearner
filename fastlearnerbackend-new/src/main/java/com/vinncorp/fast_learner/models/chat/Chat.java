package com.vinncorp.fast_learner.models.chat;

import com.vinncorp.fast_learner.models.audit.Auditable;
import com.vinncorp.fast_learner.models.video.Video;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat")
public class Chat extends Auditable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String time;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "chat", cascade = CascadeType.REMOVE)
    private List<ChatHistory> chatHistories;
}
