package com.vinncorp.fast_learner.services.docs;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.docs.DocumentRepository;
import com.vinncorp.fast_learner.models.docs.Document;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService implements IDocumentService{

    @Value("${document.summary.url}")
    private String DOC_SUMMARY_GENERATION_URL;

    @Value("${transcript.generation.auth-key}")
    private String TRANSCRIPT_GENERATION_AUTH_KEY;

    private final RestTemplate restTemplate;
    private final DocumentRepository repo;

    @Override
    public void save(Document document) throws InternalServerException {
        log.info("Saving documents.");
        try {
            repo.save(document);
        } catch (Exception e) {
            throw new InternalServerException("Document "+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Override
    public void saveAll(List<Document> documents) throws InternalServerException {
        log.info("Saving all documents.");
        try{
            repo.saveAll(documents);
        } catch (Exception e) {
            throw new InternalServerException("Document "+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public DocumentSummaryResponse generateSummary(MultipartFile file) throws BadRequestException {
        log.info("Generating document summary for a pdf file.");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", TRANSCRIPT_GENERATION_AUTH_KEY);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file_name", file.getName());
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // Perform the file upload
            log.info("Calling document summary generation service.");
            ResponseEntity<DocumentSummaryResponse> response = restTemplate.exchange(DOC_SUMMARY_GENERATION_URL, HttpMethod.POST, request, DocumentSummaryResponse.class);
            log.info("Successfully called document summary generation service.");
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Summary generated successfully.");
                return response.getBody();
            }
        } catch (Exception e) {
            throw new BadRequestException("Error: "+e.getLocalizedMessage());
        }
        throw new BadRequestException("Summary generation is failed.");
    }

    @Override
    public boolean existsByUrl(String url) {
        return repo.existsByUrl(url);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
