package com.vinncorp.fast_learner.services.favourite_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.favourite_course.FavouriteCourseResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IFavouriteCourseService {
    Message<String> create(Long courseId, String email) throws EntityNotFoundException, InternalServerException;

    Message<FavouriteCourseResponse> getAllFavouriteCourses(String courseTitle, int pageSize, int pageNo, String email) throws EntityNotFoundException;
}
