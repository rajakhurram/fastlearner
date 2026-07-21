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
import com.vinncorp.fast_learner.response.ai_grader.ai_result_question.AIResultQueRespByPaginated;
import com.vinncorp.fast_learner.services.excel.ExcelExportService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionValidationsService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.InMemoryMultipartFile;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.MultipartInputStreamFileResource;
import com.vinncorp.fast_learner.util.enums.SubscriptionValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIResultService implements IAIResultService {

    private static final int MAX_ARCHIVE_FILE_COUNT = 200;

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
    public Message updateAIResultQuestionByAIResultQuestionId(Long aiResultQuestionId, Double score, Double totalMarks) throws InternalServerException {
        StringBuilder endpoint = new StringBuilder("/api/v1/result-question/update?aiResultQuestionId=")
                .append(aiResultQuestionId)
                .append("&score=")
                .append(score);
        if (totalMarks != null) {
            endpoint.append("&totalMarks=").append(totalMarks);
        }
        Message response = restClient.makeRequestForAIGrader(
                endpoint.toString(), HttpMethod.PUT, null, Message.class);
        if (response.getStatus() == 200 && response.getCode().equalsIgnoreCase("OK")) {
            return response;
        }
        return new Message<>()
                .setMessage(response.getMessage())
                .setCode(response.getCode())
                .setStatus(response.getStatus())
                .setData(response.getData());
    }

    @Override
    public Message createManualQuestion(
            com.vinncorp.fast_learner.request.ai_grader.ai_result_question.CreateManualQuestionRequest request)
            throws InternalServerException {
        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result-question/manual", HttpMethod.POST, request, Message.class);
        return proxyMessage(response);
    }

    @Override
    public Message updateManualQuestion(
            Long aiResultQuestionId,
            com.vinncorp.fast_learner.request.ai_grader.ai_result_question.UpdateManualQuestionRequest request)
            throws InternalServerException {
        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result-question/manual?aiResultQuestionId=" + aiResultQuestionId,
                HttpMethod.PUT,
                request,
                Message.class);
        return proxyMessage(response);
    }

    @Override
    public Message deleteManualQuestion(Long aiResultQuestionId) throws InternalServerException {
        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/result-question/manual?aiResultQuestionId=" + aiResultQuestionId,
                HttpMethod.DELETE,
                null,
                Message.class);
        return proxyMessage(response);
    }

    private Message proxyMessage(Message response) {
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

    /**
     * Validates total pages in a single grading request against the plan quota (PDF_PAGE_COUNT).
     * This is used for ZIP/RAR expansion where we already computed the total page count.
     */
    private void validatePagesUploadedByUserByPageCount(int noOfPagesThisSession, SubscribedUser subscribedUser)
            throws BadRequestException, InternalServerException {
        if (subscribedUser == null || subscribedUser.getSubscription() == null) return;

        var subValidation = subscriptionValidationsService.getBySubscriptionIdAndValidationName(
                subscribedUser.getSubscription().getId(),
                SubscriptionValidation.PDF_PAGE_COUNT.name()
        );
        if (subValidation == null || subValidation.getValue() == null) return;

        User user = subscribedUser.getUser();
        int noOfPagesUsedByUser = user.getNoOfPagesUsed() == null ? 0 : user.getNoOfPagesUsed();
        if (noOfPagesUsedByUser + noOfPagesThisSession > subValidation.getValue()) {
            throw new BadRequestException("PDF upload limit exceeded for " + subscribedUser.getSubscription().getPlanType() + " users.");
        }
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
                if (singleDocNoOfPages > 10) {
                    log.warn("PDF file '{}' has more than 10 pages. Please upload a file with less than or equal to 10 pages.", f.getOriginalFilename());
                    throw new BadRequestException("PDF file has more than 10 pages. Please upload a file with less than or equal to 10 pages.");
                }
                pageCount.addAndGet(singleDocNoOfPages);
            } catch (IOException e) {
                log.warn("Failed to get page count for file: {}", f.getOriginalFilename());
                log.warn("Error: {}", e.getMessage());
            }
        }
        return pageCount.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Message<Map<String, Object>> startGradingLandingPage(List<MultipartFile> quizFiles, MultipartFile answerKeyFile, String evaluationCriteria, Long classId, Long assessmentId, String assessmentName, String className, String userSubmittedAnswerAsText, String email)
            throws IOException, EntityNotFoundException, BadRequestException {

        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        Long totalNumberOfFiles = 0l;

        try {
            // Guard: must have either files or text before we create records on AI Grader
            boolean hasFiles = quizFiles != null && !quizFiles.isEmpty();
            boolean hasText  = userSubmittedAnswerAsText != null && !userSubmittedAnswerAsText.isBlank();
            if (!hasFiles && !hasText) {
                throw new BadRequestException("Please provide quiz files or a text answer.");
            }

            // ── Step 1: Create class + assessment synchronously on AI Grader ──────
            // This returns classId and assessmentId BEFORE any file processing starts,
            // so the UI receives them in the immediate response.
            if(classId==null || classId==0l || assessmentId==null || assessmentId==0l) {
                Map<String, Object> initData = callInitLandingPage(
                        assessmentName, className, subscribedUser.getUser().getId());
                 classId = Long.parseLong(initData.get("classId").toString());
                 assessmentId = Long.parseLong(initData.get("assessmentId").toString());
            }
            // ── Path A: text-to-PDF single submission ─────────────────────────────
            if ((quizFiles == null || quizFiles.isEmpty()) &&
                    userSubmittedAnswerAsText != null && !userSubmittedAnswerAsText.isBlank()) {

                totalNumberOfFiles++;
                byte[] bytes = excelExportService.convertTextToPdf(userSubmittedAnswerAsText);
                InMemoryMultipartFile textFile = excelExportService
                        .convertBytesToMultipartFile(bytes, "student.pdf", "application/pdf");
                List<MultipartFile> singleFileList = new ArrayList<>();
                singleFileList.add(textFile);
                int pages = validatePagesUploadedByUser(singleFileList, subscribedUser);

                MultipartFile preparedKey = expandAnswerKeyZip(answerKeyFile);
                byte[] answerKeyBytes = preparedKey != null ? preparedKey.getBytes() : null;
                String answerKeyFilename = preparedKey != null ? preparedKey.getOriginalFilename() : null;

                // Send single file to /create using the pre-created assessmentId
                sendSubsequentLandingBatch(
                        List.of(new InMemoryMultipartFile(bytes, "quiz_files", "student.pdf", MediaType.APPLICATION_PDF_VALUE)),
                        answerKeyBytes, answerKeyFilename, evaluationCriteria, assessmentId);

                // Deduct pages from user quota
                User user = subscribedUser.getUser();
                user.setNoOfPagesUsed((user.getNoOfPagesUsed() == null ? 0 : user.getNoOfPagesUsed()) + pages);
                userService.save(user);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("assessmentId", assessmentId);
                data.put("classId",      classId);
                return new Message<Map<String, Object>>()
                        .setStatus(HttpStatus.OK.value())
                        .setCode(HttpStatus.OK.name())
                        .setMessage("Request accepted, reports will be shown in a while please wait.")
                        .setData(data);
            }

            // ── Path B: ZIP / PDF upload — stream, validate, batch ────────────────
            int pagesAlreadyUsed = subscribedUser.getUser().getNoOfPagesUsed() == null
                    ? 0 : subscribedUser.getUser().getNoOfPagesUsed();
            Long remainingPageBudget = null;
            var subValidation = subscriptionValidationsService.getBySubscriptionIdAndValidationName(
                    subscribedUser.getSubscription().getId(), SubscriptionValidation.PDF_PAGE_COUNT.name());
            if (subValidation != null) {
                remainingPageBudget = subValidation.getValue() - pagesAlreadyUsed;
            }

            // Stream ZIP entries one-by-one; count pages and fail fast
            List<InMemoryMultipartFile> allFiles = new ArrayList<>();
            int totalPagesThisSession = 0;

            for (MultipartFile quizFile : quizFiles) {
                if (isZipFile(quizFile)) {
                    try (ZipInputStream zip = new ZipInputStream(quizFile.getInputStream())) {
                        ZipEntry entry;
                        int archiveFileCount = 0;
                        while ((entry = zip.getNextEntry()) != null) {
                            if (entry.isDirectory()) continue;
                            String entryName = entry.getName();
                            byte[] entryBytes = readEntryBytes(zip);
                            byte[] pdfBytes;
                            String pdfName;
                            if (isPdfFilename(entryName)) {
                                pdfBytes = entryBytes;
                                pdfName  = entryName;
                            } else if (isImageFilename(entryName)) {
                                pdfBytes = convertImageToPdf(entryBytes, entryName);
                                pdfName  = replaceExtensionWithPdf(entryName);
                            } else {
                                continue;
                            }
                            archiveFileCount++;
                            if (archiveFileCount > MAX_ARCHIVE_FILE_COUNT)
                                throw new BadRequestException("Archive '" + quizFile.getOriginalFilename()
                                        + "' contains more than " + MAX_ARCHIVE_FILE_COUNT + " files.");
                            try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
                                int pages = doc.getNumberOfPages();
                                if (pages > 10)
                                    throw new BadRequestException("PDF '" + pdfName + "' exceeds page limit.");
                                totalPagesThisSession += pages;
                            }
                            totalNumberOfFiles++;
                            allFiles.add(new InMemoryMultipartFile(pdfBytes, "quiz_files", pdfName, MediaType.APPLICATION_PDF_VALUE));
                        }
                    } catch (BadRequestException e) {
                        throw e;
                    } catch (IOException ex) {
                        log.error("Failed to read zip '{}'", quizFile.getOriginalFilename(), ex);
                        throw new BadRequestException("Invalid or corrupted ZIP file.");
                    }
                } else if (isRarFile(quizFile)) {
                    byte[] rarBytes = quizFile.getBytes();
                    try (IInArchive archive = SevenZip.openInArchive(null, new RarByteArrayStream(rarBytes))) {
                        ISimpleInArchive simple = archive.getSimpleInterface();
                        int archiveFileCount = 0;
                        for (ISimpleInArchiveItem item : simple.getArchiveItems()) {
                            if (item.isFolder()) continue;
                            String entryName = item.getPath();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            item.extractSlow(data -> { baos.write(data, 0, data.length); return data.length; });
                            byte[] entryBytes = baos.toByteArray();
                            byte[] pdfBytes;
                            String pdfName;
                            if (isPdfFilename(entryName)) {
                                pdfBytes = entryBytes;
                                pdfName  = entryName;
                            } else if (isImageFilename(entryName)) {
                                pdfBytes = convertImageToPdf(entryBytes, entryName);
                                pdfName  = replaceExtensionWithPdf(entryName);
                            } else {
                                continue;
                            }
                            archiveFileCount++;
                            if (archiveFileCount > MAX_ARCHIVE_FILE_COUNT)
                                throw new BadRequestException("Archive '" + quizFile.getOriginalFilename()
                                        + "' contains more than " + MAX_ARCHIVE_FILE_COUNT + " files.");
                            try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
                                int pages = doc.getNumberOfPages();
                                if (pages > 10)
                                    throw new BadRequestException("PDF '" + pdfName + "' exceeds page limit.");
                                totalPagesThisSession += pages;
                            }
                            totalNumberOfFiles++;
                            allFiles.add(new InMemoryMultipartFile(pdfBytes, "quiz_files", pdfName, MediaType.APPLICATION_PDF_VALUE));
                        }
                    } catch (BadRequestException e) {
                        throw e;
                    } catch (IOException ex) {
                        log.error("Failed to read RAR '{}'", quizFile.getOriginalFilename(), ex);
                        throw new BadRequestException("Invalid or corrupted RAR file.");
                    }
                } else if (isPdfFilename(quizFile.getOriginalFilename())) {
                    byte[] fileBytes = quizFile.getBytes();
                    try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(fileBytes))) {
                        int pages = doc.getNumberOfPages();
                        if (pages > 10) throw new BadRequestException(
                                "PDF '" + quizFile.getOriginalFilename() + "' has more than 10 pages.");
                        totalPagesThisSession += pages;
                    }
                    totalNumberOfFiles++;
                    allFiles.add(new InMemoryMultipartFile(
                            fileBytes, "quiz_files", quizFile.getOriginalFilename(), MediaType.APPLICATION_PDF_VALUE));
                } else if (isImageFilename(quizFile.getOriginalFilename())) {
                    byte[] imageBytes = quizFile.getBytes();
                    byte[] pdfBytes   = convertImageToPdf(imageBytes, quizFile.getOriginalFilename());
                    String pdfName    = replaceExtensionWithPdf(quizFile.getOriginalFilename());
                    try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
                        totalPagesThisSession += doc.getNumberOfPages();
                    }
                    totalNumberOfFiles++;
                    allFiles.add(new InMemoryMultipartFile(pdfBytes, "quiz_files", pdfName, MediaType.APPLICATION_PDF_VALUE));
                }
            }

            if (allFiles.isEmpty()) throw new BadRequestException("No valid PDF files found in the uploaded file(s).");

            // Enforce plan quota based on TOTAL PAGES in this upload (including ZIP contents).
            // Example: 20 PDFs × 4 pages = 80 pages → blocked if PDF_PAGE_COUNT < 80.
            validatePagesUploadedByUserByPageCount(totalPagesThisSession, subscribedUser);

            // Read answer key bytes once — replayed across all batches
            MultipartFile preparedAnswerKey = expandAnswerKeyZip(answerKeyFile);
            byte[] answerKeyBytes    = preparedAnswerKey != null ? preparedAnswerKey.getBytes() : null;
            String answerKeyFilename = preparedAnswerKey != null ? preparedAnswerKey.getOriginalFilename() : null;

            List<List<InMemoryMultipartFile>> batches = partitionList(allFiles, 10);

            // Finals for async lambda
            final byte[] finalAnswerKeyBytes    = answerKeyBytes;
            final String finalAnswerKeyFilename = answerKeyFilename;
            final String finalEvalCriteria      = evaluationCriteria;
            final Long   finalAssessmentId      = assessmentId;
            final Long   finalInstructorId      = subscribedUser.getUser().getId();
            final int    finalTotalPages        = totalPagesThisSession;
            final User   userRef                = subscribedUser.getUser();

            // ── Fire-and-forget: all batches use /create (assessmentId already known) ──
            CompletableFuture.runAsync(() -> {
                try {
                    for (List<InMemoryMultipartFile> batch : batches) {
                        sendSubsequentLandingBatch(batch, finalAnswerKeyBytes, finalAnswerKeyFilename,
                                finalEvalCriteria, finalAssessmentId);
                    }
                    // Deduct page quota after all batches accepted
                    userRef.setNoOfPagesUsed(
                            (userRef.getNoOfPagesUsed() == null ? 0 : userRef.getNoOfPagesUsed()) + finalTotalPages);
                    userService.save(userRef);
                } catch (Exception e) {
                    log.error("Background grading failed for instructor={}: {}", finalInstructorId, e.getMessage(), e);
                }
            });

            // Return immediately with the IDs — no need to wait for grading
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("assessmentId", assessmentId);
            data.put("classId",      classId);
            data.put("numberOfFiles",allFiles.size());
            return new Message<Map<String, Object>>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Request accepted, reports will be shown in a while please wait.")
                    .setData(data);

        } catch (BadRequestException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error processing uploaded files: ", e);
            return new Message<Map<String, Object>>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("File processing error.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a class then an assessment on AI Grader using its existing dedicated endpoints
     * (the same endpoints used by AiGraderClassService and AssessmentService).
     * Class/assessment ownership stays entirely in FastLearner — no extra endpoint needed on AI Grader.
     *
     * Flow:
     *   1. POST /api/v1/ai-grader-class/create   → returns AIClass  { id, name, ... }
     *   2. POST /api/v1/ai-assessment/create      → returns AIAssessment { id, name, ... }
     *
     * Returns a map with "classId" and "assessmentId" keys.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callInitLandingPage(String assessmentName,
                                                    String className,
                                                    Long instructorId) throws InternalServerException {
        // ── Step 1: create class ──────────────────────────────────────────────────
        Map<String, Object> classBody = new LinkedHashMap<>();
        classBody.put("name",         className);
        classBody.put("instructorId", instructorId);

        Message classResponse = restClient.makeRequestForAIGrader(
                "/api/v1/ai-grader-class/create", HttpMethod.POST, classBody, Message.class);

        if (classResponse == null || classResponse.getStatus() != HttpStatus.OK.value()) {
            throw new RuntimeException("AI Grader class creation failed. Status: " +
                    (classResponse != null ? classResponse.getStatus() : "null"));
        }
        Map<String, Object> classData = (Map<String, Object>) classResponse.getData();
        if (classData == null || !classData.containsKey("id")) {
            throw new RuntimeException("AI Grader class creation response missing 'id'.");
        }
        Long classId = ((Number) classData.get("id")).longValue();

        // ── Step 2: create assessment ─────────────────────────────────────────────
        Map<String, Object> assessmentBody = new LinkedHashMap<>();
        assessmentBody.put("name",    assessmentName);
        assessmentBody.put("classId", classId);

        Message assessmentResponse = restClient.makeRequestForAIGrader(
                "/api/v1/ai-assessment/create", HttpMethod.POST, assessmentBody, Message.class);

        if (assessmentResponse == null || assessmentResponse.getStatus() != HttpStatus.OK.value()) {
            throw new RuntimeException("AI Grader assessment creation failed. Status: " +
                    (assessmentResponse != null ? assessmentResponse.getStatus() : "null"));
        }
        Map<String, Object> assessmentData = (Map<String, Object>) assessmentResponse.getData();
        if (assessmentData == null || !assessmentData.containsKey("id")) {
            throw new RuntimeException("AI Grader assessment creation response missing 'id'.");
        }
        Long assessmentId = ((Number) assessmentData.get("id")).longValue();

        // ── Return both IDs ───────────────────────────────────────────────────────
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("classId",      classId);
        result.put("assessmentId", assessmentId);
        return result;
    }

    /**
     * Sends a batch of files to AI Grader's /create endpoint using an existing assessmentId.
     * Used for ALL batches now that class+assessment are pre-created via /init-landing-page.
     */
    private void sendSubsequentLandingBatch(List<InMemoryMultipartFile> batch,
                                            byte[] answerKeyBytes, String answerKeyFilename,
                                            String evaluationCriteria, Long assessmentId) throws IOException, InternalServerException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (InMemoryMultipartFile file : batch) {
            body.add("quiz_files", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        }
        if (answerKeyBytes != null) {
            body.add("answer_key_file", new MultipartInputStreamFileResource(new ByteArrayInputStream(answerKeyBytes), answerKeyFilename));
        }
        body.add("evaluationCriteria", evaluationCriteria);
        body.add("assessmentId", assessmentId.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        Message response = restClient.makeRequestForAIGrader(
                "/api/v1/grader/create", HttpMethod.POST,
                new HttpEntity<>(body, headers), Message.class);

        if (response == null || response.getStatus() != HttpStatus.OK.value()) {
            log.error("Batch send failed for assessmentId={}. Status: {}", assessmentId,
                    response != null ? response.getStatus() : "null");
        }
    }

    /**
     * Splits a list into sub-lists of at most batchSize elements.
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(new ArrayList<>(list.subList(i, Math.min(i + batchSize, list.size()))));
        }
        return partitions;
    }

    private List<MultipartFile> expandZipFiles(List<MultipartFile> files, String requestPartName) throws BadRequestException {
        if (files == null || files.isEmpty()) {
            return files;
        }

        List<MultipartFile> expandedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (isZipFile(file)) {
                expandedFiles.addAll(extractPdfFilesFromZip(file, requestPartName));
            } else {
                expandedFiles.add(file);
            }
        }
        return expandedFiles;
    }

    private MultipartFile expandAnswerKeyZip(MultipartFile answerKeyFile) throws BadRequestException {
        if (answerKeyFile == null) return null;
        if (isZipFile(answerKeyFile)) {
            List<MultipartFile> extracted = extractPdfFilesFromZip(answerKeyFile, "answer_key_file");
            if (extracted.size() != 1)
                throw new BadRequestException("answer_key_file ZIP must contain exactly one PDF file.");
            return extracted.get(0);
        }
        if (isRarFile(answerKeyFile)) {
            List<MultipartFile> extracted = extractFilesFromRar(answerKeyFile, "answer_key_file");
            if (extracted.size() != 1)
                throw new BadRequestException("answer_key_file RAR must contain exactly one PDF file.");
            return extracted.get(0);
        }
        return answerKeyFile;
    }

    private List<MultipartFile> extractPdfFilesFromZip(MultipartFile zipFile, String requestPartName) throws BadRequestException {
        List<MultipartFile> extractedPdfFiles = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (!isPdfFilename(entry.getName())) {
                    continue;
                }
                byte[] entryBytes = readEntryBytes(zipInputStream);
                extractedPdfFiles.add(new InMemoryMultipartFile(
                        entryBytes,
                        requestPartName,
                        entry.getName(),
                        MediaType.APPLICATION_PDF_VALUE
                ));
            }
        } catch (IOException ex) {
            log.error("Failed to extract zip file '{}'", zipFile.getOriginalFilename(), ex);
            throw new BadRequestException("Invalid zip file uploaded.");
        }

        if (extractedPdfFiles.isEmpty()) {
            throw new BadRequestException("Zip file '" + zipFile.getOriginalFilename() + "' does not contain any PDF files.");
        }

        return extractedPdfFiles;
    }

    private boolean isRarFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".rar")) return true;
        String ct = file.getContentType();
        return "application/x-rar-compressed".equalsIgnoreCase(ct) || "application/vnd.rar".equalsIgnoreCase(ct);
    }

    private static final class RarByteArrayStream implements IInStream {
        private final byte[] data;
        private int pos;

        RarByteArrayStream(byte[] data) { this.data = data; }

        @Override
        public long seek(long offset, int seekOrigin) throws SevenZipException {
            switch (seekOrigin) {
                case SEEK_SET: pos = (int) offset; break;
                case SEEK_CUR: pos += (int) offset; break;
                case SEEK_END: pos = data.length + (int) offset; break;
            }
            return pos;
        }

        @Override
        public int read(byte[] buf) throws SevenZipException {
            if (pos >= data.length) return 0;
            int n = Math.min(buf.length, data.length - pos);
            System.arraycopy(data, pos, buf, 0, n);
            pos += n;
            return n;
        }

        @Override
        public void close() {}
    }

    private boolean isImageFilename(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".bmp") || lower.endsWith(".gif");
    }

    private String replaceExtensionWithPdf(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 ? filename.substring(0, dot) : filename) + ".pdf";
    }

    private byte[] convertImageToPdf(byte[] imageBytes, String filename) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDImageXObject image = PDImageXObject.createFromByteArray(doc, imageBytes, filename);
            float pageW  = page.getMediaBox().getWidth();
            float pageH  = page.getMediaBox().getHeight();
            float scale  = Math.min(pageW / image.getWidth(), pageH / image.getHeight());
            float drawW  = image.getWidth()  * scale;
            float drawH  = image.getHeight() * scale;
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(image, (pageW - drawW) / 2, (pageH - drawH) / 2, drawW, drawH);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private List<MultipartFile> extractFilesFromRar(MultipartFile rarFile, String requestPartName) throws BadRequestException {
        List<MultipartFile> extracted = new ArrayList<>();
        try {
            byte[] rarBytes = rarFile.getBytes();
            try (IInArchive archive = SevenZip.openInArchive(null, new RarByteArrayStream(rarBytes))) {
                ISimpleInArchive simple = archive.getSimpleInterface();
                for (ISimpleInArchiveItem item : simple.getArchiveItems()) {
                    if (item.isFolder()) continue;
                    String entryName = item.getPath();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    item.extractSlow(data -> { baos.write(data, 0, data.length); return data.length; });
                    byte[] entryBytes = baos.toByteArray();
                    if (isPdfFilename(entryName)) {
                        extracted.add(new InMemoryMultipartFile(entryBytes, requestPartName, entryName, MediaType.APPLICATION_PDF_VALUE));
                    } else if (isImageFilename(entryName)) {
                        byte[] pdfBytes = convertImageToPdf(entryBytes, entryName);
                        extracted.add(new InMemoryMultipartFile(pdfBytes, requestPartName, replaceExtensionWithPdf(entryName), MediaType.APPLICATION_PDF_VALUE));
                    }
                }
            }
        } catch (IOException ex) {
            log.error("Failed to extract RAR file '{}'", rarFile.getOriginalFilename(), ex);
            throw new BadRequestException("Invalid or corrupted RAR file.");
        }
        if (extracted.isEmpty())
            throw new BadRequestException("RAR file '" + rarFile.getOriginalFilename() + "' does not contain any PDF or image files.");
        return extracted;
    }

    private byte[] readEntryBytes(ZipInputStream zipInputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int bytesRead;
        while ((bytesRead = zipInputStream.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    private boolean isZipFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".zip")) {
            return true;
        }
        String contentType = file.getContentType();
        return "application/zip".equalsIgnoreCase(contentType) || "application/x-zip-compressed".equalsIgnoreCase(contentType);
    }

    private boolean isPdfFilename(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".pdf");
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
