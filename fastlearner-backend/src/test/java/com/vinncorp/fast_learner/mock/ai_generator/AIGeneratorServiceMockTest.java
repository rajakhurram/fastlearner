package com.vinncorp.fast_learner.mock.ai_generator;

import com.vinncorp.fast_learner.dtos.docs.DocumentSummary;
import com.vinncorp.fast_learner.dtos.keywords.KeywordsRequest;
import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import com.vinncorp.fast_learner.services.ai_generator.AIGeneratorService;
import com.vinncorp.fast_learner.services.docs.IDocumentService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.FileType;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIGeneratorServiceMockTest {

    @InjectMocks
    private AIGeneratorService aiGeneratorService;

    @Mock
    private IVideoService videoService;

    @Mock
    private IDocumentService documentService;

    @Mock
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiGeneratorService, "OPENAI_KEY", "test-key");
    }

    @Test
    @DisplayName("Test generate topic success")
    void testGenerateTopic_Success() throws InternalServerException {
        String input = "Generate AI topics";
        String expectedResponse = "AI, Machine Learning";

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.generate(input)).thenReturn(expectedResponse);

        try (MockedStatic<OpenAiChatModel> mockStatic = mockStatic(OpenAiChatModel.class)) {
            mockStatic.when(() -> OpenAiChatModel.withApiKey("test-key")).thenReturn(mockModel);

            Message<String> result = aiGeneratorService.generateTopic(input, "test@mail.com");

            assertEquals(HttpStatus.OK.value(), result.getStatus());
            assertEquals("Message successfully fetched out.", result.getMessage());
            assertEquals(expectedResponse, result.getData());
        }
    }

    @Test
    @DisplayName("Test generate topic failure")
    void testGenerateTopic_Failure() {
        String input = "Generate topics";

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.generate(input)).thenThrow(new RuntimeException("API down"));

        try (MockedStatic<OpenAiChatModel> mockStatic = mockStatic(OpenAiChatModel.class)) {
            mockStatic.when(() -> OpenAiChatModel.withApiKey("test-key")).thenReturn(mockModel);

            InternalServerException thrown = assertThrows(InternalServerException.class, () ->
                    aiGeneratorService.generateTopic(input, "test@mail.com"));

            assertTrue(thrown.getMessage().contains("API down"));
        }
    }

    @Test
    @DisplayName("Test generate keywords from course title")
    void testGenerateKeywords_FromCourseTitle() throws InternalServerException {
        KeywordsRequest request = new KeywordsRequest();
        request.setCourseTitle("Java Programming");

        String aiResponse = "[Java, OOP, Spring]";

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.generate(anyString())).thenReturn(aiResponse);

        try (MockedStatic<OpenAiChatModel> mockStatic = mockStatic(OpenAiChatModel.class)) {
            mockStatic.when(() -> OpenAiChatModel.withApiKey("test-key")).thenReturn(mockModel);

            Message<List<String>> result = aiGeneratorService.generateKeywords(request);

            assertEquals(HttpStatus.OK.value(), result.getStatus());
            assertTrue(result.getData().contains("Java"));
        }
    }

    @Test
    @DisplayName("Test generate keywords from section and topics")
    void testGenerateKeywords_FromSectionAndTopics() throws InternalServerException {
        KeywordsRequest request = new KeywordsRequest();
        request.setSectionTitle("Basics");
        request.setTopicNames(List.of("Intro", "Syntax"));

        String aiResponse = "[Intro, Syntax, Variables]";

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.generate(anyString())).thenReturn(aiResponse);

        try (MockedStatic<OpenAiChatModel> mockStatic = mockStatic(OpenAiChatModel.class)) {
            mockStatic.when(() -> OpenAiChatModel.withApiKey("test-key")).thenReturn(mockModel);

            Message<List<String>> result = aiGeneratorService.generateKeywords(request);

            assertEquals(3, result.getData().size());
            assertTrue(result.getData().contains("Variables"));
        }
    }

    @Test
    @DisplayName("Test generate keywords exception")
    void testGenerateKeywords_Exception() {
        KeywordsRequest request = new KeywordsRequest();
        request.setCourseTitle("Test");

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.generate(anyString())).thenThrow(new RuntimeException("Failed"));

        try (MockedStatic<OpenAiChatModel> mockStatic = mockStatic(OpenAiChatModel.class)) {
            mockStatic.when(() -> OpenAiChatModel.withApiKey("test-key")).thenReturn(mockModel);

            assertThrows(InternalServerException.class, () -> aiGeneratorService.generateKeywords(request));
        }
    }

    @Test
    @DisplayName("Test regenerate summary video")
    void testRegenerateSummary_Video() throws BadRequestException, BadRequestException {
        VideoTranscript transcript = new VideoTranscript();
        transcript.setSummary("This is a summary.");

        when(videoService.generateTranscript(file)).thenReturn(transcript);

        Message<String> result = aiGeneratorService.regenerateSummary(file, FileType.VIDEO.name(), "test@mail.com");

        assertEquals("This is a summary.", result.getData());
        assertEquals(HttpStatus.OK.value(), result.getStatus());
    }

    @Test
    @DisplayName("Test regenerate summary doc")
    void testRegenerateSummary_Doc() throws BadRequestException {
        DocumentSummaryResponse response = new DocumentSummaryResponse();
        DocumentSummary documentSummary = new DocumentSummary("Doc summary");
        response.setData(documentSummary);
        response.setMessage("Successfully fetch doc summary");

        when(documentService.generateSummary(file)).thenReturn(response);

        Message<String> result = aiGeneratorService.regenerateSummary(file, FileType.DOCS.name(), "test@mail.com");

        assertEquals("Doc summary", result.getData());
    }

    @Test
    @DisplayName("Test regenerate summary invalid file type")
    void testRegenerateSummary_InvalidFileType() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                aiGeneratorService.regenerateSummary(file, "INVALID_TYPE", "test@mail.com"));

        assertEquals("Provided file type is not supported.", ex.getMessage());
    }

}
