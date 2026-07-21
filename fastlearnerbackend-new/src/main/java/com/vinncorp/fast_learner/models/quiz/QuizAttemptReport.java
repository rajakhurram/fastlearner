package com.vinncorp.fast_learner.models.quiz;

import com.vinncorp.fast_learner.util.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempt_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quizAttemptId;

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // PENDING, READY, FAILED

    @Column(columnDefinition = "TEXT")
    private String html;

    private String pdfPath;

    private String quizRandomId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}