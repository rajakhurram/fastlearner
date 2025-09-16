package com.vinncorp.fast_learner.models.quiz;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_question_anwser")
public class QuizQuestionAnwser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_question_id")
    private QuizQuestion quizQuestion;

    private String answer;
    private boolean isCorrectAnswer;

    @Transient
    private Boolean delete;
}