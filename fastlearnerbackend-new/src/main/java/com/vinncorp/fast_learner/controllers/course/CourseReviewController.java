package com.vinncorp.fast_learner.controllers.course;


import com.vinncorp.fast_learner.dtos.course.FeedbackComment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.course.CreateCourseReviewRequest;
import com.vinncorp.fast_learner.response.course.CourseFeedbackResponse;
import com.vinncorp.fast_learner.response.course.CourseReviewResponse;
import com.vinncorp.fast_learner.services.course.course_review.ICourseReviewService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.enums.CourseReviewStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.COURSE_REVIEW_API)
@RequiredArgsConstructor
public class CourseReviewController {

    private final ICourseReviewService service;

    @PostMapping(APIUrls.CREATE_COURSE_REVIEW)
    public ResponseEntity<Message<FeedbackComment>> createReview(@RequestBody CreateCourseReviewRequest request, Principal principal)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        Message<FeedbackComment> m = service.createReview(request, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_COURSE_REVIEW)
    public ResponseEntity<Message<CourseReviewResponse>> findCourseReview(@PathVariable Long courseId, Principal principal)
            throws BadRequestException, EntityNotFoundException {
        Message<CourseReviewResponse> m = service.findByCourseId(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_ALL_REVIEWS_FOR_COURSE)
    public ResponseEntity<Message<CourseFeedbackResponse>> getAllReviewsOfCourses(
            @RequestParam("courseId") Long courseId,
            @RequestParam int pageNo,
            @RequestParam int pageSize, Principal principal) throws EntityNotFoundException {
        Message<CourseFeedbackResponse> m = service.findStudentFeedbackByCourseId(courseId, pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_ALL_REVIEWS_FOR_INSTRUCTOR)
    public ResponseEntity<Message<CourseFeedbackResponse>> getAllReviewsOfCoursesByInstructor(
            @RequestParam(required = false) Long courseId,
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        Message<CourseFeedbackResponse> m = service.fetchStudentFeedbackOnCoursesOfInstructor(principal.getName(), courseId, pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.LIKE_DISLIKE_A_REVIEW)
    public ResponseEntity<Message<FeedbackComment>> likeDislikeReview(
            @Valid @NotNull @PathVariable Long courseReviewId,
            @Valid @NotEmpty @PathVariable String status,
            Principal principal) throws InternalServerException, EntityNotFoundException, BadRequestException {
        var m = service.likeDislikeReview(courseReviewId, CourseReviewStatus.valueOf(status), principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
