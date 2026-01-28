package com.vinncorp.fast_learner.dtos.quiz;

import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestionDto {
    private String questionText;

    private String explanation;

    private List<QuizQuestionAnwserDto> quizQuestionAnwsers;
    private List<Long> selectedAnswerIds;
    private String answerText;
    private Boolean isCorrect;
}
