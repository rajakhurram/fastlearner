package com.vinncorp.fast_learner.services.question_answer;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.question_answer.AnswerRequest;
import com.vinncorp.fast_learner.response.question_answer.AnswerResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IAnswerService {
    Message<String> create(AnswerRequest request, String email) throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<AnswerResponse> getAllAnswerWithPagination(Long courseId, Long questionId, int pageNo, int pageSize, String email) throws BadRequestException, EntityNotFoundException;
}
