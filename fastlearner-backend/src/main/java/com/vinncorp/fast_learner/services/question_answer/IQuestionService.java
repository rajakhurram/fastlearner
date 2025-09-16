package com.vinncorp.fast_learner.services.question_answer;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.question_answer.Question;
import com.vinncorp.fast_learner.request.question_answer.QuestionRequest;
import com.vinncorp.fast_learner.dtos.question_answer.QuestionDetail;
import com.vinncorp.fast_learner.response.question_answer.QuestionResponse;
import com.vinncorp.fast_learner.util.Message;

import java.util.Optional;

public interface IQuestionService {
    Optional<Question> findById(Long questionId);

    Message<QuestionDetail> create(QuestionRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<QuestionResponse> findAllQuestionsWithPagination(Long courseId, int pageNo, int pageSize, String name) throws BadRequestException, EntityNotFoundException;
}
