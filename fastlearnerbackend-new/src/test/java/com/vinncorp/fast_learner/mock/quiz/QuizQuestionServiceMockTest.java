package com.vinncorp.fast_learner.mock.quiz;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.quiz.PreviewReport;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnwserDto;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionDto;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.repositories.quiz.*;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.chat_request.ChatRequest;
import com.vinncorp.fast_learner.request.prompt.Prompts;
import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import com.vinncorp.fast_learner.response.quiz.QuizAnswerResponse;
import com.vinncorp.fast_learner.services.excel.ExcelExportService;
import com.vinncorp.fast_learner.services.quiz.QuizQuestionAnswerService;
import com.vinncorp.fast_learner.services.quiz.QuizQuestionService;
import com.vinncorp.fast_learner.services.quiz.QuizService;
import com.vinncorp.fast_learner.services.reports.TestReportService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import com.vinncorp.fast_learner.util.enums.TestType;
import org.elasticsearch.client.RestClient;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class QuizQuestionServiceMockTest {

    @Mock
    private QuizQuestionRepository repo;

    @Mock
    private QuizQuestionService service;
    @Mock
    private ExcelExportService excelExportService;

    @Mock
    private GenericRestClient restClient;
    @InjectMocks
    private QuizQuestionAnswerService quizQuestionAnswerService;

    @Mock private UserRepository userRepository;
    @Mock private QuizQuestionAnwserRepository quizQuestionAnwserRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private QuizAttemptAnswerRepository quizAttemptAnswerRepository;
    @Mock private QuizRepository quizRepo;
    @Mock private QuizService quizService;
    @Mock
    private CourseRepository courseRepository;
    @Mock private TestReportService testReportService;


    private List<QuizQuestionDto> getSampleQuestions() {
        QuizQuestionAnwserDto ans = new QuizQuestionAnwserDto();
        ans.setAnswer("Answer 1");
        ans.setCorrectAnswer(true);

        QuizQuestionDto question = new QuizQuestionDto();
        question.setQuestionText("What is Java?");
        question.setQuizQuestionAnwsers(List.of(ans));
        question.setExplanation("Java is a programming language.");

        return List.of(question);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    @DisplayName("Test save quiz question - Success")
//    void testSaveQuizQuestion_Success() throws Exception {
//        when(repo.save(QuizTestData.quizQuestionData())).thenReturn(QuizTestData.quizQuestionData());
//        QuizQuestion savedQuizQuestion = service.save(QuizTestData.quizQuestionData());
//        assertNotNull(savedQuizQuestion);
//        assertEquals(QuizTestData.quizQuestionData().getId(), savedQuizQuestion.getId());
//        verify(repo, times(1)).save(QuizTestData.quizQuestionData());
//    }
//
//    @Test
//    @DisplayName("Test save quiz question - Delete existing question")
//    void testSaveQuizQuestion_DeleteExisting() throws Exception {
//        QuizQuestion quizQuestion = QuizTestData.quizQuestionData();
//        quizQuestion.setDelete(true);
//        QuizQuestion result = service.save(quizQuestion);
//        assertNull(result);
//        verify(repo, times(1)).deleteById(quizQuestion.getId());
//        verify(repo, never()).save(any(QuizQuestion.class));
//    }
//
//    @Test
//    @DisplayName("Test save quiz question - Handle repository save error")
//    void testSaveQuizQuestion_HandleSaveError() {
//        when(repo.save(QuizTestData.quizQuestionData())).thenThrow(new RuntimeException("Save error"));
//        InternalServerException exception = assertThrows(InternalServerException.class, () -> service.save(QuizTestData.quizQuestionData()));
//        assertEquals("Quiz question"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
//        verify(repo, times(1)).save(QuizTestData.quizQuestionData());
//    }
//
//    @Test
//    @DisplayName("Test save quiz question - Handle repository delete error")
//    void testSaveQuizQuestion_HandleDeleteError() {
//        QuizQuestion quizQuestion = QuizTestData.quizQuestionData();
//        quizQuestion.setDelete(true);
//        doThrow(new RuntimeException("Delete error")).when(repo).deleteById(quizQuestion.getId());
//
//        InternalServerException exception = assertThrows(InternalServerException.class, () -> service.save(quizQuestion));
//        assertEquals("Quiz question"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
//        verify(repo, times(1)).deleteById(quizQuestion.getId());
//        verify(repo, never()).save(any(QuizQuestion.class));
//    }
//
//    @Test
//    void testPreviewReport_Success() throws Exception {
//        // Arrange
//        PreviewReport previewReport = new PreviewReport();
//        previewReport.setQuizQuestionDtos(List.of()); // empty quiz list
//        previewReport.setReportPrompt("Analyze student results");
//
//        byte[] mockPdf = "fake-pdf-bytes".getBytes();
//
//        when(excelExportService.exportQuizToPdf(anyList())).thenReturn(mockPdf);
//        when(restClient.makeRequestForAIGraderForMultiPart(
//                eq("/api/v1/chat_gpt/file-upload"),
//                eq(HttpMethod.POST),
//                any(MultiValueMap.class),
//                eq(String.class),
//                eq(true))
//        ).thenReturn("file-123");
//
//        when(restClient.makeRequestForAIGrader(
//                eq("/api/v1/chat_gpt/responses"),
//                eq(HttpMethod.POST),
//                any(ChatRequest.class),
//                eq(String.class))
//        ).thenReturn("<html>Generated Report</html>");
//
//        // Act
//        String result = quizQuestionAnswerService.previewReport(previewReport, "Test username");
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("<html>Generated Report</html>", result);
//
//        verify(excelExportService, times(1)).exportQuizToPdf(anyList());
//        verify(restClient, times(1)).makeRequestForAIGraderForMultiPart(
//                anyString(), any(), any(), any(), eq(true));
//        verify(restClient, times(1)).makeRequestForAIGrader(
//                anyString(), any(), any(), (Class<Object>) any());
//    }

    // ---------------------------------------------------------------
    //CASE — FileId returned is null
    // ---------------------------------------------------------------
//    @Test
//    void testPreviewReport_NullFileId() throws Exception {
//        PreviewReport previewReport = new PreviewReport();
//        previewReport.setQuizQuestionDtos(List.of());
//        previewReport.setReportPrompt("Prompt");
//
//        when(excelExportService.exportQuizToPdf(anyList())).thenReturn("fake".getBytes());
//        when(restClient.makeRequestForAIGraderForMultiPart(any(), any(), any(), any(), eq(true)))
//                .thenReturn(null);
//
//        String result = quizQuestionAnswerService.previewReport(previewReport, "Test username");
//
//        assertEquals(null, result); // should return empty html
//    }

    // ---------------------------------------------------------------
    // CASE — PDF bytes are null
    // ---------------------------------------------------------------
//    @Test
//    void testPreviewReport_NullPdfBytes() throws Exception {
//        PreviewReport previewReport = new PreviewReport();
//        previewReport.setQuizQuestionDtos(List.of());
//        previewReport.setReportPrompt("Prompt");
//
//        when(excelExportService.exportQuizToPdf(anyList())).thenReturn(null);
//
//        String result = quizQuestionAnswerService.previewReport(previewReport, "Test username");
//
//        assertEquals(null, result);
//        verify(restClient, never()).makeRequestForAIGraderForMultiPart(any(), any(), any(), any(), anyBoolean());
//    }

    // ---------------------------------------------------------------
    //CASE — Report prompt is null (should use default prompt)
    // ---------------------------------------------------------------
//    @Test
//    void testPreviewReport_NullPrompt_UsesDefaultPrompt() throws Exception {
//        PreviewReport previewReport = new PreviewReport();
//        previewReport.setQuizQuestionDtos(List.of());
//        previewReport.setReportPrompt(null);
//
//        when(excelExportService.exportQuizToPdf(anyList())).thenReturn("pdf".getBytes());
//        when(restClient.makeRequestForAIGraderForMultiPart(any(), any(), any(), any(), eq(true)))
//                .thenReturn("file-xyz");
//        when(restClient.makeRequestForAIGrader(any(), any(), any(), (Class<Object>) any()))
//                .thenReturn("<html>Default Prompt Report</html>");
//
//        String result = quizQuestionAnswerService.previewReport(previewReport, "Test username");
//
//        assertEquals("<html>Default Prompt Report</html>", result);
//
//        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
//        verify(restClient).makeRequestForAIGrader(any(), any(), captor.capture(), (Class<Object>) any());
//
//        ChatRequest chatRequest = captor.getValue();
//        assertEquals(Prompts.AI_REPORT_INSTRUCTOR_PROMPT, chatRequest.getPrompt());
//    }

    // ---------------------------------------------------------------
    //  CASE — PDF generation throws IOException
    // ---------------------------------------------------------------
    @Test
    void testPreviewReport_ThrowsIOException() throws Exception {
        PreviewReport previewReport = new PreviewReport();
        previewReport.setQuizQuestionDtos(List.of());
        previewReport.setReportPrompt("Prompt");

        when(excelExportService.exportQuizToPdf(anyList())).thenThrow(new IOException("PDF error"));

        assertThrows(BadRequestException.class, () -> quizQuestionAnswerService.previewReport(previewReport, "Test username"));
    }

    // ---------------------------------------------------------------
    // CASE — AI response returns null (no HTML content)
    // ---------------------------------------------------------------
    @Test
    void testPreviewReport_NullAIResponse() throws Exception {
        PreviewReport previewReport = new PreviewReport();
        previewReport.setQuizQuestionDtos(List.of());
        previewReport.setReportPrompt("Prompt");

        when(excelExportService.exportQuizToPdf(anyList())).thenReturn("pdf".getBytes());
        when(restClient.makeRequestForAIGraderForMultiPart(any(), any(), any(), any(), eq(true)))
                .thenReturn("file-999");
        when(restClient.makeRequestForAIGrader(any(), any(), any(), (Class<Object>) any()))
                .thenReturn(null);

        String result = quizQuestionAnswerService.previewReport(previewReport, "Test username");
        assertEquals(null, result);
    }


//    @Test
//    @DisplayName("SURVEY: Should validate answers & generate AI report")
//    void testValidateAnswers_Survey_WithAIReport() throws Exception {
//
//        // ------------------ GIVEN ------------------
//        String email = "instructor@test.com";
//
//        User user = new User();
//        user.setId(1L);
//        user.setEmail(email);
//        user.setFullName("Test Instructor");
//
//        Quiz quiz = new Quiz();
//        quiz.setId(10L);
//        quiz.setRandomQuestion(1);
//        quiz.setTestType(TestType.SURVEY);
//        quiz.setGenerateAIReport(true);
//        quiz.setReportPrompt("Survey Prompt");
//        quiz.setTitle("Survey Quiz");
//        quiz.setDurationInMinutes(5);
//
//        QuizQuestion question = new QuizQuestion();
//        question.setId(20L);
//        question.setQuestionText("How do you feel?");
//        question.setQuiz(quiz);
//        question.setQuestionType(QuestionType.SINGLE_CHOICE);
//
//        QuizQuestionAnwser ans1 = new QuizQuestionAnwser();
//        ans1.setId(30L);
//        ans1.setAnswer("Happy");
//        ans1.setCorrectAnswer(false);
//        ans1.setQuizQuestion(question);
//
//        QuizQuestionAnwser ans2 = new QuizQuestionAnwser();
//        ans2.setId(31L);
//        ans2.setAnswer("Sad");
//        ans2.setCorrectAnswer(false);
//        ans2.setQuizQuestion(question);
//
//        ValidationAnswerRequest request = new ValidationAnswerRequest();
//        request.setQuestionId(20L);
//        request.setAnswerId(List.of(30L));
//        request.setCourseId(1L);
//        request.setTimeZone("Asia/Karachi");
//
//
//        // ------------------ MOCKS ------------------
//        // ------------------ MOCKS ------------------
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//        when(repo.findById(20L))
//                .thenReturn(Optional.of(question));
//        when(quizQuestionAnwserRepository.findPassingCriteriaAndQuizIdByQuestionId(20L)).thenReturn(0L);
//        when(quizQuestionAnwserRepository.findByQuizQuestionId(20L)).thenReturn(List.of(ans1, ans2));
//        when(quizService.findByQuizQuestionsId(20L)).thenReturn(quiz);
//        when(quizAttemptRepository.findByQuizAndUsers(10L, 1L)).thenReturn(null);
//        when(quizAttemptRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//
//
//        // PDF + AI mocks
//        when(excelExportService.exportPdfForPersonalityTest(any()))
//                .thenReturn("PDF_BYTES".getBytes());
//
//        when(restClient.makeRequestForAIGraderForMultiPart(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(MultiValueMap.class),
//                eq(String.class),
//                eq(true)))
//                .thenReturn("file-123");
//
//        when(restClient.makeRequestForAIGrader(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(ChatRequest.class),
//                eq(String.class)))
//                .thenReturn("<html>SURVEY REPORT</html>");
//
//        when(testReportService.generateReport(
//                anyString(),
//                anyString(),
//                anyString(),
//                anyString(),
//                anyInt()))
//                .thenReturn("<html>FINAL SURVEY REPORT</html>");
//
//        Course course = new Course();
//        course.setId(1L);
//
//        User courseInstructor = new User();
//        courseInstructor.setFullName("Course Instructor");
//
//        course.setInstructor(courseInstructor);
//
//        when(courseRepository.findById(1L))
//                .thenReturn(Optional.of(course));
//
//        // ------------------ WHEN ------------------
//        Message<QuizAnswerResponse> result =
//                quizQuestionAnswerService.validateAnswers(List.of(request), email);
//
//        // ------------------ THEN ------------------
//        assertEquals(200, result.getStatus());
//        assertNotNull(result.getData());
//
//        QuizAnswerResponse data = result.getData();
//
//        assertEquals(1L, data.getTotalCorrectAnswer());
//        assertEquals(100.0, data.getPercentage());
//
//        verify(excelExportService, times(1))
//                .exportPdfForPersonalityTest(any());
//
//        verify(restClient, times(1))
//                .makeRequestForAIGrader(any(), any(), any(), eq(String.class));
//    }

}
