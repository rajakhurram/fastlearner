package com.vinncorp.fast_learner.controllers.question_answer;

import com.vinncorp.fast_learner.dtos.question_answer.QuestionDetail;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.question_answer.QuestionRequest;
import com.vinncorp.fast_learner.response.question_answer.QuestionResponse;
import com.vinncorp.fast_learner.services.question_answer.IQuestionService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.QUESTION)
@RequiredArgsConstructor
public class QuestionController {

    private final IQuestionService service;

    @PostMapping(APIUrls.CREATE_QUESTION)
    public ResponseEntity<Message<QuestionDetail>> create(@Valid @RequestBody QuestionRequest request, Principal principal)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        Message<QuestionDetail> m = service.create(request, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_QUESTIONS)
    public ResponseEntity<Message<QuestionResponse>> fetchAllQAWithPagination(
            @RequestParam Long courseId,
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            Principal principal
    ) throws BadRequestException, EntityNotFoundException {
        Message<QuestionResponse> m =  service.findAllQuestionsWithPagination(courseId, pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
