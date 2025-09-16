package com.vinncorp.fast_learner.response.quiz;

import com.vinncorp.fast_learner.util.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAnswerResponse {
    private String questionText;
    private List<Long> correctAnswerId;
    private List<Long> selectedAnswerId;
    private boolean isCorrect;
    private String explanation;
    private QuestionType questionType;
    private List<AnswerResponse> answerResponseList;

}
