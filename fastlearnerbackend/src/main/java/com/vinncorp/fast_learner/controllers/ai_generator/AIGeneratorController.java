package com.vinncorp.fast_learner.controllers.ai_generator;

import com.vinncorp.fast_learner.dtos.keywords.KeywordsRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.ai_generator.IAIGeneratorService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.AI_GENERATOR)
@RequiredArgsConstructor
public class AIGeneratorController {

    private final IAIGeneratorService generatorService;

    @PostMapping(APIUrls.GET_TOPIC_OR_ARTICLE)
    public ResponseEntity<Message<String>> generateTopicOrArticle(@RequestParam String input, Principal principal)
            throws InternalServerException {
        var m = generatorService.generateTopic(input, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.GET_KEYWORDS)
    public ResponseEntity<Message<List<String>>> generateKeywords(@RequestBody KeywordsRequest request) throws InternalServerException {
        Message<List<String>> m = generatorService.generateKeywords(request);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(value = APIUrls.REGENERATE_SUMMARY, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Message<String>> regenerateSummary(@RequestPart("file") MultipartFile file,
                                                             @RequestPart("fileType") String fileType,
                                                             Principal principal) throws BadRequestException {
        var m = generatorService.regenerateSummary(file, fileType, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
