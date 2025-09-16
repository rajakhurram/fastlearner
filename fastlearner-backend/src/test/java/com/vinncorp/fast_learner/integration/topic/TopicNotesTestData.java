package com.vinncorp.fast_learner.integration.topic;

import com.vinncorp.fast_learner.request.topic.CreateUpdateTopicNotesRequest;

public class TopicNotesTestData {

    public static CreateUpdateTopicNotesRequest createUpdateTopicNotesRequest(Long courseId, Long topicId){
        CreateUpdateTopicNotesRequest request = new CreateUpdateTopicNotesRequest();
        request.setCourseId(courseId);
        request.setTopicId(topicId);
        request.setNote("This is a new note.");
        request.setTime("00:05:00");
        return request;
    }

}
