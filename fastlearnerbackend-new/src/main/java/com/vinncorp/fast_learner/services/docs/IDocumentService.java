package com.vinncorp.fast_learner.services.docs;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.docs.Document;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentService {
    void save(Document document) throws InternalServerException;

    void saveAll(List<Document> documents) throws InternalServerException;

    DocumentSummaryResponse generateSummary(MultipartFile file) throws BadRequestException;

    boolean existsByUrl(String url);

    void deleteById(Long id);
}
