package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ICourseCategoryService {
    Message<List<CourseCategory>> fetchAllCourseCategory() throws EntityNotFoundException;

    CourseCategory findById(Long categoryId) throws EntityNotFoundException;
}
