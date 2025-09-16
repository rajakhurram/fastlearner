package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.topic.CreateUpdateTopicNotesRequest;
import com.vinncorp.fast_learner.response.topic.TopicNotesResponse;
import com.vinncorp.fast_learner.util.Message;

public interface ITopicNotesService {
    Message<String> takeNotes(CreateUpdateTopicNotesRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<TopicNotesResponse> fetchAllTopicNotes(Long courseId, int pageNo, int pageSize, String email) throws BadRequestException, EntityNotFoundException;

    Message<String> deleteTopicNote(Long topicNoteId, Long topicId, Long courseId, String email) throws BadRequestException, EntityNotFoundException, InternalServerException;
}
