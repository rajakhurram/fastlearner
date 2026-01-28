package com.vinncorp.fast_learner.services.quiz;

import com.stripe.net.RequestOptions;
import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.quiz.QuizAnswer;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestion;
import com.vinncorp.fast_learner.dtos.quiz.RandomQuizProjection;
import com.vinncorp.fast_learner.dtos.quiz.*;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.models.quiz.QuizAttemptAnswer;
import com.vinncorp.fast_learner.models.quiz.QuizAttemptReport;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.repositories.quiz.*;
import com.vinncorp.fast_learner.dtos.section.QuizQuestionAnswers;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.chat_request.ChatRequest;
import com.vinncorp.fast_learner.request.prompt.Prompts;
import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import com.vinncorp.fast_learner.request.quiz.QuizQuestionReport;
import com.vinncorp.fast_learner.response.quiz.*;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.excel.ExcelExportService;
import com.vinncorp.fast_learner.services.open_ai.IOpenAIService;
import com.vinncorp.fast_learner.services.reports.TestReportService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import com.vinncorp.fast_learner.util.enums.TestType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizQuestionAnswerService implements IQuizQuestionAnswerService{

    private final QuizQuestionAnwserRepository repo;

    private final UserRepository userRepository;

    private final ExcelExportService excelExportService;
    private final GenericRestClient restClient;

    private final QuizAttemptRepository quizAttemptRepository;
    private final IUserService userService;
    private final IOpenAIService openAIService;
    private final IQuizService quizService;
    private final QuizRepository quizRepository;
    private final TestReportService testReportService;
    private final CourseRepository courseRepository;
    private final QuizQuestionRepository quizRepo;
    private final QuizAttemptAnswerRepository quizAttemptAnswerRepository;


    private final QuizReportService quizReportService;

    @Override
    public QuizQuestionAnwser save(QuizQuestionAnwser quizQuestionAnswer) throws InternalServerException, BadRequestException {
        log.info("Creating quiz question's answers.");

        boolean isDelete = Boolean.TRUE.equals(quizQuestionAnswer.getDelete());

        if (isDelete && Objects.isNull(quizQuestionAnswer.getId())) {
            throw new BadRequestException("Answer id cannot be empty when delete");
        }
        try {
            if (Objects.nonNull(quizQuestionAnswer.getId()) && isDelete) {
                repo.deleteById(quizQuestionAnswer.getId());
                return null;
            } else {
                return repo.save(quizQuestionAnswer);
            }
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("Quiz question answer" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<QuizQuestionAnswers> findByQuizQuestionId(Long quizQuestionId) throws EntityNotFoundException {
        log.info("Fetching quiz question answers by quiz question id: " + quizQuestionId);
        List<QuizQuestionAnwser> quizQuestionAnswers = repo.findByQuizQuestionId(quizQuestionId);
        if (CollectionUtils.isEmpty(quizQuestionAnswers)) {
            throw new EntityNotFoundException("No quiz question answers found.");
        }
        return QuizQuestionAnswers.from(quizQuestionAnswers);
    }

    /**
     * Fetch all questions and answers of quiz by topicId, forStudent param is used for populating the correct answer
     * flag in the response because we don't want to return correct answer option when a student is taking a quiz, we
     * only have to send the correct answer option when an instructor want to modify his course.
     */
    @Override
    public QuizQuestionAnswer fetchAllQuestionAndAnswersByTopicId(Long topicId, boolean forStudent, Boolean random, Pageable pageable) {
        log.info("Fetching all the quiz's question and answers by section id.");
        QuizQuestionAnswer quizQuestionAnswer = new QuizQuestionAnswer();
        if (random) {
            Page<RandomQuizProjection> quizQuestions = repo.findAllQuizQuestionAndAnswersByTopicRandom(topicId, pageable);
            quizQuestionAnswer.setQuizQuestions(QuizQuestion.from(quizQuestions.getContent()));
            setPageMetaData(quizQuestionAnswer, quizQuestions);
        } else {
            Page<QuizQuestion> quizQuestions = repo.findAllQuizQuestionAndAnswersByTopic(topicId, pageable);
            quizQuestionAnswer.setQuizQuestions(quizQuestions.getContent());
            setPageMetaData(quizQuestionAnswer, quizQuestions);
        }
        this.setQuestionAnswers(quizQuestionAnswer, forStudent);
        quizQuestionAnswer.setTopicId(quizQuestionAnswer.getQuizQuestions().get(0).getTopicId());
        quizQuestionAnswer.setQuizId(quizQuestionAnswer.getQuizQuestions().get(0).getQuizId());
        return quizQuestionAnswer;
    }

    private void setPageMetaData(QuizQuestionAnswer target, Page<?> page) {
        target.setPageNumber(page.getNumber());
        target.setPageSize(page.getSize());
        target.setTotalPages(page.getTotalPages());
        target.setTotalElements(page.getTotalElements());
    }

    private void setQuestionAnswers(QuizQuestionAnswer quizQuestionAnswer, Boolean forStudent){
        quizQuestionAnswer.getQuizQuestions().forEach(quizQuestion -> {
            List<QuizAnswer> answers = this.repo.findByQuizQuestionIdCustom(quizQuestion.getQuestionId());

            if (forStudent) {
                answers.forEach(answer -> {
                    answer.setIsCorrect(null);
                    if (quizQuestion.getQuestionType().equals(QuestionType.TEXT_FIELD)) {
                        answer.setAnswerText(null);
                    }
                });
            }

            quizQuestion.setQuizAnswers(answers);
        });

    }

    @Override
    public Message<QuizQuestionAnswerResponse> validateAnswer(Long questionId, Long answerId, String email)
            throws EntityNotFoundException, BadRequestException {
        log.info("Validating answer.");
        if (Objects.isNull(answerId)) {
            throw new BadRequestException("Please provide answer ID.");
        }

        List<QuizQuestionAnwser> quizQuestionAnswers = repo.findByQuizQuestionId(questionId);
        if (CollectionUtils.isEmpty(quizQuestionAnswers)) {
            throw new EntityNotFoundException("No data found for provided question and answer id.");
        }

        QuizQuestionAnwser quizQuestionAnwser = quizQuestionAnswers.stream().filter(e -> Objects.equals(e.getId(), answerId)).findAny()
                .orElseThrow(() -> new EntityNotFoundException("Question doesn't have any answer by provided id."));
        boolean isCorrect = quizQuestionAnwser.isCorrectAnswer();
        var correctAnswer = quizQuestionAnswers.stream().filter(QuizQuestionAnwser::isCorrectAnswer).findAny();
        if(correctAnswer.isEmpty())
            throw new EntityNotFoundException("Correct answer is not present for the question.");
        return new Message<QuizQuestionAnswerResponse>()
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setMessage(isCorrect ? "Answer is correct." : "Answer is incorrect.")
                .setData(QuizQuestionAnswerResponse.builder().isCorrect(isCorrect).correctAnswerId(correctAnswer.get().getId()).build());
    }



    @Override
    public Message<QuizAnswerResponse> validateAnswers(List<ValidationAnswerRequest> validationAnswerRequests, String name) throws InternalServerException {

        log.info("Checking if instructor exists with email: {}", name);
        Long courseId = validationAnswerRequests.get(0).getCourseId();
        String userTimeZone = validationAnswerRequests.get(0).getTimeZone();
        String instructorName = courseRepository.findById(courseId).get().getInstructor().getFullName();

        List<QuestionEvaluationResponse> evaluationResponseList = new ArrayList<>();
        Optional<User> instructorOpt = userRepository.findByEmail(name);
        if (!instructorOpt.isPresent()) {
            log.info("Instructor not found with email: {}", name);
            return new Message<QuizAnswerResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())

                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + name);
        }
        log.info("Validating answers for user: {}", name);

        List<QuestionAnswerResponse> questionAnswerResponses = new ArrayList<>();
        AtomicLong totalCorrectAnswers = new AtomicLong();
        Long totalAttemptQuestion = 0L;
        Long totalQuestion = 0L;
        if (validationAnswerRequests == null || validationAnswerRequests.isEmpty()) {
            throw new RequestOptions.InvalidRequestOptionsException("Invalid Request Body: No question and answers provided");
        }
        Long passingPercentage = repo.findPassingCriteriaAndQuizIdByQuestionId(validationAnswerRequests.get(0).getQuestionId());

        if (passingPercentage == null) {
            throw new RuntimeException("No quiz data found for the provided question ID");
        }

        Double passingCriteria = passingPercentage == 0 ? 0.0 : passingPercentage;


        log.info("Passing criteria for the question: {}", passingCriteria);
        List<Long> listOfQuestions = new ArrayList<>();
        Quiz quiz = null;

        List<QuizQuestionReport> quizQuestionReports = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10); // max 10 parallel threads
        List<CompletableFuture<QuestionAnswerResponse>> futures = new ArrayList<>();

        quiz = this.quizService.findByQuizQuestionsId(validationAnswerRequests.get(0).getQuestionId());
        AtomicReference<Boolean> personalityTest = new AtomicReference<>(false);
        AtomicReference<String> prompt = new AtomicReference<>(quiz.getReportPrompt());
        AtomicReference<Boolean> generateAIReport = new AtomicReference<>(false);
        //prompt check
        for (ValidationAnswerRequest request : validationAnswerRequests) {
            QuestionEvaluationResponse questionEvaluationResponse = new QuestionEvaluationResponse();
            QuizQuestionReport quizQuestionReport = new QuizQuestionReport();

            if (request.getAnswerId() != null) {
                totalAttemptQuestion++;
            }
            CompletableFuture<QuestionAnswerResponse> future = CompletableFuture.supplyAsync(() -> {
                boolean isCorrect = false;
                List<Long> correctAnswerIds;
                List<QuizQuestionAnwser> correctAnswers = repo.findByQuizQuestionId(request.getQuestionId());

                if (correctAnswers.isEmpty()) {
                    return new QuestionAnswerResponse("Question not found", List.of(),
                            request.getAnswerId(), false, null, null, List.of(), request.getAnswerText());
                }

                QuestionType type = correctAnswers.get(0).getQuizQuestion().getQuestionType();
                List<Long> answerIds = request.getAnswerId() == null ? List.of() : request.getAnswerId();
                questionEvaluationResponse.setQuestionText(correctAnswers.get(0).getQuizQuestion().getQuestionText());
                if (!correctAnswers.get(0).getQuizQuestion().getQuiz().getTestType().equals(TestType.SURVEY) ||
                        correctAnswers.get(0).getQuizQuestion().getQuiz().getTestType() == null) {

                    questionEvaluationResponse.setCorrectAnswers(correctAnswers.stream().filter(QuizQuestionAnwser::isCorrectAnswer).map(QuizQuestionAnwser::getAnswer).toList());
                }
                quizQuestionReport.setQuestionText(correctAnswers.get(0).getQuizQuestion().getQuestionText());
                if (Boolean.TRUE.equals(correctAnswers.get(0).getQuizQuestion().getQuiz().getGenerateAIReport())) {
                    generateAIReport.set(true);
                    prompt.set(correctAnswers.get(0).getQuizQuestion().getQuiz().getReportPrompt() == null
                            ? "" : correctAnswers.get(0).getQuizQuestion().getQuiz().getReportPrompt());
                }

                correctAnswers.get(0).getQuizQuestion().getQuiz().getGenerateAIReport();
                List<Long> studentAnswerIds = request.getAnswerId() == null ? List.of() : request.getAnswerId();

                if (!correctAnswers.get(0).getQuizQuestion().getQuiz().getTestType().equals(TestType.SURVEY) ||
                        correctAnswers.get(0).getQuizQuestion().getQuiz().getTestType() == null) {

                    List<String> correctAnswerTexts = correctAnswers.stream()
                            .filter(QuizQuestionAnwser::isCorrectAnswer)
                            .map(QuizQuestionAnwser::getAnswer)
                            .toList();

                    questionEvaluationResponse.setCorrectAnswers(correctAnswerTexts);
                } else {
                    personalityTest.set(true);
                    List<String> allAnswerTexts = correctAnswers.stream()
                            .map(QuizQuestionAnwser::getAnswer)   // Just get answer text
                            .toList();

                    questionEvaluationResponse.setCorrectAnswers(allAnswerTexts);
                }

                // ✅ Get student-selected answer texts from IDs
                List<String> studentSelectedAnswerTexts = correctAnswers.stream()
                        .filter(a -> studentAnswerIds.contains(a.getId()))
                        .map(QuizQuestionAnwser::getAnswer)
                        .toList();

                quizQuestionReport.setStudentAnswer(studentSelectedAnswerTexts);
                questionEvaluationResponse.setStudentAnswers(studentSelectedAnswerTexts);


                quizQuestionReport.setCorrectAnswer(correctAnswers.stream().filter(QuizQuestionAnwser::isCorrectAnswer).map(QuizQuestionAnwser::getAnswer).toList());

                if (type.equals(QuestionType.TEXT_FIELD)) {
                    correctAnswerIds = new ArrayList<>();
                    isCorrect = this.openAIService.validateAnswer(
                            correctAnswers.get(0).getAnswer(),
                            request.getAnswerText()
                    );
                    if (isCorrect) {
                        totalCorrectAnswers.getAndIncrement();
                        correctAnswerIds.add(correctAnswers.get(0).getId());
                    }

                    if (personalityTest.get()) {
                        isCorrect = true;
                        totalCorrectAnswers.getAndIncrement();
                        quizQuestionReport.setIsStudentAnswerCorrect(true);
                        questionEvaluationResponse.setIsCorrect(true);
                    } else {
                        quizQuestionReport.setIsStudentAnswerCorrect(isCorrect);
                        questionEvaluationResponse.setIsCorrect(isCorrect);
                    }

                } else {
                    correctAnswerIds = correctAnswers.stream()
                            .filter(QuizQuestionAnwser::isCorrectAnswer)
                            .map(QuizQuestionAnwser::getId)
                            .collect(Collectors.toList());
                    List<Long> providedAnswerIds = request.getAnswerId() != null ? request.getAnswerId() : new ArrayList<>();

                    if (personalityTest.get()) {
                        // For personality test: everything selected is correct
                        isCorrect = true;
                        totalCorrectAnswers.getAndIncrement();
                        quizQuestionReport.setIsStudentAnswerCorrect(true);
                        questionEvaluationResponse.setIsCorrect(true);
                    } else {
                        List<Long> correctlySelected = providedAnswerIds.stream()
                                .filter(correctAnswerIds::contains)
                                .collect(Collectors.toList());

                        List<Long> incorrectlySelected = providedAnswerIds.stream()
                                .filter(id -> !correctAnswerIds.contains(id))
                                .collect(Collectors.toList());

                        List<Long> missedCorrectAnswers = correctAnswerIds.stream()
                                .filter(id -> !providedAnswerIds.contains(id))
                                .collect(Collectors.toList());

                        isCorrect = incorrectlySelected.isEmpty() && missedCorrectAnswers.isEmpty();
                        if (isCorrect) {
                            totalCorrectAnswers.getAndIncrement();
                            quizQuestionReport.setIsStudentAnswerCorrect(true);
                        }
                        questionEvaluationResponse.setIsCorrect(isCorrect);
                    }
                }
                evaluationResponseList.add(questionEvaluationResponse);
                quizQuestionReports.add(quizQuestionReport);
                List<AnswerResponse> answerResponseList = correctAnswers.stream()
                        .map(answer -> new AnswerResponse(answer.getId(), answer.getAnswer()))
                        .collect(Collectors.toList());
                return new QuestionAnswerResponse(
                        correctAnswers.get(0).getQuizQuestion().getQuestionText(),
                        correctAnswerIds,
                        request.getAnswerId(),
                        isCorrect,
                        correctAnswers.get(0).getQuizQuestion().getExplanation(),
                        type,
                        answerResponseList,
                        request.getAnswerText()
                );
            }, executor);

            futures.add(future);
        }
        String instructorPrompt = prompt.get();
        questionAnswerResponses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        executor.shutdown();
        log.info("response of evaluation");
        String quizTitle = quiz.getTitle();
        Integer quizDuration = quiz.getDurationInMinutes();

        log.info(evaluationResponseList.toString());
        totalQuestion = Long.valueOf(quiz.getRandomQuestion());

        double percentage = (totalQuestion > 0) ? ((double) totalCorrectAnswers.get() / totalQuestion) * 100 : 0;

        log.info("Total correct answers: {} out of {}. Percentage: {}", totalCorrectAnswers, totalQuestion, percentage);
        // Create final quiz response
        QuizAnswerResponse quizAnswerResponse = new QuizAnswerResponse(
                percentage,
                passingCriteria,
                totalQuestion,
                totalCorrectAnswers.get(),
                totalAttemptQuestion,
                null,
                quiz.getIsAllowedToRetake(),
                "",
                questionAnswerResponses

        );

        Long attemptCount = 1L;
        QuizAttempt quizAttempt = quizAttemptRepository.findByQuizAndUser(quiz, instructorOpt.get());

        if (quizAttempt == null) {
            quizAttempt = new QuizAttempt();
            quizAttempt.setQuiz(quizAttempt.getQuiz() == null ? quizRepository.findById(quiz.getId()).get() : quizAttempt.getQuiz());
            quizAttempt.setUser(instructorOpt.get());
            quizAttempt.setTotalAttemptCount(1L);
        } else {
            quizAttempt.setTotalAttemptCount(quizAttempt.getTotalAttemptCount() + 1);
        }

        quizAttempt.setTotalCorrectAnswer(totalCorrectAnswers.get());
        quizAttempt.setTotalQuestion(totalQuestion);
        quizAttempt.setAttemptedQuestionCount(totalAttemptQuestion);
        quizAttempt.setObtainedPercentage(percentage);
        quizAttempt.setAttemptDate(LocalDate.now());

        quizAttemptRepository.save(quizAttempt);

        if (quizAttempt == null) {
            log.info("quiz attempt detail not save" + instructorOpt.get().getEmail());
        }
        Long quizAttemptId = quizAttempt.getId();

        AtomicReference<String> quizRandomId = new AtomicReference<>("");
        if (generateAIReport.get()) {
            quizRandomId.set(UUID.randomUUID().toString());
            quizAnswerResponse.setQuizAttemptId(quizRandomId.get());
            CompletableFuture<Void> pdfFuture = CompletableFuture.runAsync(() -> {

                QuizAttemptReport report = quizReportService.createPendingReport(quizAttemptId, quizRandomId.get());
                log.info("report is created in DB: {}", report);
                log.info("🚀 Starting async PDF generation for quizAttemptId: {}", quizAttemptId);
                try {
                    byte[] pdfFile;

                    if (personalityTest.get()) {
                        log.info("🧠 Personality test PDF generation started");
                        // Generate PDF only if personalityTest is true
                        pdfFile = excelExportService.exportPdfForPersonalityTest(evaluationResponseList);
                    } else {
                        log.info("📄 Normal quiz PDF generation started");
                        // Optional: handle non-personality test PDF
                        pdfFile = excelExportService.exportToPdf(evaluationResponseList);
                    }
                    log.info("📌 PDF bytes generated: {}", pdfFile.length);
                    MultipartBodyBuilder builder = new MultipartBodyBuilder();
                    builder.part("file", new ByteArrayResource(pdfFile) {
                        @Override
                        public String getFilename() {
                            return "quiz.pdf";
                        }
                    });
                    MultiValueMap<String, HttpEntity<?>> multipartData = builder.build();
                    log.info("☁ Uploading PDF to AI Grader...");
                    String fileId = restClient.makeRequestForAIGraderForMultiPart(
                            "/api/v1/chat_gpt/file-upload",
                            HttpMethod.POST,
                            multipartData,
                            String.class,
                            true
                    );
                    log.info("📎 File Uploaded, fileId = {}", fileId);
                    if (fileId == null) {
                        log.error("❌ PDF file upload failed! fileId is null");
                        quizReportService.updateReportFailed(quizAttemptId);
                        return;
                    }
//            String promptToUse=Prompts.AI_REPORT_PROMPT;
//            if (personalityTest.get()) {
//                // Use a special prompt for personality test
//                promptToUse = Prompts.PERSONALITY_TEST_PROMPT;
//            }else {
//                promptToUse = Prompts.AI_REPORT_PROMPT;
//            }
                    String promptToUse = instructorPrompt == null || instructorPrompt.isBlank() ? Prompts.AI_REPORT_PROMPT : instructorPrompt;
                    ChatRequest chatRequest = ChatRequest.builder()
                            .fileId(fileId)
                            .prompt(promptToUse + Prompts.IMPORTANT_INSTRUCTIONS)
                            .build();
                    if (fileId != null) {
                        log.info("🤖 Requesting AI grader response...");
                        String aiGeneratedHtml = restClient.makeRequestForAIGrader(
                                "/api/v1/chat_gpt/responses",
                                HttpMethod.POST,
                                chatRequest,
                                String.class
                        );
                        String studentName = instructorOpt.get().getFullName();
                        log.info("Student Name: {}", studentName);

                        String html = testReportService.generateReport(studentName, instructorName, aiGeneratedHtml, quizTitle, quizDuration);
                        log.info("html generated successfully: {}", html);
                        // Save report in database
                        quizReportService.updateReportReady(report.getId(), html, " ");
                        log.info("📌 Report update in DB successfully for quizAttemptId: {}", quizAttemptId);
                    } else {
                        // file uploading error
                        quizReportService.updateReportFailed(quizAttemptId);
                        log.error("Failed to upload quiz PDF for quizAttemptId: {}", quizAttemptId);

                    }
                    // Save PDF file to uploads/pdf folder
                    String directoryPath = "uploads/pdf/";
                    Files.createDirectories(Paths.get(directoryPath)); // ensure folder exists

                    String filePath = directoryPath + "quiz_evaluation_" + name.replaceAll("@", "_") + ".pdf";
                    Files.write(Paths.get(filePath), pdfFile);

                    log.info("PDF file generated successfully at: {}", new File(filePath).getAbsolutePath());

                    // Prepare HTTP headers for download (if needed in response)
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDisposition(ContentDisposition.builder("attachment")
                            .filename("quiz_evaluation_" + name.replaceAll("@", "_") + ".pdf")
                            .build());

                } catch (IOException | InternalServerException e) {
                    log.error("Error while generating PDF file: {}", e.getMessage(), e);
                }
            });
        }

        log.info(evaluationResponseList.toString());
        totalQuestion = Long.valueOf(quiz.getRandomQuestion());

//        double percentage = (totalQuestion > 0) ? ((double) totalCorrectAnswers.get() / totalQuestion) * 100 : 0;

        log.info("Total correct answers: {} out of {}. Percentage: {}", totalCorrectAnswers, totalQuestion, percentage);
        // Create final quiz response
//        QuizAnswerResponse quizAnswerResponse = new QuizAnswerResponse(
//                percentage,
//                passingCriteria,
//                totalQuestion,
//                totalCorrectAnswers.get(),
//                totalAttemptQuestion,
//                null,
//                quiz.getIsAllowedToRetake(),
//                quizRandomId,
//                questionAnswerResponses
//        );
        quizAnswerResponse.setQuizAttemptId(quizRandomId.get());

//        Long attemptCount = 1L;
//        QuizAttempt quizAttemptNew = quizAttemptRepository.findByQuizAndUsers(quiz.getId(), instructorOpt.get().getId());
//        if (quizAttemptNew != null) {
//            attemptCount = quizAttemptNew.getTotalAttemptCount() + 1;
//        }
//
//        quizAttemptNew = QuizAttempt.builder()
//                .id(quizAttemptNew == null ? null : quizAttemptNew.getId())
//                .quiz(quiz)
//                .totalCorrectAnswer(totalCorrectAnswers.get())
//                .totalQuestion(totalQuestion)
//                .user(instructorOpt.get())
//                .obtainedPercentage(percentage)
//                .attemptDate(LocalDate.now())
//                .totalAttemptCount(attemptCount)
//                .build();
//        quizAttempt = quizAttemptRepository.save(quizAttemptNew);

        List<QuizAttemptAnswer> attemptAnswers = new ArrayList<>();

        for (ValidationAnswerRequest request : validationAnswerRequests) {
            List<QuizAttemptAnswer> existingAnswers =
                    quizAttemptAnswerRepository.findByQuizAttemptIdAndQuestionId(quizAttemptId, request.getQuestionId());

            com.vinncorp.fast_learner.models.quiz.QuizQuestion quizQuestion =
                    quizRepo.findById(request.getQuestionId())
                            .orElseThrow(() -> new RuntimeException("Question not found"));

            if (!existingAnswers.isEmpty()) {
                // Update all existing answers
                for (QuizAttemptAnswer answer : existingAnswers) {
                    answer.setSelectedAnswerIds(request.getAnswerId());
                    answer.setAnswerText(request.getAnswerText());
                    answer.setIsCorrect(
                            questionAnswerResponses.stream()
                                    .filter(r -> r.getQuestionText().equals(quizQuestion.getQuestionText()))
                                    .findFirst()
                                    .map(QuestionAnswerResponse::isCorrect)
                                    .orElse(false)
                    );
                }
                quizAttemptAnswerRepository.saveAll(existingAnswers);
            } else {
                // No previous answer, create a new one
                QuizAttemptAnswer attemptAnswer = QuizAttemptAnswer.builder()
                        .quizAttempt(quizAttempt)
                        .quizQuestion(quizQuestion)
                        .selectedAnswerIds(request.getAnswerId())
                        .answerText(request.getAnswerText())
                        .isCorrect(
                                questionAnswerResponses.stream()
                                        .filter(r -> r.getQuestionText().equals(quizQuestion.getQuestionText()))
                                        .findFirst()
                                        .map(QuestionAnswerResponse::isCorrect)
                                        .orElse(false)
                        )
                        .build();
                quizAttemptAnswerRepository.save(attemptAnswer);
            }


            if (quizAttempt == null) {
                log.info("quiz attempt detail not save" + instructorOpt.get().getEmail());
            }

            log.info("Returning quiz response with percentage: {}, passing criteria: {}", percentage, passingCriteria);
//        QuizReportResult topicQuizReport = null;
//
//        if (!personalityTest.get()
//                && !quiz.getTestType().equals(TestType.SURVEY)
//                && generateAIReport.get()) {
//
//            topicQuizReport = this.openAIService.fetchQuizTopicReport(quizQuestionReports);
//        }
//        System.out.println(topicQuizReport);


        }
        return new Message<QuizAnswerResponse>()
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setMessage("Quiz Response fetch successfully")
                .setData(quizAnswerResponse);
    }

    @Override
    public Message<QuizQuestionAnswer> fetchPaginatedQuestionAndAnswersByTopicId(Long topicId, boolean forStudent, Boolean random, Pageable pageable, String email) throws EntityNotFoundException {
        User user = this.userService.findByEmail(email);
        QuizQuestionAnswer quizQuestionAnswer = this.fetchAllQuestionAndAnswersByTopicId(topicId, forStudent, random, pageable);
        return new Message<QuizQuestionAnswer>()
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setMessage("Quiz question and answers fetched successfully")
                .setData(quizQuestionAnswer);
    }

    @Override
    public String previewReport(PreviewReport previewReport, String username) throws BadRequestException, InternalServerException {
        String html="";
        try {
            byte[] pdfFile = excelExportService.exportQuizToPdf(previewReport.getQuizQuestionDtos());
            if (pdfFile!=null){
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("file", new ByteArrayResource(pdfFile) {
                    @Override
                    public String getFilename() {
                        return "quiz.pdf";
                    }
                });
                MultiValueMap<String, HttpEntity<?>> multipartData = builder.build();

                String fileId = restClient.makeRequestForAIGraderForMultiPart(
                        "/api/v1/chat_gpt/file-upload",
                        HttpMethod.POST,
                        multipartData,
                        String.class,
                        true // <- important: tells method it's multipart
                );
                if (fileId!=null){
                    String prompt =  previewReport.getReportPrompt() == null || previewReport.getReportPrompt().isBlank()
                            ? Prompts.AI_REPORT_INSTRUCTOR_PROMPT
                            :previewReport.getReportPrompt();
                    //CourseService.appendPrompt() removed this.
                    ChatRequest chatRequest = ChatRequest.builder()
                        .fileId(fileId)
                        .prompt(prompt + Prompts.IMPORTANT_INSTRUCTIONS)
                        .build();

                    String aiGeneratedHtml = restClient.makeRequestForAIGrader(
                        "/api/v1/chat_gpt/responses",
                        HttpMethod.POST,
                        chatRequest,
                        String.class
                    );
                    html = testReportService.generateReport(
                            username,
                            username,
                            aiGeneratedHtml,
                            previewReport.getTopicTitle(),
                            previewReport.getDurationInMinutes()
                    );
                    log.info(html);
                }
            }

        } catch (IOException e) {
            throw new BadRequestException("Failed to generate PDF file");
        }
        return html;
    }

    @Transactional
    public void deleteAnswersByQuestionId(Long questionId) {
        List<QuizQuestionAnwser> answers = repo.findByQuizQuestionId(questionId);
        answers.forEach(repo::delete);
    }
}
