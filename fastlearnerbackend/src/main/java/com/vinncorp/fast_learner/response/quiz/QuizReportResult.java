package com.vinncorp.fast_learner.response.quiz;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizReportResult {
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer score;
    private String confidenceLevel;
    private List<String> weaknesses;
    private List<String> recommendations;
    private List<String> practiceSuggestions;
    private String finalThoughts;
    private String motivation;
    private String feedback;
    private String htmlReport;
}
