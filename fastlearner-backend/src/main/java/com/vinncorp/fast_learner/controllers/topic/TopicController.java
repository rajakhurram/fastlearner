package com.vinncorp.fast_learner.controllers.topic;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.topic.TopicDetailForUpdateResponse;
import com.vinncorp.fast_learner.response.topic.TopicDetailResponse;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.TOPIC)
@RequiredArgsConstructor
public class TopicController {

    private final ITopicService service;

    @GetMapping(APIUrls.GET_ALL_TOPIC_BY_COURSE_AND_SECTION)
    public ResponseEntity<Message<List<TopicDetailResponse>>> getAllTopicsBySectionId(
            @PathVariable Long courseId, @PathVariable Long sectionId, Principal principal)
            throws BadRequestException, EntityNotFoundException {
        var m = service.getAllTopicBySection(courseId, sectionId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_SUMMARY)
    public ResponseEntity<Message<String>> getSummaryOfVideoByTopic(@PathVariable("topicId") Long topicId, Principal principal)
            throws BadRequestException, EntityNotFoundException {
        var m = service.getSummaryOfVideoByTopicId(topicId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }


    @GetMapping(APIUrls.GET_ALL_TOPIC_BY_SECTION_FOR_UPDATE)
    public ResponseEntity<Message<List<TopicDetailForUpdateResponse>>> getAllTopicBySectionForUpdate(@PathVariable Long sectionId, Principal principal) throws BadRequestException, EntityNotFoundException {
        var m = service.getAllTopicBySectionForUpdate(sectionId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
