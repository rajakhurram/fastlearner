package com.vinncorp.fast_learner.services.ai_grader.ai_result;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.request.ai_grader.ai_result.AIResultReq;
import com.vinncorp.fast_learner.response.ai_grader.ai_result.AIResultResByPaginated;
import com.vinncorp.fast_learner.response.ai_grader.ai_result.AIResultResponse;
import com.vinncorp.fast_learner.response.ai_grader.ai_result_question.AIResultQueRespByPaginated;
import com.vinncorp.fast_learner.response.ai_grader.ai_result_question.AIResultQuestionResponse;
import com.vinncorp.fast_learner.services.excel.ExcelExportService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionValidationsService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.InMemoryMultipartFile;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.MultipartInputStreamFileResource;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import com.vinncorp.fast_learner.util.enums.SubscriptionValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIResultService implements IAIResultService {

    private final GenericRestClient restClient;
    private final ISubscribedUserService subscribedUserService;
    private final ISubscriptionValidationsService subscriptionValidationsService;
    private final IUserService userService;
    private final RabbitMQProducer rabbitMQProducer;
    private final ExcelExportService excelExportService;

    @Override
    public Message<AIResultResByPaginated> getAIResultFilteredByClassIdAndAssessmentId(AIResultReq aiResultReq,
                                                                                       Pageable pageable) throws InternalServerException {
        Message<AIResultResByPaginated> response = restClient.makeRequestForAIGrader(
                "/api/v1/result/?pageNo="+pageable.getPageNumber()+"&pageSize="+pageable.getPageSize(),
                HttpMethod.POST, aiResultReq, Message.class);
        if (response.getStatus() == 200 && response.getCode().equalsIgnoreCase("OK")) {
            return response;
        }
        return new Message<AIResultResByPaginated>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }

    @Override
    public Message<AIResultQueRespByPaginated> getAiResultQuestionsByAIResultId(Long aiResultId, Pageable pageable) throws InternalServerException {
        ParameterizedTypeReference<Message<AIResultQueRespByPaginated>> responseType = new ParameterizedTypeReference<>() {};

        Message<AIResultQueRespByPaginated> response = restClient.makeRequestForAIGrader(
                "/api/v1/result-question/?aiResultId=" + aiResultId+"&pageNo="+pageable.getPageNumber()+"&pageSize="+pageable.getPageSize(), HttpMethod.GET, null, responseType);
        if (response.getStatus() == 200 && response.getCode().equalsIgnoreCase("OK")) {
            return response;
        }
        return new Message<AIResultQueRespByPaginated>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }

    @Override
    public Message updateAIResultQuestionByAIResultQuestionId(Long aiResultQuestionId, Double score) throws InternalServerException {
        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result-question/update?aiResultQuestionId=" + aiResultQuestionId+ "&score=" +score , HttpMethod.PUT, null, Message.class);
        if (response.getStatus() == 200 && response.getCode().equalsIgnoreCase("OK")) {
            return response;
        }
        return new Message<>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }

    public Message updateStudentEmailNameRollNumberByAiResultId(Long aiResultId, String email, String studentName, String studentRollNumber) throws InternalServerException {
        log.info("Attempting to update email || name || roll number for AI Result ID: {}, email: {}, name: {}, roll: {}",
                aiResultId, email, studentName, studentRollNumber);

        StringBuilder endpoint = new StringBuilder("/api/v1/result/update?aiResultId=" + aiResultId);

        if (email != null) {
            endpoint.append("&email=").append(email);
        }
        if (studentName != null) {
            endpoint.append("&studentName=").append(studentName);
        }
        if (studentRollNumber != null) {
            endpoint.append("&studentRollNumber=").append(studentRollNumber);
        }

        if (endpoint.toString().equals("/api/v1/result/update?aiResultId=" + aiResultId)) {
            return new Message<>()
                    .setStatus(HttpStatus.BAD_REQUEST.value())
                    .setCode(HttpStatus.BAD_REQUEST.name())
                    .setMessage("No update field provided. Please provide email, name, or roll number.")
                    .setData(null);
        }

        Message response = restClient.makeRequestForAIGrader(endpoint.toString(), HttpMethod.PUT, null, Message.class);

        log.info("Update response received with status: {} and code: {}", response.getStatus(), response.getCode());

        if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
            return response;
        }


        return new Message<>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }


    @Override
    public Message deleteAIResultByAIResultId(Long aiResultId) throws InternalServerException {
        log.info("Attempting to delete AI Result with ID: {}", aiResultId);

        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result/delete?aiResultId=" + aiResultId, HttpMethod.DELETE, null, Message.class);

        log.info("Delete response received with status: {} and code: {}", response.getStatus(), response.getCode());

        if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
            return response;
        }

        if (response.getStatus() == 404) {
            log.warn("AI Result with ID {} not found.", aiResultId);
            return new Message<>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.name())
                    .setMessage("AI Result not found with ID: " + aiResultId)
                    .setData(null);
        }

        return new Message<>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }

    @Override
    public Message updateStatusByAiResultId(Long aiResultId) throws InternalServerException {
        log.info("Attempting to update status for AI Result with ID: {}", aiResultId);

        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result/update/status?aiResultId=" + aiResultId, HttpMethod.PUT, null, Message.class);

        log.info("Update status response received for AI Result ID: {} with status: {} and code: {}",
                aiResultId, response.getStatus(), response.getCode());

        if (response.getStatus() == 200 && "OK".equalsIgnoreCase(response.getCode())) {
            log.info("AI Result status updated successfully for ID: {}", aiResultId);
            return response;
        }

        if (response.getStatus() == 404) {
            log.warn("AI Result not found for status update. ID: {}", aiResultId);
            return new Message<>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.name())
                    .setMessage("AI Result not found with ID: " + aiResultId)
                    .setData(null);
        }

        log.warn("Failed to update status for AI Result ID: {}. Status: {}, Code: {}, Message: {}",
                aiResultId, response.getStatus(), response.getCode(), response.getMessage());

        return new Message<>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }

    @Override
    public Message<String> startGrading(List<MultipartFile> quizFiles, MultipartFile answerKeyFile,
                                        String evaluationCriteria, Long assessmentId, String email)
            throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Starting grading for assessment ID: {}, email: {}", assessmentId, email);
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        int pagesUsedInThisSession = validatePagesUploadedByUser(quizFiles, subscribedUser);

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add quiz files
            if (quizFiles != null) {
                for (MultipartFile file : quizFiles) {
                    body.add("quiz_files", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
                }
            }

            // Add answer key
            if (answerKeyFile != null) {
                body.add("answer_key_file", new MultipartInputStreamFileResource(answerKeyFile.getInputStream(), answerKeyFile.getOriginalFilename()));
            }

            // Add form fields
            body.add("evaluationCriteria", evaluationCriteria);
            body.add("assessmentId", assessmentId.toString()); // Important: must be string

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            Message<String> response = restClient.makeRequestForAIGrader(
                    "/api/v1/grader/create",
                    HttpMethod.POST,
                    entity,
                    Message.class
            );

            if (response != null && response.getStatus() == HttpStatus.OK.value()) {
                User user = subscribedUser.getUser();
                int currentPagesUsed = user.getNoOfPagesUsed() == null ? 0 : user.getNoOfPagesUsed();
                user.setNoOfPagesUsed(currentPagesUsed + pagesUsedInThisSession);
                userService.save(user);
            }

            return response;

        } catch (IOException e) {
            log.error("Error uploading files: ", e);
            return new Message<String>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("File processing error.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int validatePagesUploadedByUser(List<MultipartFile> quizFiles, SubscribedUser subscribedUser) throws BadRequestException, InternalServerException {
        log.info("Validating the pdf upload limit for FREE users");
        int noOfPages = getPdfPageCount(quizFiles);
        log.info("Total number of pages: {}", noOfPages);

        var subValidation = subscriptionValidationsService.getBySubscriptionIdAndValidationName(
                subscribedUser.getSubscription().getId(), SubscriptionValidation.PDF_PAGE_COUNT.name());
        User user = subscribedUser.getUser();

        if(subValidation != null) {
            int noOfPagesUsedByUser = user.getNoOfPagesUsed() == null ? 0 : user.getNoOfPagesUsed();
            log.info("No of pages used by user: {}", noOfPagesUsedByUser);

            if (noOfPagesUsedByUser + noOfPages > subValidation.getValue()) {
                throw new BadRequestException("PDF upload limit exceeded for " + subscribedUser.getSubscription().getPlanType() + " users.");
            }

        }

        return noOfPages;

    }

    @Override
    public Message sendEmail(String email, Long aiResultId, String name) throws InternalServerException, EntityNotFoundException {
        log.info("Attempting to send result email to '{}' for classId={}", email, aiResultId);
        User user = userService.findByEmail(name);

        String teacherName = user.getFullName();

        User student = null;
        try {
            student = userService.findByEmail(email);
        } catch (EntityNotFoundException ex) {
            log.warn("Student not found with email: {}. Notification will be skipped.", email);
        }

        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result/send-email?email=" + email + "&aiResultId=" + aiResultId + "&teacherName="+ teacherName,
                HttpMethod.POST,
                null,
                Message.class
        );

        log.info("Send email response received with status: {}, code: {}, message: {}",
                response.getStatus(), response.getCode(), response.getMessage());

        if (student != null) {
            rabbitMQProducer.sendSimpleNotification(
                    student.getId(),
                    "Your AI Grader result has been sent to your email.",
                    "student/grader-results",
                    teacherName
            );
        }


        if (response.getStatus() == 404) {
            log.warn("Class not found while sending email. Class ID: {}", aiResultId);
            return new Message<>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.name())
                    .setMessage("Class not found with ID: " + aiResultId)
                    .setData(null);
        }

        log.warn("Failed to send email to: {}. Status: {}, Code: {}, Message: {}",
                email, response.getStatus(), response.getCode(), response.getMessage());

        return new Message<>()
                .setStatus(response.getStatus())
                .setCode(response.getCode())
                .setMessage(response.getMessage())
                .setData(response.getData());
    }

    private int getPdfPageCount(List<MultipartFile> file) throws BadRequestException {
        if(file == null || file.isEmpty()) return 0;

        AtomicInteger pageCount = new AtomicInteger();
        for (MultipartFile f : file) {
            try (PDDocument document = PDDocument.load(f.getInputStream())) {
                int singleDocNoOfPages = document.getNumberOfPages();
                if (singleDocNoOfPages > 6) {
                    log.warn("PDF file '{}' has more than 6 pages. Please upload a file with less than or equal to 6 pages.", f.getOriginalFilename());
                    throw new BadRequestException("PDF file has more than 6 pages. Please upload a file with less than or equal to 6 pages.");
                }
                pageCount.addAndGet(singleDocNoOfPages);
            } catch (IOException e) {
                log.warn("Failed to get page count for file: {}", f.getOriginalFilename());
                log.warn("Error: {}", e.getMessage());
            }
        }
        return pageCount.get();
    }

    public Message<String> startGradingLandingPage(List<MultipartFile> quizFiles, MultipartFile answerKeyFile,
                                                   String evaluationCriteria,String assessmentName, String className, String userSubmittedAnswerAsText, String email) throws IOException, EntityNotFoundException, BadRequestException {
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            if (quizFiles != null) {
                for (MultipartFile file : quizFiles) {
                    body.add("quiz_files", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
                }
            } else if (userSubmittedAnswerAsText != null && !userSubmittedAnswerAsText.isBlank()) {
                // convert text to pdf
                byte[] bytes = excelExportService.convertTextToPdf(userSubmittedAnswerAsText);
                // convert byte array to multipart file
                InMemoryMultipartFile file = excelExportService
                        .convertBytesToMultipartFile(bytes, "student.pdf", "application/pdf");
                // add that file to quizFiles list and append to MultiValueMap
                var filePart = new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename());
                body.add("quiz_files", filePart);
                quizFiles = List.of(file);
            }

            if (answerKeyFile != null) {
                body.add("answer_key_file", new MultipartInputStreamFileResource(answerKeyFile.getInputStream(), answerKeyFile.getOriginalFilename()));
            }
            body.add("evaluationCriteria", evaluationCriteria);
            body.add("assessmentName", assessmentName);
            body.add("className", className);
            body.add("instructorId", subscribedUser.getUser().getId());

            int pagesUsedInThisSession = validatePagesUploadedByUser(quizFiles, subscribedUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            Message<String> response = restClient.makeRequestForAIGrader(
                    "/api/v1/grader/create-landing-page",
                    HttpMethod.POST,
                    entity,
                    Message.class
            );

            if (response != null && response.getStatus() == HttpStatus.OK.value()) {
                User user = subscribedUser.getUser();
                int currentPagesUsed = user.getNoOfPagesUsed() == null ? 0 : user.getNoOfPagesUsed();
                user.setNoOfPagesUsed(currentPagesUsed + pagesUsedInThisSession);
                userService.save(user);
            }

            return response;
        } catch (IOException e) {
            log.error("Error uploading files: ", e);
            return new Message<String>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("File processing error.");
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message getAIResultById(Long id) throws InternalServerException {

        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result/ai-result/" + id,
                HttpMethod.GET,
                null,
                Message.class
        );


        if (response.getStatus() == 200 && response.getCode().equalsIgnoreCase("OK")) {
            return response;
        }

        return new Message<>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }

    public Message retryGrading(Long resultId) throws BadRequestException, InternalServerException {
        if(Objects.nonNull(resultId)){
            Message response = restClient.makeRequestForAIGrader(
                    "/api/v1/grader/retry-grading?resultId=" + resultId,
                    HttpMethod.POST,
                    null,
                    Message.class
            );
            return response;
        }
        throw new BadRequestException("resultId cannot be null");
    }
}
