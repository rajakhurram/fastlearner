package com.vinncorp.fast_learner.response.quiz;

import com.vinncorp.fast_learner.dtos.user.UserDto;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptResponse {


    private Long totalQuestion;


    private Long totalCorrectAnswer;

    private Double obtainedPercentage;
    private Long totalAttemptCount;

    private QuizResponse quiz;

    private LocalDate attemptDate;
    private Boolean isAllowedToRetake;
    private String html;
    private Long attemptedQuestionCount;

}
