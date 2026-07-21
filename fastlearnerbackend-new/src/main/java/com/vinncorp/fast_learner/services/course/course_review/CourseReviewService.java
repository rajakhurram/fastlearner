package com.vinncorp.fast_learner.services.course.course_review;

import com.vinncorp.fast_learner.dtos.course.CourseFeedback;
import com.vinncorp.fast_learner.dtos.course.FeedbackComment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.repositories.course.course_review.CourseReviewRepository;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.notification.IStudentQnADiscussion;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.course.course_review.CourseReview;
import com.vinncorp.fast_learner.models.course.course_review.CourseReviewLikedDisliked;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.request.course.CreateCourseReviewRequest;
import com.vinncorp.fast_learner.response.course.CourseFeedbackResponse;
import com.vinncorp.fast_learner.response.course.CourseReviewResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseReviewStatus;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseReviewService implements ICourseReviewService {

    private final IUserService userService;
    private final CourseReviewRepository repo;
    private final ICourseService courseService;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;
    private final RabbitMQProducer producer;
    private final ICourseReviewLikedDislikedService courseReviewLikedDislikedService;
    private final IStudentQnADiscussion studentQnADiscussion;
    private final ICourseUrlService courseUrlService;


    @Transactional
    @Override
    public Message<FeedbackComment> createReview(CreateCourseReviewRequest request, String email) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("User: " + email + " is giving the course review.");
        if (!enrollmentService.isEnrolled(request.getCourseId(), email)) {
            throw new BadRequestException("You are not enrolled in this course please enroll in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        Course course = courseService.findById(request.getCourseId());

        // Check whether the course review already exists for the user
        CourseReview review = repo.findByCreatedByAndCourseId(subscribedUser.getUser().getId(), course.getId());
        if (Objects.isNull(review)) {
            review = CourseReview.builder()
                    .course(course)
                    .comment(request.getComment())
                    .rating(request.getValue())
                    .build();
            review.setCreatedBy(subscribedUser.getUser().getId());
            review.setCreationDate(new Date());
        } else {
            review.setLastModifiedDate(new Date());
            review.setModifiedBy(subscribedUser.getUser().getId());
            review.setRating(request.getValue());
            review.setComment(request.getComment());
        }

        try {
            review = repo.save(review);
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("Course review " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);

        producer.sendMessage(course.getTitle(), "student/course-content/" + courseUrl.getUrl(), email.trim().toLowerCase(),
                course.getCreatedBy(), course.getContentType(), NotificationContentType.TEXT,
                Objects.isNull(review.getLastModifiedDate()) ? NotificationType.COURSE_REVIEW : NotificationType.COURSE_REVIEW_UPDATED,
                course.getId());

        return new Message<FeedbackComment>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Review is given successfully.")
                .setData(FeedbackComment.from(this.repo.findByReviewId(review.getId())));
    }

    @Override
    public Message<CourseReviewResponse> findByCourseId(Long courseId, String email) throws BadRequestException, EntityNotFoundException {
        log.info("Fetching a course review by course and user.");
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enroll in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        CourseReview courseReview = repo.findByCreatedByAndCourseId(subscribedUser.getUser().getId(), courseId);
        if (Objects.isNull(courseReview)) {
            throw new EntityNotFoundException("Course review is not found for this course by user.");
        }

        long noOfReviews = repo.countByCourse_Id(courseId);

        CourseReviewResponse response = CourseReviewResponse.builder()
                .courseReviewId(courseReview.getId())
                .courseId(courseId)
                .comment(courseReview.getComment())
                .value(courseReview.getRating())
                .totalReviews(noOfReviews)
                .build();

        return new Message<CourseReviewResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Course review fetched successfully.")
                .setData(response);
    }

    /**
     * Fetching all the feedback of users as shown in the course's FEEDBACK section
     */
    @Override
    public Message<CourseFeedbackResponse> findStudentFeedbackByCourseId(Long courseId, int pageNo, int pageSize) throws EntityNotFoundException {
        log.info("Fetching feedback for a course.");

        List<Tuple> rawData = repo.findStudentFeedbackByCourseId(courseId);
        if (rawData.isEmpty()) {
            throw new EntityNotFoundException("No feedback is found for the course.");
        }

        // Fetching all reviews by users
        Page<Tuple> data = repo.findByCourseId(courseId, PageRequest.of(pageNo, pageSize));

        CourseFeedback feedback = CourseFeedback.toOnlyRating(rawData, data.getTotalElements());
        feedback.setFeedbackComments(FeedbackComment.from(data.getContent()));

        return new Message<CourseFeedbackResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully fetched course feedback.")
                .setData(CourseFeedbackResponse.builder()
                        .feedback(feedback)
                        .totalElements(data.getTotalElements())
                        .totalPages(data.getTotalPages())
                        .pageNo(pageNo)
                        .pageSize(pageSize)
                        .build());
    }

    /**
     * Fetching all reviews by the instructor not only by the courses
     */
    @Override
    public Message<CourseFeedbackResponse> fetchStudentFeedbackOnCoursesOfInstructor(String email, Long courseId, int pageNo, int pageSize) throws EntityNotFoundException, BadRequestException {
        log.info("Fetching all reviews on an instructor courses.");

        if(pageNo < 0 || pageSize < 0){
            throw new BadRequestException("Page no or Page size cannot be negative.");
        }
        User instructor = userService.findByEmail(email);
        List<Tuple> reviewsByPercentage = null;
        if(Objects.isNull(courseId))
            reviewsByPercentage = repo.findStudentFeedbackForCoursesOfInstructor(instructor.getId());
        else
            reviewsByPercentage = repo.findStudentFeedbackByCourseId(courseId);

        if (CollectionUtils.isEmpty(reviewsByPercentage)) {
            throw new EntityNotFoundException("No feedback is found for the course.");
        }

        Page<Tuple> data = repo.findAllCoursesReviewsOfAnInstructor(instructor.getId(), courseId, PageRequest.of(pageNo, pageSize));

        CourseFeedback feedback = CourseFeedback.toOnlyRating(reviewsByPercentage, data.getTotalElements());
        feedback.setFeedbackComments(FeedbackComment.from(data.getContent()));
        return new Message<CourseFeedbackResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully fetched course feedback.")
                .setData(CourseFeedbackResponse.builder()
                        .feedback(feedback)
                        .totalElements(data.getTotalElements())
                        .totalPages(data.getTotalPages())
                        .pageNo(pageNo)
                        .pageSize(pageSize)
                        .build());
    }

    @Override
    public Tuple fetchAllReviewsForAnInstructorCourses(Long instructorId) {
        log.info("Fetching all reviews of an instructor's courses...");
        return repo.findAllCoursesReviewsOfAnInstructor(instructorId);
    }

    @Override
    public Tuple fetchAllReviewOfACourse(Long courseId) {
        log.info("Fetching all reviews of a course.");
        return repo.findStudentReviewsByCourseId(courseId);
    }

    @Override
    public Message<FeedbackComment> likeDislikeReview(Long courseReviewId, CourseReviewStatus status, String email) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Liking or disliking a review.");
        User user = userService.findByEmail(email);
        String message = null;
        var courseReviewLikedDisliked = courseReviewLikedDislikedService.getByCourseReviewId(courseReviewId, user.getId());
        validateCourseReviewLikedDislikedWithProvidedStatus(courseReviewLikedDisliked, status);
        CourseReview courseReview = repo.findById(courseReviewId)
                .orElseThrow(() -> new EntityNotFoundException("Course review not found with the provided course review id."));
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(courseReview.getCourse().getId(), GenericStatus.ACTIVE);

        if(CourseReviewStatus.LIKED == status) {
            courseReview.setLikes(1 + courseReview.getLikes());
            if(Objects.nonNull(courseReviewLikedDisliked) && courseReviewLikedDisliked.getStatus() == CourseReviewStatus.DISLIKED)
                courseReview.setDislikes(courseReview.getDislikes() > 0 ? courseReview.getDislikes() - 1 : 0);
            message = "Course review is liked successfully.";
            studentQnADiscussion.notifyToUserLikeDislikedReview(courseUrl.getUrl(), user.getFullName(), CourseReviewStatus.LIKED.name(),
                    courseReview.getCourse(), courseReview.getCreatedBy(), user.getId());

        } else {
            courseReview.setDislikes(1 + courseReview.getDislikes());
            if (Objects.nonNull(courseReviewLikedDisliked) && courseReviewLikedDisliked.getStatus() == CourseReviewStatus.LIKED)
                courseReview.setLikes(courseReview.getLikes() > 0 ? courseReview.getLikes() - 1 : 0);
            message = "Course review is disliked successfully.";
            studentQnADiscussion.notifyToUserLikeDislikedReview(courseUrl.getUrl(), user.getFullName(), CourseReviewStatus.DISLIKED.name(),
                    courseReview.getCourse(), courseReview.getCreatedBy(), user.getId());
        }
        try {
            if(Objects.isNull(courseReviewLikedDisliked)){
                courseReviewLikedDisliked = new CourseReviewLikedDisliked();
                courseReviewLikedDisliked.setCourseReviewId(courseReviewId);
                courseReviewLikedDisliked.setStatus(status);
                courseReviewLikedDisliked.setCreatedAt(new Date());
                courseReviewLikedDisliked.setCreatedBy(user.getId());
            }else {
                courseReviewLikedDisliked.setStatus(status);
                courseReviewLikedDisliked.setCreatedAt(new Date());
            }
            courseReviewLikedDislikedService.save(courseReviewLikedDisliked);
            courseReview = repo.save(courseReview);
        } catch (Exception e) {
            throw new InternalServerException("Course review like or dislike status cannot updated.");
        }

        return new Message<FeedbackComment>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage(message)
                .setData(FeedbackComment.from(this.repo.findByReviewId(courseReview.getId())));
    }

    private void validateCourseReviewLikedDislikedWithProvidedStatus(CourseReviewLikedDisliked courseReviewLikedDisliked, CourseReviewStatus status) throws BadRequestException {
        log.info("Validating already having the provided status.");
        if(Objects.nonNull(courseReviewLikedDisliked)){
            if(courseReviewLikedDisliked.getStatus() == status){
                throw new BadRequestException("This user already "+status.name()+" the course review.");
            }
        }
    }
}
