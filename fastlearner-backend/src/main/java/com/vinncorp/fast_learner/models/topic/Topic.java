package com.vinncorp.fast_learner.models.topic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinncorp.fast_learner.models.article.Article;
import com.vinncorp.fast_learner.models.question_answer.Question;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.List;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topic")
public class Topic{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "topic_type_id")
    private TopicType topicType;

    @Column(name = "duration_in_sec", columnDefinition = "INT default 0")
    private int durationInSec;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    private int sequenceNumber;

    @Column(name = "created_date", updatable = false)
    @Temporal(TIMESTAMP)
    @CreatedDate
    protected Date creationDate;

    @Column(name = "last_mod_date")
    @LastModifiedDate
    @Temporal(TIMESTAMP)
    protected Date lastModifiedDate;

    @Transient
    private Boolean delete;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.REMOVE)
    private List<Quiz> quizs;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.REMOVE)
    private List<Video> videos;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.REMOVE)
    private List<Article> articles;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.REMOVE)
    private List<TopicNotes> topicNotes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.REMOVE)
    private List<UserCourseProgress> userCourseProgresses;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.REMOVE)
    private List<Question> questions;
}