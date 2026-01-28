package com.vinncorp.fast_learner.services.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.repositories.section.SectionReviewRepository;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.section.SectionReview;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.request.section.CreateSectionReviewRequest;
import com.vinncorp.fast_learner.response.section.SectionReviewResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionReviewService implements ISectionReviewService {

    private final SectionReviewRepository repo;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;
    private final ISectionService sectionService;
    private final RabbitMQProducer producer;
    private final ICourseUrlService courseUrlService;

    @Override
    public Message<String> createSectionReview(CreateSectionReviewRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Creating section review for user: " + email);
        if (!enrollmentService.isEnrolled(request.getCourseId(), email)) {
            throw new BadRequestException("You are not enrolled in this course please enroll in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        boolean isFree = Objects.nonNull(subscribedUser.getSubscribedId());
        Section section = sectionService.findById(request.getSectionId());
        if (!section.getCourse().getCreatedBy().equals(subscribedUser.getUser().getId())){
            if(section.getCourse().getCourseType() != CourseType.PREMIUM_COURSE) {
                if (!isFree && !section.isFree()) {
                    throw new BadRequestException("User have to get a paid subscription, this section isn't free.");
                }
            }
        }

        SectionReview sectionReview = repo.findBySectionIdAndCreatedBy(request.getSectionId(), subscribedUser.getUser().getId());
        if (Objects.isNull(sectionReview)) {
            sectionReview = new SectionReview();
            sectionReview.setSection(section);
            sectionReview.setComment(request.getComment());
            sectionReview.setRating(request.getValue());
            sectionReview.setCreatedBy(subscribedUser.getUser().getId());
            sectionReview.setCreationDate(new Date());
        } else {
            sectionReview.setComment(request.getComment());
            sectionReview.setRating(request.getValue());
            sectionReview.setModifiedBy(subscribedUser.getUser().getId());
            sectionReview.setLastModifiedDate(new Date());
        }

        try {
            repo.save(sectionReview);
        } catch (Exception e) {
            throw new InternalServerException("Section review " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(request.getCourseId(), GenericStatus.ACTIVE);
        producer.sendMessage(section.getCourse().getTitle(), "/student/course-content/" + courseUrl.getUrl(),
                email.trim().toLowerCase(), section.getCourse().getCreatedBy(), section.getCourse().getContentType(), NotificationContentType.TEXT,
                Objects.isNull(sectionReview.getLastModifiedDate()) ? NotificationType.SECTION_REVIEW : NotificationType.SECTION_REVIEW_UPDATED,
                courseUrl.getCourse().getId());

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Section review is created successfully.")
                .setData("Section review is created successfully.");
    }

    @Override
    public Message<SectionReviewResponse> findBySectionId(Long sectionId, String email) throws BadRequestException, EntityNotFoundException {
        log.info("Fetch section review by section and user.");
        Section section = sectionService.findById(sectionId);
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }
        boolean isFree = Objects.nonNull(subscribedUser.getSubscribedId());
        if(section.getCourse().getCourseType() != CourseType.PREMIUM_COURSE) {
            if (!isFree && !section.isFree()) {
                throw new BadRequestException("You doesn't have permission of this section.");
            }
        }

        SectionReview sectionReview = repo.findBySectionIdAndCreatedBy(sectionId, subscribedUser.getUser().getId());
        if (Objects.isNull(sectionReview)) {
            throw new EntityNotFoundException("Section review is not found for the provided section.");
        }

        long noOfReviews = repo.countBySection_Id(sectionId);

        SectionReviewResponse response = SectionReviewResponse.builder()
                .sectionReviewId(sectionReview.getId())
                .sectionId(sectionId)
                .comment(sectionReview.getComment())
                .value(sectionReview.getRating())
                .totalReviews(noOfReviews)
                .build();

        return new Message<SectionReviewResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("")
                .setData(response);
    }
}
