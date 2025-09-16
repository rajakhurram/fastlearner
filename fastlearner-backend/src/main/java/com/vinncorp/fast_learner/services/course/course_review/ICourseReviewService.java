package com.vinncorp.fast_learner.services.course.course_review;

import com.vinncorp.fast_learner.dtos.course.FeedbackComment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.course.CreateCourseReviewRequest;
import com.vinncorp.fast_learner.response.course.CourseFeedbackResponse;
import com.vinncorp.fast_learner.response.course.CourseReviewResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseReviewStatus;
import jakarta.persistence.Tuple;

public interface ICourseReviewService {
    Message<FeedbackComment> createReview(CreateCourseReviewRequest request, String email) throws EntityNotFoundException, InternalServerException, BadRequestException;

    Message<CourseReviewResponse> findByCourseId(Long courseId, String email) throws BadRequestException, EntityNotFoundException;

    Message<CourseFeedbackResponse> findStudentFeedbackByCourseId(Long courseId, int pageNo, int pageSize) throws EntityNotFoundException;

    Message<CourseFeedbackResponse> fetchStudentFeedbackOnCoursesOfInstructor(String email, Long courseId, int pageNo, int pageSize) throws EntityNotFoundException, BadRequestException;

    Tuple fetchAllReviewsForAnInstructorCourses(Long instructorId);

    Tuple fetchAllReviewOfACourse(Long courseId);

    Message<FeedbackComment> likeDislikeReview(Long courseReviewId, CourseReviewStatus status, String email) throws EntityNotFoundException, InternalServerException, BadRequestException;
}
