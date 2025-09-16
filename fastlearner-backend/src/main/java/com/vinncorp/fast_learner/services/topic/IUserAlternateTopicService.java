package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.topic.AlternativeTopicResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IUserAlternateTopicService {
    Message<AlternativeTopicResponse> fetchAlternativeTopics(long courseId, long topicId, int pageNo, int pageSize, String email)
            throws BadRequestException, EntityNotFoundException;

    Message<String> pinAlternateTopic(long courseId, long sectionId, long fromTopicId, long fromCourseId, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<String> unpinAlternateTopic(long courseId, long topicId, String email) throws EntityNotFoundException, InternalServerException;
}
