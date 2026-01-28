package com.vinncorp.fast_learner.controllers.quiz;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.quiz.QuizAttemptReport;
import com.vinncorp.fast_learner.response.quiz.QuizQuestionAnswerResponse;
import com.vinncorp.fast_learner.services.quiz.IQuizReportService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.QUIZ_REPORT)
@RequiredArgsConstructor
public class QuizReportController {

    private final IQuizReportService iQuizReportService;

    @PostMapping(APIUrls.GET_BY_QUIZ_ATTEMPT_ID)
    public ResponseEntity<Message<QuizAttemptReport>> fetchByQuizAttemptId(@RequestParam String quizAttemptId)
    {
        var m = iQuizReportService.getByQuizAttemptId(quizAttemptId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
