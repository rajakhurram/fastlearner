//package com.vinncorp.fast_learner.mock.aigrader;
//
//import com.vinncorp.fast_learner.config.GenericRestClient;
//import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
//import com.vinncorp.fast_learner.models.subscription.Subscription;
//import com.vinncorp.fast_learner.models.user.User;
//import com.vinncorp.fast_learner.services.ai_grader.ai_result.AIResultService;
//import com.vinncorp.fast_learner.services.excel.ExcelExportService;
//import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
//import com.vinncorp.fast_learner.services.subscription.SubscriptionValidationsService;
//import com.vinncorp.fast_learner.services.user.UserService;
//import com.vinncorp.fast_learner.util.InMemoryMultipartFile;
//import com.vinncorp.fast_learner.util.Message;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//@ExtendWith(MockitoExtension.class)
//public class AIResultServiceTest {
//    @InjectMocks
//    private AIResultService aiResultService;
//
//    @Mock
//    private ExcelExportService excelExportService;
//
//    @Mock
//    private SubscribedUserService subscribedUserService;
//
//    @Mock
//    private GenericRestClient restClient;
//
//    @Mock
//    private SubscriptionValidationsService subscriptionValidationsService;
//
//    @Mock
//    private UserService userService;
//
//    private SubscribedUser mockSubscribedUser() {
//        User user = new User();
//        user.setId(1L);
//        user.setNoOfPagesUsed(5);
//
//        Subscription subscription = Subscription.builder()
//                .id(1L)
//                .content("Access to all standard subscription courses")
//                .name("Premium Instructor Plan")
//                .isActive(true)
//                .build();
//
//        SubscribedUser subscribedUser = new SubscribedUser();
//        subscribedUser.setUser(user);
//        subscribedUser.setSubscription(subscription);
//
//        return subscribedUser;
//    }
//
//    @Test
//    void shouldStartGradingWhenQuizFilesProvided() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//        User user = subscribedUser.getUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
//
//        Mockito.verify(userService).save(user);
//        Mockito.verify(subscribedUserService).findByUser("test@mail.com");
//    }
//
//    @Test
//    void shouldStartGradingWhenMultipleQuizFilesProvided() throws Exception {
//        MultipartFile quizFile1 = new MockMultipartFile(
//                "file1", "quiz1.pdf", "application/pdf", "data1".getBytes()
//        );
//        MultipartFile quizFile2 = new MockMultipartFile(
//                "file2", "quiz2.pdf", "application/pdf", "data2".getBytes()
//        );
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//        User user = subscribedUser.getUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                List.of(quizFile1, quizFile2),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Mockito.verify(userService).save(user);
//    }
//
//    @Test
//    void shouldStartGradingWhenAnswerKeyFileProvided() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "quiz", "quiz.pdf", "application/pdf", "quiz data".getBytes()
//        );
//        MultipartFile answerKeyFile = new MockMultipartFile(
//                "answer", "answer.pdf", "application/pdf", "answer data".getBytes()
//        );
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//        User user = subscribedUser.getUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                answerKeyFile,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Mockito.verify(userService).save(user);
//    }
//
//    @Test
//    void shouldStartGradingWhenUserSubmittedAnswerAsTextProvided() throws Exception {
//        String userAnswerText = "This is the student's submitted answer in text format.";
//        byte[] pdfBytes = "converted pdf".getBytes();
//
//        InMemoryMultipartFile convertedFile = new InMemoryMultipartFile(
//                pdfBytes, "student.pdf", "original-file-name.pdf", "application/pdf"
//        );
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//        User user = subscribedUser.getUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//        Mockito.when(excelExportService.convertTextToPdf(userAnswerText)).thenReturn(pdfBytes);
//        Mockito.when(excelExportService.convertBytesToMultipartFile(
//                pdfBytes, "student.pdf", "application/pdf"
//        )).thenReturn(convertedFile);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                null,
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                userAnswerText,
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
//
//        Mockito.verify(excelExportService).convertTextToPdf(userAnswerText);
//        Mockito.verify(excelExportService).convertBytesToMultipartFile(
//                pdfBytes, "student.pdf", "application/pdf"
//        );
//        Mockito.verify(userService).save(user);
//    }
//
//    @Test
//    void shouldNotStartGradingWhenTextSubmittedIsBlank() throws Exception {
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                null,
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                "   ",
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
//
//        Mockito.verify(excelExportService, Mockito.never()).convertTextToPdf(Mockito.anyString());
//        Mockito.verify(excelExportService, Mockito.never()).convertBytesToMultipartFile(
//                Mockito.any(), Mockito.anyString(), Mockito.anyString()
//        );
//    }
//
//    @Test
//    void shouldUpdatePageCountCorrectlyAfterSuccessfulGrading() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//        User user = subscribedUser.getUser();
//        int initialPageCount = user.getNoOfPagesUsed();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
//        Mockito.verify(userService).save(userCaptor.capture());
//
//        User savedUser = userCaptor.getValue();
//        Assertions.assertTrue(savedUser.getNoOfPagesUsed() >= initialPageCount);
//    }
//
//    @Test
//    void shouldUpdatePageCountFromNullToCorrectValue() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//        User user = subscribedUser.getUser();
//        user.setNoOfPagesUsed(null);
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
//        Mockito.verify(userService).save(userCaptor.capture());
//
//        User savedUser = userCaptor.getValue();
//        Assertions.assertNotNull(savedUser.getNoOfPagesUsed());
//        Assertions.assertTrue(savedUser.getNoOfPagesUsed() >= 0);
//    }
//
//    @Test
//    void shouldNotUpdatePageCountWhenResponseIsNotOK() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//
//        Message<String> failureResponse = new Message<>();
//        failureResponse.setStatus(HttpStatus.BAD_REQUEST.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(failureResponse);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
//        Mockito.verify(userService, Mockito.never()).save(Mockito.any(User.class));
//    }
//
//    @Test
//    void shouldNotUpdatePageCountWhenResponseIsNull() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(null);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        Assertions.assertNull(response);
//        Mockito.verify(userService, Mockito.never()).save(Mockito.any(User.class));
//    }
//
//    @Test
//    void shouldReturnInternalServerErrorWhenIOExceptionOccurs() throws Exception {
//        MultipartFile quizFile = Mockito.mock(MultipartFile.class);
//
////        Mockito.when(quizFile.getOriginalFilename()).thenReturn("quiz.pdf");
//        Mockito.when(quizFile.getInputStream()).thenThrow(new IOException("File read error"));
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
//        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), response.getCode());
//        Assertions.assertEquals("File processing error.", response.getMessage());
//
//        Mockito.verify(userService, Mockito.never()).save(Mockito.any(User.class));
//    }
//
//    @Test
//    void shouldThrowRuntimeExceptionWhenUnexpectedErrorOccurs() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenThrow(new RuntimeException("Unexpected error"));
//
//        Assertions.assertThrows(RuntimeException.class, () -> {
//            aiResultService.startGradingLandingPage(
//                    List.of(quizFile),
//                    null,
//                    "criteria",
//                    "assessment",
//                    "class",
//                    null,
//                    "test@mail.com"
//            );
//        });
//
//        Mockito.verify(userService, Mockito.never()).save(Mockito.any(User.class));
//    }
//
//    @Test
//    void shouldIncludeAllParametersInRequestBody() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//        MultipartFile answerKeyFile = new MockMultipartFile(
//                "answer", "answer.pdf", "application/pdf", "answer data".getBytes()
//        );
//
//        String evaluationCriteria = "Test criteria";
//        String assessmentName = "Mid-term Exam";
//        String className = "Math 101";
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        Message<String> response = aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                answerKeyFile,
//                evaluationCriteria,
//                assessmentName,
//                className,
//                null,
//                "test@mail.com"
//        );
//
//        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
//
//        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
//        Mockito.verify(restClient).makeRequestForAIGrader(
//                Mockito.eq("/api/v1/grader/create-landing-page"),
//                Mockito.eq(HttpMethod.POST),
//                entityCaptor.capture(),
//                Mockito.eq(Message.class)
//        );
//
//        HttpEntity<MultiValueMap<String, Object>> capturedEntity = entityCaptor.getValue();
//        MultiValueMap<String, Object> body = capturedEntity.getBody();
//
//        Assertions.assertNotNull(body);
//        Assertions.assertTrue(body.containsKey("quiz_files"));
//        Assertions.assertTrue(body.containsKey("answer_key_file"));
//        Assertions.assertTrue(body.containsKey("evaluationCriteria"));
//        Assertions.assertTrue(body.containsKey("assessmentName"));
//        Assertions.assertTrue(body.containsKey("className"));
//        Assertions.assertTrue(body.containsKey("instructorId"));
//
//        Assertions.assertEquals(evaluationCriteria, body.getFirst("evaluationCriteria"));
//        Assertions.assertEquals(assessmentName, body.getFirst("assessmentName"));
//        Assertions.assertEquals(className, body.getFirst("className"));
//        Assertions.assertEquals(1L, body.getFirst("instructorId"));
//    }
//
//    @Test
//    void shouldSetCorrectContentTypeHeader() throws Exception {
//        MultipartFile quizFile = new MockMultipartFile(
//                "file", "quiz.pdf", "application/pdf", "data".getBytes()
//        );
//
//        Message<String> successResponse = new Message<>();
//        successResponse.setStatus(HttpStatus.OK.value());
//
//        SubscribedUser subscribedUser = mockSubscribedUser();
//
//        Mockito.when(subscribedUserService.findByUser("test@mail.com")).thenReturn(subscribedUser);
//
//        Mockito.when(restClient.makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                Mockito.any(HttpEntity.class),
//                Mockito.eq(Message.class)
//        )).thenReturn(successResponse);
//
//        aiResultService.startGradingLandingPage(
//                List.of(quizFile),
//                null,
//                "criteria",
//                "assessment",
//                "class",
//                null,
//                "test@mail.com"
//        );
//
//        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
//        Mockito.verify(restClient).makeRequestForAIGrader(
//                Mockito.anyString(),
//                Mockito.eq(HttpMethod.POST),
//                entityCaptor.capture(),
//                Mockito.eq(Message.class)
//        );
//
//        HttpEntity capturedEntity = entityCaptor.getValue();
//        Assertions.assertEquals(MediaType.MULTIPART_FORM_DATA, capturedEntity.getHeaders().getContentType());
//    }
//}