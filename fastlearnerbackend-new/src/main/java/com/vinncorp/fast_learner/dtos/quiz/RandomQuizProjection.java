package com.vinncorp.fast_learner.dtos.quiz;

import java.util.List;

public interface RandomQuizProjection {
    Long getTopicId();
    Long getQuizId();
    Long getQuestionId();
    String getQuestionText();
    String getQuestionType(); // or enum
    String getExplanation();

    String getAnswerImageUrl();
}
