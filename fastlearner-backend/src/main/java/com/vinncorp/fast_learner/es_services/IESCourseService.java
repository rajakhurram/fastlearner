package com.vinncorp.fast_learner.es_services;

import com.vinncorp.fast_learner.es_dto.CourseAutoComplete;
import com.vinncorp.fast_learner.es_models.Course;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;

public interface IESCourseService {
    Message<String> save(Course course) throws InternalServerException;

    Course findByDBId(Long id) throws EntityNotFoundException;

    Message<CourseAutoComplete> autocompleteForCourseSearch(String input) throws EntityNotFoundException;
}
