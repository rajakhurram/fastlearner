package com.vinncorp.fast_learner.models.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinncorp.fast_learner.models.chat.Chat;
import com.vinncorp.fast_learner.models.docs.Document;
import com.vinncorp.fast_learner.models.topic.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String videoURL;
    private String filename;
    private Date uploadedDate;
    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "transcribe", columnDefinition = "TEXT")
    private String transcribe;

    @Column(name = "vtt_content", columnDefinition = "TEXT")
    private String vttContent;

    @Transient
    private Boolean delete;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "video", cascade = CascadeType.REMOVE)
    private List<Chat> chats;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "video", cascade = CascadeType.REMOVE)
    private List<Document> documents;
}