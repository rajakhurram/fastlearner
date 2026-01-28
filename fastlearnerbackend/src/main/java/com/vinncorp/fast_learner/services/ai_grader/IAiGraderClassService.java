package com.vinncorp.fast_learner.services.ai_grader;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.ai_grader.AiGraderClassRequest;
import com.vinncorp.fast_learner.request.ai_grader.ai_class.AIClassReq;
import com.vinncorp.fast_learner.request.ai_grader.ai_result.AIResultReq;
import com.vinncorp.fast_learner.response.ai_grader.ai_class.AIClassPaginatedResp;
import com.vinncorp.fast_learner.response.ai_grader.ai_class.ClassPaginatedResp;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface IAiGraderClassService {

    Message getClassByInstructorId(Principal principal) throws InternalServerException;

    Message createClass(AiGraderClassRequest request, Principal principal) throws InternalServerException, EntityNotFoundException;

    byte[] getAiGraderClassExport(AIResultReq aiResultReq,Pageable pageable, Principal principal) throws InternalServerException;

    Message<AIClassPaginatedResp> getAllClassByInstructor(Long classId, Principal principal, Pageable pageable) throws BadRequestException;

    Message deleteClass(Long classId) throws BadRequestException, InternalServerException;

    Message updateClass(Long classId, String name) throws InternalServerException;

    Message<ClassPaginatedResp> getAllClassByStudentEmail(Principal principal, Pageable pageable) throws BadRequestException, EntityNotFoundException;
}
