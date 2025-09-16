package com.vinncorp.fast_learner.controllers.question_answer;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.question_answer.AnswerRequest;
import com.vinncorp.fast_learner.response.question_answer.AnswerResponse;
import com.vinncorp.fast_learner.services.question_answer.IAnswerService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.ANSWER)
@RequiredArgsConstructor
public class AnswerController {

    private final IAnswerService service;

    @PostMapping(APIUrls.CREATE_ANSWER)
    public ResponseEntity<Message<String>> create(@Valid @RequestBody AnswerRequest request, Principal principal)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        Message<String> m = service.create(request, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_ANSWERS)
    public ResponseEntity<Message<AnswerResponse>> getAllAnswerWithPagination(
            @RequestParam Long courseId, @RequestParam Long questionId,
            @RequestParam int pageNo, @RequestParam int pageSize,
            Principal principal
            ) throws BadRequestException, EntityNotFoundException {
        Message<AnswerResponse> m = service.getAllAnswerWithPagination(courseId, questionId, pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
