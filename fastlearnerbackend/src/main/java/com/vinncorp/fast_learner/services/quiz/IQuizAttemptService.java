package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.dtos.payout.PaidUser;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.response.quiz.QuizAttemptResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IQuizAttemptService {
    Message<QuizAttemptResponse> quizAttempt(Long quizId, String name) throws InternalServerException;
}
