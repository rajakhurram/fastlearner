package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.repositories.topic.TopicTypeRepository;
import com.vinncorp.fast_learner.models.topic.TopicType;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicTypeService implements ITopicTypeService{

    private final TopicTypeRepository repo;

    @Override
    public Message<List<TopicType>> fetchAllTopicType(String email) throws EntityNotFoundException {
        log.info("Fetching all topic type.");
        List<TopicType> topicTypes = repo.findByIsActive(true);
        if (CollectionUtils.isEmpty(topicTypes)) {
            log.error("No topic type found.");
            throw new EntityNotFoundException("No topic type found.");
        }
        log.info("All topic type is fetched successfully.");
        return new Message<List<TopicType>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("All topic type is fetched successfully.")
                .setData(topicTypes);
    }

    @Override
    public TopicType findById(Long topicTypeId) throws EntityNotFoundException {
        log.info("Fetching topic type by id: "+topicTypeId);
        return repo.findById(topicTypeId).orElseThrow(() -> {
            log.error("Topic type is not found.");
            return new EntityNotFoundException("Topic type is not found.");
        });
    }

    @Override
    public List<TopicType> findAllTopicType() {
        return repo.findByIsActive(true);
    }
}
