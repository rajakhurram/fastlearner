package com.vinncorp.fast_learner.models.quiz;

import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_attempt")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "total_question", nullable = false)
    private Long totalQuestion;

    @Column(name = "total_correct_answer", nullable = false)
    private Long totalCorrectAnswer;

    @Column(name = "obtained_percentage", nullable = false)
    private Double obtainedPercentage;
    @Column(name = "total_attempt_count")
    private Long totalAttemptCount;


    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate attemptDate;


}
