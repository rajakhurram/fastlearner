package com.vinncorp.fast_learner.controllers.ai_grader.ai_result;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.ai_grader.ai_result.AIResultReq;
import com.vinncorp.fast_learner.response.ai_grader.ai_result.AIResultResByPaginated;
import com.vinncorp.fast_learner.response.ai_grader.ai_result.AIResultResponse;
import com.vinncorp.fast_learner.response.ai_grader.ai_result_question.AIResultQueRespByPaginated;
import com.vinncorp.fast_learner.services.ai_grader.ai_result.IAIResultService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * AI Grader Result Controller
 * */
@RestController
@RequestMapping(APIUrls.AI_RESULT)
public class AIResultController {
    private final IAIResultService aiResultService;

    public AIResultController(IAIResultService aiResultService) {
        this.aiResultService = aiResultService;
    }
    @PostMapping(APIUrls.GET_RESULT_FILTER)
    public ResponseEntity<Message<AIResultResByPaginated>> getAiResult(
            @RequestBody @Valid AIResultReq aiResultReq,@RequestParam int pageNo,
            @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize) throws InternalServerException {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var m =aiResultService.getAIResultFilteredByClassIdAndAssessmentId(aiResultReq,pageable);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_RESULT_BY_ID)  // define this in APIUrls
    public ResponseEntity<Message> getAiResultById(@PathVariable Long id) throws InternalServerException {
        Message<AIResultResponse> response = aiResultService.getAIResultById(id);

        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @GetMapping(APIUrls.GET_AI_RESULT_QUESTIONS)
    public ResponseEntity<Message<AIResultQueRespByPaginated>> getAiResultQuestionsByAIResultId(@RequestParam("aiResultId") Long aiResultId
    , @RequestParam int pageNo, @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize) throws InternalServerException {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var m = aiResultService.getAiResultQuestionsByAIResultId(aiResultId,pageable);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
    @PutMapping(APIUrls.UPDATE_AI_RESULT_QUESTION)
    public ResponseEntity<Message> updateAIResultQuestionByAIResultQuestionId
            (@RequestParam Long aiResultQuestionId, @RequestParam Double score) throws InternalServerException {
        var m = aiResultService.updateAIResultQuestionByAIResultQuestionId(aiResultQuestionId, score);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
    @PutMapping(APIUrls.UPDATE_AI_STUDENT_RESULT)
    public ResponseEntity<Message> updateStudentEmailNameRollNumberByAiResultId

            (@RequestParam Long aiResultId,
             @RequestParam(value = "email", required = false) String email,
             @RequestParam(value = "studentName", required = false) String studentName,
             @RequestParam(value = "studentRollNumber", required = false) String studentRollNumber

            ) throws InternalServerException {
        var m = aiResultService.updateStudentEmailNameRollNumberByAiResultId(aiResultId, email,studentName,studentRollNumber);
        return ResponseEntity.status(m.getStatus()).body(m);
    }


    @DeleteMapping(APIUrls.DELETE_AI_RESULT)
    public ResponseEntity<Message> deleteAIResultByAIResultId(@RequestParam Long aiResultId) throws InternalServerException {
        var m = aiResultService.deleteAIResultByAIResultId(aiResultId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PutMapping(APIUrls.UPDATE_STATUS)
    public ResponseEntity<Message> updateStatusByAiResultId
            (@RequestParam Long aiResultId) throws InternalServerException {
        var m = aiResultService.updateStatusByAiResultId(aiResultId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
    @PostMapping(APIUrls.SEND_EMAIL)
    public ResponseEntity<Message> sendEmail
            (@RequestParam String email,@RequestParam Long aiResultId,Principal principal) throws InternalServerException, EntityNotFoundException {
        var m = aiResultService.sendEmail(email,aiResultId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.CREATE_AI_GRADER)
    @RateLimiter(name = "grader-service", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Message<String>> grader(@RequestPart(value = "quiz_files", required = false) List<MultipartFile> quizFiles,
                                                  @RequestPart(value = "answer_key_file", required = false) MultipartFile answerKeyFile,
                                                  @RequestParam(value = "evaluationCriteria", required = false) String evaluationCriteria,
                                                  @RequestParam(value = "assessmentId", required = true) Long assessmentId,
                                                  Principal principal) throws IOException, EntityNotFoundException, BadRequestException, InternalServerException {
        var m = aiResultService.startGrading(quizFiles, answerKeyFile, evaluationCriteria, assessmentId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.CREATE_AI_GRADER_LANDING_PAGE)
    public ResponseEntity<Message<String>> graderLandingPage(@RequestPart(value = "quiz_files", required = false) List<MultipartFile> quizFiles,
                                                             @RequestPart(value = "answer_key_file", required = false) MultipartFile answerKeyFile,
                                                             @RequestParam(value = "evaluationCriteria", required = false) String evaluationCriteria,
                                                             @RequestParam(value = "userSubmittedAnswerAsText", required = false) String userSubmittedAnswerAsText,
                                                             @RequestParam(value = "assessmentName", required = true) String assessmentName,
                                                             @RequestParam(value = "className", required = true) String className,
                                                             Principal principal) throws IOException, EntityNotFoundException, BadRequestException {
        var m = aiResultService.startGradingLandingPage(quizFiles, answerKeyFile, evaluationCriteria, assessmentName,className ,userSubmittedAnswerAsText, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }


    public ResponseEntity<?> rateLimitFallback(List<MultipartFile> quizFiles, MultipartFile answerKeyFile,
                                               String evaluationCriteria, Long assessmentId,
                                               Principal principal, RequestNotPermitted t) {
        var m = new Message<>();
        m.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        m.setCode(HttpStatus.TOO_MANY_REQUESTS.name());
        m.setMessage("Too many requests, please try again later.");

        return ResponseEntity.status(429).body(m);
    }

    @PostMapping(APIUrls.RETRY_GRADING)
    public ResponseEntity<Message> grader(@RequestParam(value = "resultId") Long resultId,
                                                  Principal principal) throws BadRequestException, InternalServerException {
        var m = aiResultService.retryGrading(resultId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
