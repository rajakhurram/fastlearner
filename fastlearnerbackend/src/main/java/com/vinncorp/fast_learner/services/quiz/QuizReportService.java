package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.models.quiz.QuizAttemptReport;
import com.vinncorp.fast_learner.repositories.quiz.QuizAttemptReportRepository;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizReportService implements IQuizReportService{

    private final QuizAttemptReportRepository reportRepository;

    public QuizAttemptReport createPendingReport(Long quizAttemptId,String quizRandomId) {
        log.info("quiz attempt creating");
        QuizAttemptReport report = QuizAttemptReport.builder()
                .quizAttemptId(quizAttemptId)
                .quizRandomId(quizRandomId)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        log.info("quiz attempt report successfully");
        return reportRepository.save(report);
    }

    public void updateReportReady(Long reportId, String html, String pdfPath) {
        log.info("report id: {} ",reportId);
        QuizAttemptReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        log.info("quiz attempt updating");
        report.setHtml(html);
        report.setPdfPath(pdfPath);
        report.setStatus(ReportStatus.READY);
        report.setUpdatedAt(LocalDateTime.now());
        log.info("quiz attempt report save successfully");
        reportRepository.save(report);
    }

    public void updateReportFailed(Long quizAttemptId) {
        QuizAttemptReport report = reportRepository.findByQuizAttemptId(quizAttemptId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(ReportStatus.FAILED);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    public QuizAttemptReport getReport(Long quizAttemptId) {
        return reportRepository.findByQuizAttemptId(quizAttemptId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    @Override
    public Message<QuizAttemptReport> getByQuizAttemptId(String quizAttemptId) {
        Optional<QuizAttemptReport> report=reportRepository.findByQuizRandomId(quizAttemptId);
        if (report.isPresent()){
            return new Message<QuizAttemptReport>()
                    .setData(report.get())
                    .setMessage("Quiz report fetched successfully.")
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString());

        }
        return new Message<QuizAttemptReport>()
                .setData(null)
                .setMessage("Quiz report not found.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }
}
