package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.dtos.topic.NoOfTopicInCourse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.response.topic.TopicDetailForUpdateResponse;
import com.vinncorp.fast_learner.response.topic.TopicDetailResponse;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ITopicService {
    Message<String> deleteTopicById(Long id) throws InternalServerException;

    Topic save(Topic build) throws InternalServerException;

    List<NoOfTopicInCourse> getAllTopicByCourses(List<Long> courseIdList) throws EntityNotFoundException;

    Topic getTopicById(Long topicId) throws EntityNotFoundException;

    Message<List<TopicDetailResponse>> getAllTopicBySection(Long courseId, Long sectionId, String email) throws BadRequestException, EntityNotFoundException;

    Message<String> getSummaryOfVideoByTopicId(Long topicId, String email) throws EntityNotFoundException, BadRequestException;


    Message<List<TopicDetailForUpdateResponse>> getAllTopicBySectionForUpdate(Long sectionId, String email) throws EntityNotFoundException, BadRequestException;

    List<Topic> fetchAllTopicsBySectionId(Long sectionId) throws EntityNotFoundException;
}
