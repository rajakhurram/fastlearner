package com.vinncorp.fast_learner.controllers.topic;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.topic.AlternativeTopicResponse;
import com.vinncorp.fast_learner.services.topic.IUserAlternateTopicService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.ALTERNATE_TOPIC)
@RequiredArgsConstructor
public class AlternateTopicController {

    private final IUserAlternateTopicService service;

    @GetMapping(APIUrls.GET_ALL_ALTERNATE_TOPICS)
    public ResponseEntity<Message<AlternativeTopicResponse>> getAlternateTopics(
            @RequestParam long courseId,
            @RequestParam long topicId,
            @RequestParam(required = false, defaultValue = "0") int pageNo,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            Principal principal) throws BadRequestException, EntityNotFoundException {
        var m = service.fetchAlternativeTopics(courseId, topicId, pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.PIN_ALTERNATE_TOPIC)
    public ResponseEntity<Message<String>> pinAlternateTopic(
            @RequestParam long courseId,
            @RequestParam long sectionId,
            @RequestParam long fromTopicId,
            @RequestParam long fromCourseId,
            Principal principal
    ) throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = service.pinAlternateTopic(courseId, sectionId, fromTopicId, fromCourseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.UNPIN_ALTERNATE_TOPIC)
    public ResponseEntity<Message<String>> unpinAlternateTopic(
            @RequestParam long courseId,
            @RequestParam long topicId,
            Principal principal) throws EntityNotFoundException, InternalServerException {
        var m = service.unpinAlternateTopic(courseId, topicId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
