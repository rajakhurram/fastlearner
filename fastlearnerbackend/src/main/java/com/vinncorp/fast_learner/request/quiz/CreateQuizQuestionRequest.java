package com.vinncorp.fast_learner.request.quiz;

import com.vinncorp.fast_learner.util.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateQuizQuestionRequest {
    private Long id;
    private Boolean delete;
    private String questionText;
    private String explanation;
    private QuestionType questionType;
    private List<CreateQuizQuestionAnswerRequest> answers;
}
