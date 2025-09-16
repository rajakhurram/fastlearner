package com.vinncorp.fast_learner.mock.document;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.docs.DocumentRepository;
import com.vinncorp.fast_learner.services.docs.DocumentService;
import com.vinncorp.fast_learner.models.docs.Document;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DocumentServiceTest {

    @Mock
    private DocumentRepository repo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(documentService,"DOC_SUMMARY_GENERATION_URL", "http://localhost:5000/api/v1/document-summary");
    }

    @Test
    @DisplayName("Document save with valid data")
    void testSave_Success() throws InternalServerException, IOException {
        Document document = DocumentTestData.document();
        when(repo.save(any())).thenReturn(document);

        documentService.save(document);
        verify(repo, times(1)).save(document);
    }

    @Test
    @DisplayName("Document save with database error InternalServerException")
    void testSave_InternalServerException() throws IOException {
        Document document = DocumentTestData.document();

        doThrow(new RuntimeException("Database error")).when(repo).save(document);

        InternalServerException exception = assertThrows(InternalServerException.class, () -> documentService.save(document));
        assertEquals("Document cannot be saved due to database error.", exception.getMessage());
        verify(repo, times(1)).save(document);
    }

    @Test
    @DisplayName("Save multiple document with valid data")
    void testSaveAll_Success() throws InternalServerException, IOException {
        Document document = DocumentTestData.document();
        List<Document> documents = List.of(document);
        when(repo.saveAll(documents)).thenReturn(documents);

        documentService.saveAll(documents);

        verify(repo, times(1)).saveAll(documents);
    }

    @Test
    @DisplayName("Save multiple document when database error")
    void testSaveAll_InternalServerException() throws IOException {
        Document document = DocumentTestData.document();
        List<Document> documents = List.of(document);
        doThrow(new RuntimeException("Database error")).when(repo).saveAll(documents);

        InternalServerException exception = assertThrows(InternalServerException.class, () -> documentService.saveAll(documents));
        assertEquals("Document cannot be saved due to database error.", exception.getMessage());
        verify(repo, times(1)).saveAll(documents);
    }

    @Test
    @DisplayName("Generate summary when provided valid data")
    void testGenerateSummary_Success() throws BadRequestException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getName()).thenReturn("document.pdf");
        when(file.getResource()).thenReturn(mock(org.springframework.core.io.Resource.class));

        DocumentSummaryResponse mockResponse = DocumentTestData.documentSummaryResponse();

        ResponseEntity<DocumentSummaryResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(DocumentSummaryResponse.class)))
                .thenReturn(responseEntity);

        DocumentSummaryResponse response = documentService.generateSummary(file);

        assertNotNull(response);
        assertEquals("Document summary", response.getData().getSummary());

        verify(restTemplate, times(1))
                .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(DocumentSummaryResponse.class));
    }

    @Test
    @DisplayName("Generate summary when invalid data provided")
    void testGenerateSummary_BadRequestException_OnServiceError() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getName()).thenReturn("document.pdf");
        when(file.getResource()).thenReturn(mock(org.springframework.core.io.Resource.class));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file_name", file.getName());
        body.add("file", file.getResource());

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(DocumentSummaryResponse.class)))
                .thenThrow(new RuntimeException("Service error"));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> documentService.generateSummary(file));
        assertTrue(exception.getMessage().contains("Error: Service error"));
    }

    @Test
    @DisplayName("Document exists by url when provided valid data")
    void testExistsByUrl_Exists() throws IOException {
        Document document = DocumentTestData.document();
        when(repo.existsByUrl(document.getUrl())).thenReturn(true);

        boolean exists = documentService.existsByUrl(document.getUrl());

        assertTrue(exists);
        verify(repo, times(1)).existsByUrl(document.getUrl());
    }

    @Test
    @DisplayName("Document exists by url when provided invalid data")
    void testExistsByUrl_DoesNotExist() throws IOException {
        Document document = DocumentTestData.document();
        when(repo.existsByUrl(document.getUrl())).thenReturn(false);

        boolean exists = documentService.existsByUrl(document.getUrl());

        assertFalse(exists);
        verify(repo, times(1)).existsByUrl(document.getUrl());
    }

    @Test
    @DisplayName("Document delete when provided valid id")
    void testDeleteById() throws IOException {
        Document document = DocumentTestData.document();

        documentService.deleteById(document.getId());

        verify(repo, times(1)).deleteById(document.getId());
    }
}
