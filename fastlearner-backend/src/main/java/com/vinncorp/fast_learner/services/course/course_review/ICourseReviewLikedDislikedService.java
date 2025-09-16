package com.vinncorp.fast_learner.services.course.course_review;

import com.vinncorp.fast_learner.models.course.course_review.CourseReviewLikedDisliked;

public interface ICourseReviewLikedDislikedService {
    CourseReviewLikedDisliked getByCourseReviewId(Long courseReviewId, Long userId);

    void save(CourseReviewLikedDisliked courseReviewLikedDisliked);
}
