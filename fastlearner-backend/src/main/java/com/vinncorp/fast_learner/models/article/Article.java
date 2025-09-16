package com.vinncorp.fast_learner.models.article;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinncorp.fast_learner.models.docs.Document;
import com.vinncorp.fast_learner.models.topic.Topic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Transient
    private Boolean delete;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "article", cascade = CascadeType.REMOVE)
    private List<Document> documents;
}
