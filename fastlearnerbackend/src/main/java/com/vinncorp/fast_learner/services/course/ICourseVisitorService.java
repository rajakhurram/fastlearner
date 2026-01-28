package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.course.CourseVisitorResponse;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ICourseVisitorService {
    void save(Course course, User user) throws InternalServerException, BadRequestException;

    Message<List<CourseVisitorResponse>> fetchAllVisitors(Long courseId, String email) throws EntityNotFoundException;

    long totalCourseVisit(long courseId);
}
