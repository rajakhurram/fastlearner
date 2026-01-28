package com.vinncorp.fast_learner.repositories.course.course_review;

import com.vinncorp.fast_learner.models.course.course_review.CourseReviewLikedDisliked;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseReviewLikedDislikedRepository extends JpaRepository<CourseReviewLikedDisliked, Long> {
    CourseReviewLikedDisliked findByCourseReviewIdAndCreatedBy(Long courseReviewId, Long id);
}
