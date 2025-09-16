package com.vinncorp.fast_learner.controllers.topic;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.topic.CreateUpdateTopicNotesRequest;
import com.vinncorp.fast_learner.response.topic.TopicNotesResponse;
import com.vinncorp.fast_learner.services.topic.ITopicNotesService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.TOPIC_NOTES)
@RequiredArgsConstructor
public class TopicNotesController {

    private final ITopicNotesService service;

    @GetMapping(APIUrls.GET_ALL_TOPIC_NOTES)
    public ResponseEntity<Message<TopicNotesResponse>> getTopicNotes(
            @RequestParam Long courseId, @RequestParam int pageNo,
            @RequestParam int pageSize, Principal principal)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = service.fetchAllTopicNotes(courseId, pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.CREATE_TOPIC_NOTES)
    public ResponseEntity<Message<String>> createTopicNotes(@RequestBody CreateUpdateTopicNotesRequest request, Principal principal)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = service.takeNotes(request, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_TOPIC_NOTES)
    public ResponseEntity<Message<String>> deleteTopicNotes(
            @RequestParam Long topicNoteId,
            @RequestParam Long topicId,
            @RequestParam Long courseId,
            Principal principal)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = service.deleteTopicNote(topicNoteId, topicId, courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
