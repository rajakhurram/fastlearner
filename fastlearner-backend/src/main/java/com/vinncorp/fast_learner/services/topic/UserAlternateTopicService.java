package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.dtos.topic.AlternativeTopicDetail;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.repositories.topic.UserAlternateTopicRepository;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.section.ISectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.topic.UserAlternateTopic;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.topic.AlternativeTopicResponse;
import com.vinncorp.fast_learner.services.tag.ITagService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAlternateTopicService implements IUserAlternateTopicService {

    private final UserAlternateTopicRepository repo;

    private final ITopicService topicService;
    private final ITagService tagService;
    private final ISectionService sectionService;
    private final ICourseService courseService;
    private final IUserService userService;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;

    /**
     * Fetching all alternate topic by fuzzy searching with the course's name, tags and topic name.
     * The topics which are already pinned by the user will not appear for the same user for same course.
     *
     * @param courseId
     * @param topicId
     * @param pageNo
     * @param pageSize
     * @param email
     *
     * @return Message<AlternativeTopicResponse>
     *
     * */
    @Override
    public Message<AlternativeTopicResponse> fetchAlternativeTopics(long courseId, long topicId, int pageNo, int pageSize, String email)
            throws BadRequestException, EntityNotFoundException {
        log.info("Fetching alternative topics for user: "+ email);
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }
        Course course = courseService.findById(courseId);
        List<Tag> tags = tagService.findByCourseId(course.getId());
        Topic topic = topicService.getTopicById(topicId);
        String tagStringify = tags.stream().map(Tag::getName).collect(Collectors.joining(" "));
        Page<Tuple> rawData = repo.findAlternativeTopics(courseId, course.getTitle() + " " + tagStringify , topic.getName(),
                subscribedUser.getUser().getId(), PageRequest.of(pageNo, pageSize));
        if (rawData.isEmpty()) {
            throw new EntityNotFoundException("Alternate topics not found.");
        }
        AlternativeTopicResponse response = AlternativeTopicResponse.builder()
                .details(AlternativeTopicDetail.from(rawData.getContent()))
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(rawData.getTotalElements())
                .totalPages(rawData.getTotalPages())
                .build();
        return new Message<AlternativeTopicResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched alternative topics successfully.")
                .setData(response);
    }

    /**
     * Pinning the alternate topic for a specific user in a course
     *
     * @param courseId
     * @param sectionId
     * @param fromTopicId
     * @param fromCourseId
     * @param email
     *
     * @return Message<String>
     * */
    @Override
    public Message<String> pinAlternateTopic(long courseId, long sectionId, long fromTopicId, long fromCourseId, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Pinning the alternate topic.");
        // Check if the topic is already pinned
        if (repo.existsByCourse_IdAndFromTopic_IdAndUser_Email(courseId, fromTopicId, email)) {
            throw new BadRequestException("Already pinned topic cannot be pinned.");
        }
        User user = userService.findByEmail(email);
        Topic fromTopic = topicService.getTopicById(fromTopicId);
        Course course = courseService.findById(courseId);
        Section section = sectionService.findById(sectionId);
        Course fromCourse = courseService.findById(fromCourseId);

        UserAlternateTopic alternateTopic = UserAlternateTopic.builder()
                .course(course)
                .section(section)
                .fromTopic(fromTopic)
                .fromCourse(fromCourse)
                .user(user)
                .build();

        try {
            repo.save(alternateTopic);
            log.info("Alternate topic is pinned.");
        } catch (Exception e) {
            throw new InternalServerException("Users alternate topic "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully pinned the topic.")
                .setData("Successfully pinned the topic.");
    }

    /**
     * Hard delete from the user alternate topic table by the topic id for specific user and course.
     *
     * @param courseId
     * @param topicId
     * @param email
     *
     * @return Message<String>
     * */
    @Override
    public Message<String> unpinAlternateTopic(long courseId, long topicId, String email) throws EntityNotFoundException, InternalServerException {
        log.info("Unpinning the alternate topic.");
        UserAlternateTopic alternateTopic = repo.findByCourse_IdAndFromTopic_IdAndUser_Email(courseId, topicId, email)
                .orElseThrow(() -> new EntityNotFoundException("No alternate topic found with provided id."));
        try {
            repo.delete(alternateTopic);
            log.info("Alternate topic is deleted successfully.");
        } catch (Exception e) {
            throw new InternalServerException("Alternate topic cannot be deleted from db.");
        }
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Alternate topic is deleted successfully.")
                .setData("Alternate topic is deleted successfully.");
    }
}
