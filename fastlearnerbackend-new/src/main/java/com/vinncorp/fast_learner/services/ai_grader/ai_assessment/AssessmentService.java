package com.vinncorp.fast_learner.services.ai_grader.ai_assessment;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AIAssessmentReq;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AssessmentReqForStud;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AssessmentRequest;
import com.vinncorp.fast_learner.request.ai_grader.ai_assessment.AIAssessmentRequest;
import com.vinncorp.fast_learner.response.ai_grader.ai_assessment.AIAssessmentPaginatedResp;
import com.vinncorp.fast_learner.response.ai_grader.ai_assessment.AIAssessmentResponse;
import com.vinncorp.fast_learner.response.ai_grader.ai_assessment.AssessmentStatusByPaginatedResp;
import com.vinncorp.fast_learner.response.ai_grader.ai_assessment.AssessmentWithClassInfoPaginatedResponse;
import com.vinncorp.fast_learner.response.ai_grader.ai_result_question.AIResultQuestionResponse;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService implements IAssessmentService {

    private final GenericRestClient restClient;
    private final IUserService userService;

    @Override
    public Message<AIAssessmentPaginatedResp> getAssessments(AIAssessmentReq request, int pageNo, int pageSize)
            throws BadRequestException {

        log.info("Starting getAssessments with pageNo={} and pageSize={}", pageNo, pageSize);

        try {
            // Making a GET request to the AI Grader service
            Message<AIAssessmentPaginatedResp> response = restClient.makeRequestForAIGrader(
                    "/api/v1/ai-assessment/?pageNo=" + pageNo + "&pageSize=" + pageSize,
                    HttpMethod.POST,
                    request,
                    Message.class
            );

            log.info("Received response: status={}, code={}", response.getStatus(), response.getCode());

            // Successful response
            if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
                log.info("AI assessments fetched successfully.");
                return response;
            } else {
                // Unsuccessful response – return 404
                log.warn("AI assessments not found or failed to fetch. Returning 404 response.");
                return new Message<AIAssessmentPaginatedResp>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.name())
                        .setMessage("AI assessments not found.")
                        .setData(null);
            }

        } catch (Exception ex) {
            log.error("Exception occurred while fetching AI assessments: {}", ex.getMessage(), ex);
            throw new BadRequestException("Failed to fetch AI assessments", ex);
        }
    }

    @Override
    public Message getAllByClassIdAndAssessmentId(AssessmentRequest assessmentRequest, Pageable pageable)
            throws InternalServerException {
        log.info("Sending request to AI Grader for classId={}, assessmentId={}, pageNo={}, pageSize={}",
                assessmentRequest.getClassId(),
                assessmentRequest.getAssessmentId(),
                pageable.getPageNumber(),
                pageable.getPageSize());

        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/ai-assessment/class-id-and-assessment-id?pageNo=" + pageable.getPageNumber()
                        + "&pageSize=" + pageable.getPageSize(),
                HttpMethod.POST,
                assessmentRequest,
                Message.class
        );

        log.info("Received response from AI Grader: status={}, code={}", response.getStatus(), response.getCode());
        return response;
    }

    @Override
    public Message createAssessment(AIAssessmentRequest aiAssessmentRequest, Principal principal) throws InternalServerException {
        Message<AIAssessmentResponse> response = restClient.makeRequestForAIGrader(
                "/api/v1/ai-assessment/create", HttpMethod.POST, aiAssessmentRequest, Message.class);
        return response;
    }

    @Override
    public Message updateAIAssessment(Long aiAssessmentId, String name) throws BadRequestException {
        log.info("Initiating UPDATE request to AI Grader for aiAssessmentId={}, newName={}", aiAssessmentId, name);

        try {
            Message<AssessmentStatusByPaginatedResp> response = restClient.makeRequestForAIGrader(
                    "/api/v1/ai-assessment/?aiAssessmentId=" + aiAssessmentId + "&name=" + name,
                    HttpMethod.PUT,
                    null,
                    Message.class
            );

            if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
                log.info("AI Assessment with ID {} updated successfully.", aiAssessmentId);
                return response;
            } else {
                log.warn("Failed to update AI Assessment with ID {}. Response code: {}, message: {}",
                        aiAssessmentId, response.getCode(), response.getMessage());
                return new Message<AssessmentStatusByPaginatedResp>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.name())
                        .setMessage("AI assessment not found.")
                        .setData(null);
            }

        } catch (Exception ex) {
            log.error("Exception occurred while updating AI Assessment with ID {}: {}", aiAssessmentId, ex.getMessage(), ex);
            throw new BadRequestException("Failed to update AI assessment", ex);
        }
    }

    @Override
    public Message deleteAssessmentById(Long id) throws InternalServerException {
        log.info("Initiating DELETE request to AI Grader for aiAssessmentId={}", id);

        Message<AssessmentStatusByPaginatedResp> response = restClient.makeRequestForAIGrader(
                "/api/v1/ai-assessment/?id=" + id,
                HttpMethod.DELETE,
                null,
                Message.class
        );

        log.info("Received response from AI Grader for aiAssessmentId={}: status={}, code={}",
                id, response.getStatus(), response.getCode());

        return response;
    }

    @Override
    public Message getAssessmentDetails(Long classId, Long assessmentId, String email, int pageNo, int pageSize) throws BadRequestException {
        log.info("Starting getAssessmentDetails with classId={}, assessmentId={}, pageNo={} and pageSize={}", classId, assessmentId, pageNo, pageSize);

        User user = userService.getUserByEmail(email);
        try {
            StringBuilder params = new StringBuilder();
            boolean firstParam = true;

            if (classId != null) {
                params.append(firstParam ? "?" : "&").append("classId=").append(classId);
                firstParam = false;
            }

            if (assessmentId != null) {
                params.append(firstParam ? "?" : "&").append("assessmentId=").append(assessmentId);
                firstParam = false;
            }

            // instructorId is always present
            params.append(firstParam ? "?" : "&").append("instructorId=").append(user.getId());

            Message<AssessmentWithClassInfoPaginatedResponse> response = restClient.makeRequestForAIGrader(
                    "/api/v1/ai-assessment/assessment-details" + params + "&pageNo=" + pageNo + "&pageSize=" + pageSize,
                    HttpMethod.GET,
                    null,
                    Message.class
            );
            return response;
        } catch (Exception ex) {
            log.error("Exception occurred while fetching AI assessment details: {}", ex.getMessage(), ex);
            throw new BadRequestException("Failed to fetch AI assessment details", ex);
        }
    }

}
