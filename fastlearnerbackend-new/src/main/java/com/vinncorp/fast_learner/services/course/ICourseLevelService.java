package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseLevel;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ICourseLevelService {
    Message<List<CourseLevel>> fetchAllCourseLevel(String email) throws EntityNotFoundException;

    CourseLevel findById(Long courseLevelId) throws EntityNotFoundException;
}
