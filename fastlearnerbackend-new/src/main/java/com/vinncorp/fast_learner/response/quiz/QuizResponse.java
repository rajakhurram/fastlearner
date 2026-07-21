package com.vinncorp.fast_learner.response.quiz;

import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionDto;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.util.enums.TestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizResponse {
    private Long id;

    private String title;

//    @ManyToOne
//    @JoinColumn(name = "topic_id")
//    private Topic topic;

    private Boolean delete;

    private Integer durationInMinutes;

    private Double passingCriteria;
    private Integer randomQuestion;

    private TestType testType;

    private List<QuizQuestionDto> quizQuestions;
    private List<QuizAttempt> quizAttempts;
    private Boolean generateAIReport;

    private String reportPrompt;
}
