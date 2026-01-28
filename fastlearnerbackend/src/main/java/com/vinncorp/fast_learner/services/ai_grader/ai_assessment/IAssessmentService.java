package com.vinncorp.fast_learner.services.ai_grader.ai_assessment;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AssessmentReqForStud;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AssessmentRequest;
import com.vinncorp.fast_learner.response.ai_grader.ai_assessment.AssessmentStatusByPaginatedResp;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AIAssessmentRequest;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AIAssessmentReq;

import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface IAssessmentService {
    Message getAssessments(AIAssessmentReq request, int pageNo, int pageSize) throws BadRequestException;

    Message<AssessmentStatusByPaginatedResp> getAllByClassIdAndAssessmentId(AssessmentRequest assessmentRequest, Pageable pageable) throws InternalServerException;

    Message createAssessment(AIAssessmentRequest aiAssessmentRequest, Principal principal) throws InternalServerException;

    Message updateAIAssessment(Long aiAssessmentId, String name) throws BadRequestException;

    Message deleteAssessmentById(Long id) throws InternalServerException;

    Message getAssessmentDetails(Long classId, Long assessmentId, String email, int pageNo, int pageSize) throws BadRequestException;

//    Message getAllByClassIdAndAssessmentIdInStudent(AssessmentReqForStud assessmentRequest, Pageable pageable) throws BadRequestException;
}
