package com.vinncorp.fast_learner.controllers.topic;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.topic.TopicType;
import com.vinncorp.fast_learner.services.topic.ITopicTypeService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.TOPIC_TYPE_API)
@RequiredArgsConstructor
public class TopicTypeController {

    private final ITopicTypeService service;

    @GetMapping(APIUrls.GET_ALL_TOPIC_TYPE)
    public ResponseEntity<Message<List<TopicType>>> fetchAllTopicType(Principal principal) throws EntityNotFoundException {
        Message<List<TopicType>> m = service.fetchAllTopicType(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
