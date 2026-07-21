package com.vinncorp.fast_learner.services.ai_grader;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.ai_grader.AiGraderClassRequest;
import com.vinncorp.fast_learner.request.ai_grader.ai_class.AIClassReq;
import com.vinncorp.fast_learner.request.ai_grader.ai_result.AIResultReq;
import com.vinncorp.fast_learner.response.ai_grader.AIGraderClass;
import com.vinncorp.fast_learner.response.ai_grader.ai_class.AIClassPaginatedResp;
import com.vinncorp.fast_learner.response.ai_grader.ai_class.ClassPaginatedResp;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGraderClassService implements IAiGraderClassService {

    private static final String AI_GRADER_ERROR = "ERROR: Connection not established with ai_grader api";

    private final IUserService userService;

    private final GenericRestClient restClient;


    @Override
    public Message getClassByInstructorId(Principal principal) throws InternalServerException {
        User user = this.userService.getUserByEmail(principal.getName());

        if (user == null) {
            return new Message<>(403, "User not found", "403", null);
        }

        ParameterizedTypeReference<Message<List<AIGraderClass>>> responseType =
                new ParameterizedTypeReference<>() {
                };

        Message<List<AIGraderClass>> response = restClient.makeRequestForAIGrader(
                "/api/v1/ai-grader-class/?instructorId=" + user.getId(), HttpMethod.GET, null, responseType);
        return response;
    }

    @Override
    public Message createClass(AiGraderClassRequest request, Principal principal) throws InternalServerException, EntityNotFoundException {
        User user = this.userService.findByEmail(principal.getName());

        request.setInstructorId(user.getId());

        ParameterizedTypeReference<Message<AIGraderClass>> responseType =
                new ParameterizedTypeReference<>() {
                };

        Message<AIGraderClass> response = restClient.makeRequestForAIGrader(
                "/api/v1/ai-grader-class/create", HttpMethod.POST, request, responseType);

        return response;
    }

    @Override
    public byte[] getAiGraderClassExport(AIResultReq aiResultReq,Pageable pageable, Principal principal) throws InternalServerException {

        User user = this.userService.getUserByEmail(principal.getName());
        if (user == null) {
            return null;
        }

        ParameterizedTypeReference<byte[]> responseType =
                new ParameterizedTypeReference<>() {
                };

        byte[] response = restClient.makeRequestForAIGrader(
                "/api/v1/ai-grader-class/export?&pageNo=" + pageable.getPageNumber() + "&pageSize=" + pageable.getPageSize(),
                HttpMethod.POST,
                aiResultReq,
                responseType
        );

        if (response == null) {
            throw new InternalServerException("Empty Excel export.");
        }

        // Convert to Byte[] for compatibility
        byte[] byteObjects = new byte[response.length];
        System.arraycopy(response, 0, byteObjects, 0, response.length);

        return byteObjects;
    }

    @Override
    public Message<AIClassPaginatedResp> getAllClassByInstructor(Long classId, Principal principal, Pageable pageable) throws BadRequestException {
        User user = this.userService.getUserByEmail(principal.getName());
        if (user == null) {
            return new Message<>(403, "User not found", "403", null);
        }

        try {
            log.info("Sending request to AI Grader Class for classId={}, instructorId={}, pageNo={}, pageSize={}",
                    classId,
                    user.getId(),
                    pageable.getPageNumber(),
                    pageable.getPageSize());
            AIClassReq aiClassReq = new AIClassReq();
            aiClassReq.setClassId(classId);
            aiClassReq.setInstructorId(user.getId());

            Message<AIClassPaginatedResp> response = restClient.makeRequestForAIGrader(
                    "/api/v1/ai-grader-class/all?pageNo=" + pageable.getPageNumber()
                            + "&pageSize=" + pageable.getPageSize(),
                    HttpMethod.POST,
                    aiClassReq,
                    Message.class
            );

            log.info("Received response from AI Grader CLass: status={}, code={}", response.getStatus(), response.getCode());

            // Successful response
            if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
                log.info("Successfully fetched AI Class status data.");
                return response;
            } else {
                // Unsuccessful or empty response
                log.warn("No AI Class found or unexpected response code. Returning 404.");
                return new Message<AIClassPaginatedResp>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.name())
                        .setMessage("AI Class not found.")
                        .setData(null);
            }

        } catch (Exception ex) {
            log.error("Error occurred while fetching AI Clas status data: {}", ex.getMessage(), ex);
            throw new BadRequestException("Failed to fetch AI Class", ex);
        }
    }

    @Override
    public Message deleteClass(Long classId) throws InternalServerException {
        log.info("Initiating DELETE request to AI Grader Class service for classId={}", classId);

        try {
            Message response = restClient.makeRequestForAIGrader(
                    "/api/v1/ai-grader-class/?classId=" + classId,
                    HttpMethod.DELETE,
                    null,
                    Message.class
            );

            log.info("Received response from AI Grader Class service: status={}, code={}", response.getStatus(), response.getCode());

            if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
                log.info("Class with ID {} deleted successfully from AI Grader service.", classId);
                return response;
            } else {
                log.warn("Class with ID {} not found or deletion failed. Response code: {}", classId, response.getCode());
                return new Message<>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.name())
                        .setMessage("AI Class not found.")
                        .setData(null);
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("AI Grader service returned 404 for classId={}", classId);
                return new Message<>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.name())
                        .setMessage("AI Class not found.")
                        .setData(null);
            }
            log.error("HTTP error while deleting class from AI Grader: status={}, message={}", e.getStatusCode(), e.getMessage(), e);
            throw new InternalServerException("Failed to delete AI Class: " + e.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error occurred while deleting AI Class with ID {}: {}", classId, ex.getMessage(), ex);
            throw new InternalServerException("Failed to delete AI Class", ex);
        }
    }

    @Override
    public Message updateClass(Long classId, String name) throws InternalServerException {
        log.info("Initiating UPDATE request to AI Grader Class service for classId={}, newName={}", classId, name);

        try {
            Message response = restClient.makeRequestForAIGrader(
                    "/api/v1/ai-grader-class/update?classId=" + classId + "&name=" + name,
                    HttpMethod.PUT,
                    null,
                    Message.class
            );

            log.info("Received response from AI Grader Class service for classId={}: status={}, code={}",
                    classId, response.getStatus(), response.getCode());

            if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
                log.info("Class with ID {} updated successfully in AI Grader service.", classId);
                return response;
            } else {
                log.warn("Failed to update class with ID {}. Response code: {}, message: {}",
                        classId, response.getCode(), response.getMessage());
                return new Message<>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.name())
                        .setMessage("AI Class not found or update failed.")
                        .setData(null);
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("AI Grader service returned 404 for classId={}", classId);
                return new Message<>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.name())
                        .setMessage("AI Class not found.")
                        .setData(null);
            }
            log.error("HTTP error while updating class in AI Grader service: status={}, message={}",
                    e.getStatusCode(), e.getMessage(), e);
            throw new InternalServerException("Failed to update AI Class: " + e.getMessage());
        }
    }

    @Override
    public Message<ClassPaginatedResp> getAllClassByStudentEmail(Principal principal, Pageable pageable) throws EntityNotFoundException {
        // Step 1: Fetch user by email
        User user = this.userService.getUserByEmail(principal.getName());
        if (user == null) {
            log.warn("User not found for email: {}", principal.getName());
            return new Message<>(403, "User not found", "403", null);
        }

        try {
            log.info("Preparing request to fetch AI Classes for studentEmail: '{}', instructorId: {}, pageNo: {}, pageSize: {}",
                    principal.getName(), user.getId(), pageable.getPageNumber(), pageable.getPageSize());

            // Step 3: Send request to AI Grader Class microservice
            String url = "/api/v1/ai-grader-class/student?email=" + principal.getName()
                    + "&pageNo=" + pageable.getPageNumber()
                    + "&pageSize=" + pageable.getPageSize();

            log.info("Sending POST request to: {}", url);

            @SuppressWarnings("unchecked")
            Message<ClassPaginatedResp> response = restClient.makeRequestForAIGrader(
                    url,
                    HttpMethod.POST,
                    null,
                    Message.class
            );

            log.info("Received response from AI Grader Class - status: {}, code: {}", response.getStatus(), response.getCode());

            if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
                log.info("Successfully fetched AI Class data for studentEmail: '{}'", principal.getName());
                return response;
            } else {
                throw new EntityNotFoundException("Not found");
            }

        } catch (Exception ex) {
            log.error("Exception occurred while fetching AI Classes for studentEmail: '{}': {}", principal.getName(), ex.getMessage(), ex);
            throw new EntityNotFoundException("Failed to fetch AI Class", ex);
        }
    }


}
