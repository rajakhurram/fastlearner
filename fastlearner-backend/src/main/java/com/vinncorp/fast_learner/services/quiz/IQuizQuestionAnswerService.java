package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnswer;
import com.vinncorp.fast_learner.dtos.section.QuizQuestionAnswers;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import com.vinncorp.fast_learner.request.question_answer.AnswerRequest;
import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import com.vinncorp.fast_learner.response.quiz.QuizAnswerResponse;
import com.vinncorp.fast_learner.response.quiz.QuizQuestionAnswerResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IQuizQuestionAnswerService {
    QuizQuestionAnwser save(QuizQuestionAnwser build) throws InternalServerException, BadRequestException;

    QuizQuestionAnswer fetchAllQuestionAndAnswersByTopicId(Long topicId, boolean forStudent, Boolean random, Pageable pageable);

    Message<QuizQuestionAnswerResponse> validateAnswer(Long questionId, Long answerId, String email) throws EntityNotFoundException, BadRequestException;

    Message<QuizAnswerResponse> validateAnswers(List<ValidationAnswerRequest> validationAnswerRequests, String name) throws BadRequestException;
}
