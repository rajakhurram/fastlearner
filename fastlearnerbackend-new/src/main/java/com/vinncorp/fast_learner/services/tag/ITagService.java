package com.vinncorp.fast_learner.services.tag;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.request.tag.CreateTagRequest;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ITagService {
    Message<List<Tag>> fetchTageByName(String name) throws EntityNotFoundException, BadRequestException;

    void createAllNewAndAlreadyExistsTags(List<CreateTagRequest> tags, Course course) throws BadRequestException;

//    void createAllNewAndAlreadyExistsTags(List<CreateTagRequest> tags, Section section) throws BadRequestException;

    List<Tag> findByCourseId(Long courseId);

    Message<List<Tag>> fetchTagsByCourse(Long courseId, String email) throws EntityNotFoundException;
}
