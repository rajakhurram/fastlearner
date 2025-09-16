package com.vinncorp.fast_learner.controllers.quiz;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.request.question_answer.AnswerRequest;
import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import com.vinncorp.fast_learner.response.quiz.QuizAnswerResponse;
import com.vinncorp.fast_learner.response.quiz.QuizQuestionAnswerResponse;
import com.vinncorp.fast_learner.services.quiz.IQuizQuestionAnswerService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.QUIZ)
@RequiredArgsConstructor
public class QuizController {

    private final IQuizQuestionAnswerService quizQuestionAnswerService;

    @PostMapping(APIUrls.VALIDATE_ANSWER)
    public ResponseEntity<Message<QuizQuestionAnswerResponse>> validateAnswer(@RequestParam Long questionId, @RequestParam Long answerId, Principal principal)
            throws EntityNotFoundException, BadRequestException {
        var m = quizQuestionAnswerService.validateAnswer(questionId, answerId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }



    @PostMapping(APIUrls.VALIDATE_ANSWERS)
    public ResponseEntity<Message<QuizAnswerResponse>> validateAnswering(
            @RequestBody List<ValidationAnswerRequest> validationAnswerRequests,Principal principal) throws BadRequestException {
        var m = quizQuestionAnswerService.validateAnswers(validationAnswerRequests, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }


}
