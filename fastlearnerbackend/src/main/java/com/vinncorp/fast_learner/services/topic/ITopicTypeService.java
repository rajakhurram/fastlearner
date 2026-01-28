package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.topic.TopicType;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ITopicTypeService {
    Message<List<TopicType>> fetchAllTopicType(String email) throws EntityNotFoundException;

    TopicType findById(Long topicTypeId) throws EntityNotFoundException;

    List<TopicType> findAllTopicType();
}
