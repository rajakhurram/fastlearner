package com.vinncorp.fast_learner.dtos.quiz;

public interface RandomQuizProjection {
    Long getTopicId();
    Long getQuizId();
    Long getQuestionId();
    String getQuestionText();
    String getQuestionType(); // or enum
    String getExplanation();
}
