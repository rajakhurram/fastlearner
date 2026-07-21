package com.vinncorp.fast_learner.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreviewReport {
    private String reportPrompt;
    List<QuizQuestionDto> quizQuestionDtos;
    private String topicTitle;
    private String timeZone;
    private Integer durationInMinutes;
}
