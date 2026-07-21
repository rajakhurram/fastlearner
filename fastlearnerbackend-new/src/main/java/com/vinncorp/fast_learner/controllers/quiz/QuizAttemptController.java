package com.vinncorp.fast_learner.controllers.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.response.quiz.QuizAttemptResponse;
import com.vinncorp.fast_learner.response.quiz.QuizQuestionAnswerResponse;
import com.vinncorp.fast_learner.services.quiz.IQuizAttemptService;
import com.vinncorp.fast_learner.services.quiz.QuizAttemptService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.QUIZ_ATTEMPT)
@RequiredArgsConstructor
public class QuizAttemptController {

    @Autowired
    private IQuizAttemptService quizAttemptService;

    @GetMapping("/{quizId}")
    public ResponseEntity<Message<QuizAttemptResponse>> quizAttempt(@PathVariable Long quizId, Principal principal) throws InternalServerException {
        var m = quizAttemptService.quizAttempt(quizId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);

    }

    @GetMapping("/instructor/{quizId}")
    public ResponseEntity<Message<QuizAttemptResponse>> quizInsAttempt(@PathVariable Long quizId, @RequestParam String email) throws InternalServerException {
        var m = quizAttemptService.quizAttempt(quizId,email);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
