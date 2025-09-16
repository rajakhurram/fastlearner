package com.vinncorp.fast_learner.services.ai_generator;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import com.vinncorp.fast_learner.services.docs.IDocumentService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.dtos.keywords.KeywordsRequest;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.FileType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class AIGeneratorService implements IAIGeneratorService{

    @Value("${openai.api.key-value}")
    private String OPENAI_KEY;

    @Value("${chat.api.url}")
    private String CHAT_API_URL;

    @Value("${transcript.generation.auth-key}")
    private String TRANSCRIPT_GENERATION_AUTH_KEY;

    private final IVideoService videoService;
    private final IDocumentService documentService;

    @Override
    public Message<String> generateTopic(String input, String email) throws InternalServerException {
        log.info("Generating topics.");

        ChatLanguageModel model = OpenAiChatModel.withApiKey(OPENAI_KEY);
        String response;
        try {
            response = model.generate(input);
        }
        catch (Exception e){
            log.error("ERROR: "+e.getMessage());
            throw new InternalServerException("ERROR: "+e.getLocalizedMessage());
        }

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(response)
                .setMessage("Message successfully fetched out.");
    }

    @Override
    public Message<List<String>> generateKeywords(KeywordsRequest request) throws InternalServerException {
        log.info("Generating keywords from course and topic names");
        String prompt;
        if(Objects.nonNull(request.getCourseTitle())){
            prompt = "Generate only 20 keywords of this course " + request.getCourseTitle() + " in json array form";
        }
        else {
            if (Objects.nonNull(request.getTopicNames()) && !request.getTopicNames().isEmpty()) {
                prompt = "Generate only 20 keywords of section "+request.getSectionTitle()+" with these topics "+request.getTopicNames().toString().replace("[", "").replace("]", "").trim() +" in json array form without headings";

            } else {
                prompt = "Generate only 20 keywords of this section " + request.getSectionTitle() + " in json array form";
            }

        }

        ChatLanguageModel model = OpenAiChatModel.withApiKey(OPENAI_KEY);
        String response;
        try {
            response = model.generate(prompt);
        }
        catch (Exception e){
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("ERROR: "+e.getLocalizedMessage());
        }

        return new Message<List<String>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Keywords Generated successfully")
                .setData(Arrays.asList(response.replaceAll("[\\[\\]\\n\"]+", "").trim().split("\\s*,\\s*")));
    }

    @Override
    public Message<String> regenerateSummary(MultipartFile file, String fileType, String email) throws BadRequestException {
        log.info("Regenerating summary...");
        if(!fileType.equals(FileType.VIDEO.name()) && !fileType.equals(FileType.DOCS.name()))
            throw new BadRequestException("Provided file type is not supported.");
        if (fileType.equals(FileType.VIDEO.name())) {
            VideoTranscript videoTranscript = videoService.generateTranscript(file);
            return new Message<String>()
                    .setData(videoTranscript.getSummary())
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Regenerated the video summary.");
        } else{
            DocumentSummaryResponse documentSummaryResponse = documentService.generateSummary(file);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setData(documentSummaryResponse.getData().getSummary())
                    .setMessage("Regenerated the document summary.");
        }
    }
}
