package com.vinncorp.fast_learner.services.ai_generator;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.keywords.KeywordsRequest;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAIGeneratorService {
    Message<String> generateTopic(String input, String email) throws InternalServerException;

    Message<List<String>> generateKeywords(KeywordsRequest request) throws InternalServerException;

    Message<String> regenerateSummary(MultipartFile file, String fileType, String email) throws BadRequestException;
}
