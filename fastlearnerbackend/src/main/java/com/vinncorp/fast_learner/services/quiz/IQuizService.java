package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.TestType;

import java.util.Optional;

public interface IQuizService {
    Quiz save(Quiz quiz) throws InternalServerException;

    Message<String> deleteQuizById(Long id) throws InternalServerException;
    Quiz findByQuizQuestionsId(Long questionId);
    Optional<TestType> findQuizTestTypeByTopicId(Long topicId);
}
