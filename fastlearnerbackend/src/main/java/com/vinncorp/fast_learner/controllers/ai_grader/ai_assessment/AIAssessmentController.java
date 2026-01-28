package com.vinncorp.fast_learner.controllers.ai_grader.ai_assessment;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AIAssessmentReq;
//import com.vinncorp.fast_learner.response.ai_assessment.AIAssessmentResponse;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AssessmentReqForStud;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AssessmentRequest;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AIAssessmentRequest;
import com.vinncorp.fast_learner.response.ai_grader.ai_assessment.*;
import com.vinncorp.fast_learner.services.ai_grader.ai_assessment.IAssessmentService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.ASSESSMENT)
public class AIAssessmentController {

    private final IAssessmentService aiAssessmentService;

    public AIAssessmentController(IAssessmentService aiAssessmentService) {
        this.aiAssessmentService = aiAssessmentService;
    }

    @PostMapping(APIUrls.GET_ASSESSMENT)
    public ResponseEntity<Message<AIAssessmentPaginatedResp>> getAssessments
            (@RequestBody AIAssessmentReq request, @RequestParam int pageNo, @RequestParam(defaultValue = "0")
            @Min(value = 1, message = "Page size must be greater than zero") int pageSize) throws BadRequestException {
        var m =aiAssessmentService.getAssessments(request, pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.ASSESSMENT_DETAILS)
    public ResponseEntity<Message> getAssessmentDetails
            (@RequestParam(required = false) Long classId, @RequestParam(required = false) Long assessmentId,
                    @RequestParam int pageNo, @RequestParam(defaultValue = "0")
            @Min(value = 1, message = "Page size must be greater than zero") int pageSize,
             Principal principal) throws BadRequestException {
        var m =aiAssessmentService.getAssessmentDetails(classId, assessmentId, principal.getName(), pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.CREATE_ASSESSMENT)
    public ResponseEntity<Message<AIAssessmentResponse>> createAssessment(
            @RequestBody AIAssessmentRequest aiAssessmentRequest,
            Principal principal) throws InternalServerException, EntityNotFoundException {
        var m = aiAssessmentService.createAssessment(aiAssessmentRequest, principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.GET_BY_CLASS_ID_AND_ASSESSMENT_ID)
    public ResponseEntity<Message<AssessmentStatusByPaginatedResp>> getAllByClassIdAndAssessmentId
            (@RequestBody @Valid AssessmentRequest assessmentRequest, @RequestParam int pageNo,
             @RequestParam(defaultValue = "0")@Min(value = 1, message = "Page size must be greater than zero") int pageSize,
             Principal principal) throws InternalServerException{
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var m = aiAssessmentService.getAllByClassIdAndAssessmentId(assessmentRequest,pageable);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

//    @PostMapping(APIUrls.GET_BY_CLASS_ID_AND_ASSESSMENT_ID_STUDENT)
//    public ResponseEntity<Message<AssessmentStudByPaginatedResp>> getAllByClassIdAndAssessmentIdInStudent
//            (@RequestBody @Valid AssessmentReqForStud assessmentRequest, @RequestParam int pageNo,
//             @RequestParam(defaultValue = "0")@Min(value = 1, message = "Page size must be greater than zero") int pageSize) throws BadRequestException {
//        Pageable pageable = PageRequest.of(pageNo, pageSize);
//        var m = aiAssessmentService.getAllByClassIdAndAssessmentIdInStudent(assessmentRequest,pageable);
//        return ResponseEntity.status(m.getStatus()).body(m);
//    }

    @PutMapping(APIUrls.UPDATE_ASSESSMENT)
    public ResponseEntity<Message> updateAIAssessment(@RequestParam Long aiAssessmentId,@RequestParam String name) throws BadRequestException {
        var m = aiAssessmentService.updateAIAssessment(aiAssessmentId, name);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_ASSESSMENT)
    public ResponseEntity<Message> deleteAIAssessment(@RequestParam Long id) throws InternalServerException {
        var m = aiAssessmentService.deleteAssessmentById(id);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
