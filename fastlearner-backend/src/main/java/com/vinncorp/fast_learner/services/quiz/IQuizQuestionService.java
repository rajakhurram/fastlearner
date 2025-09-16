package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;

public interface IQuizQuestionService {
    QuizQuestion save(QuizQuestion quizQuestion) throws InternalServerException;
}
