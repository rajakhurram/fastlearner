package com.vinncorp.fast_learner.models.quiz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import com.vinncorp.fast_learner.util.enums.TestType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Transient
    private Boolean delete;

    @Column(name = "duration_in_minutes")
    private Integer durationInMinutes;

    @Column(name = "passing_criteria")
    private Double passingCriteria;
    private Integer randomQuestion;

    @Enumerated(EnumType.STRING)
    private TestType testType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "quiz", cascade = CascadeType.REMOVE)
    private List<QuizQuestion> quizQuestions;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "quiz", cascade = CascadeType.REMOVE)
    private List<QuizAttempt> quizAttempts;
    private Boolean generateAIReport;
    @Column(name = "report_prompt", nullable = true)
    private String reportPrompt;

    @Column(nullable = false)
    private Boolean isAllowedToRetake = false;
}