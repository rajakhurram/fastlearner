package com.vinncorp.fast_learner.models.quiz;

import com.vinncorp.fast_learner.util.Constants.Text;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_question")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;


    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Transient
    private Boolean delete;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "quizQuestion", cascade = CascadeType.REMOVE)
    private List<QuizQuestionAnwser> quizQuestionAnwsers;
}