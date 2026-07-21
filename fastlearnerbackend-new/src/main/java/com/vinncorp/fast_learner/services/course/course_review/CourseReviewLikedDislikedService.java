package com.vinncorp.fast_learner.services.course.course_review;

import com.vinncorp.fast_learner.repositories.course.course_review.CourseReviewLikedDislikedRepository;
import com.vinncorp.fast_learner.models.course.course_review.CourseReviewLikedDisliked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseReviewLikedDislikedService implements ICourseReviewLikedDislikedService{

    private final CourseReviewLikedDislikedRepository repo;

    @Override
    public CourseReviewLikedDisliked getByCourseReviewId(Long courseReviewId, Long userId) {
        log.info("Fetching course review liked disliked data by course review id: " + courseReviewId);
        return repo.findByCourseReviewIdAndCreatedBy(courseReviewId, userId);
    }

    @Override
    public void save(CourseReviewLikedDisliked courseReviewLikedDisliked) {
        if(!Objects.isNull(courseReviewLikedDisliked)){
            log.info("Saving/Updating course review liked disliked.");
            repo.save(courseReviewLikedDisliked);
        }
    }
}
