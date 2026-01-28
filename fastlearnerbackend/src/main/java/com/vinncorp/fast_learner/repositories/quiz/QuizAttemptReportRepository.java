package com.vinncorp.fast_learner.repositories.quiz;

import com.vinncorp.fast_learner.models.quiz.QuizAttemptReport;
import com.vinncorp.fast_learner.util.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizAttemptReportRepository extends JpaRepository<QuizAttemptReport,Long> {
    Optional<QuizAttemptReport> findByQuizAttemptId(Long quizAttemptId);

//    Optional<QuizAttemptReport> findFirstByQuizAttemptIdOrderByUpdatedAtDesc(Long quizAttemptId);

    Optional<QuizAttemptReport> findByQuizRandomId(String quizAttemptId);

    Optional<QuizAttemptReport> findFirstByQuizAttemptIdAndStatusOrderByCreatedAtDesc(
            Long quizAttemptId,
            ReportStatus status
    );
}
