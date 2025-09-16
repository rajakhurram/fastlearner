package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.util.Message;

public interface IQuizService {
    Quiz save(Quiz quiz) throws InternalServerException;

    Message<String> deleteQuizById(Long id) throws InternalServerException;
}
