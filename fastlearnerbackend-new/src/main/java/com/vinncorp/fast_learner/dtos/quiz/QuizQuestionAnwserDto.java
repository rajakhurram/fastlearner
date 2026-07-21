package com.vinncorp.fast_learner.dtos.quiz;

import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestionAnwserDto {
    private Long id;
    private String answer;
    private String answerImageUrl;
    private boolean isCorrectAnswer;
    private QuestionType questionType;

}
