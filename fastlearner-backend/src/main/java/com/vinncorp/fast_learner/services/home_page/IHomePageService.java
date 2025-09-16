package com.vinncorp.fast_learner.services.home_page;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.request.homepage.ViewAllReq;
import com.vinncorp.fast_learner.response.course.CourseDetailByPaginatedResponse;
import com.vinncorp.fast_learner.response.course.CourseDetailResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorPaginatedResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorResponse;
import com.vinncorp.fast_learner.util.Message;
import org.apache.poi.ss.formula.functions.T;

import java.security.Principal;
import java.util.List;

public interface IHomePageService {
    Message<CourseDetailByPaginatedResponse> getAllNewCourses(int pageNo, int pageSize, Principal principal);

    Message<CourseDetailByPaginatedResponse> getAllFreeCourses(int pageNo, int pageSize);

    Message<InstructorPaginatedResponse> getTopInstructor(int pageNo, int pageSize);

    Message<CourseDetailByPaginatedResponse> getTrendingCourses(int pageNo, int pageSize,Principal principal);

    Message<CourseDetailByPaginatedResponse>  getAllPremiumCourses(int pageNo, int pageSize, Principal principal);

    Message<CourseDetailByPaginatedResponse> getByType(ViewAllReq viewAllReq, int pageNo, int pageSize, Principal principal) throws EntityNotFoundException;
}
