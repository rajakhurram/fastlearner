package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.dtos.topic.TopicNotesDetail;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.section.UserAlternateSection;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.repositories.topic.TopicNotesRepository;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.section.IUserAlternateSectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.topic.TopicNotes;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.topic.CreateUpdateTopicNotesRequest;
import com.vinncorp.fast_learner.response.topic.TopicNotesResponse;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.PlanType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicNotesService implements ITopicNotesService{

    private final TopicNotesRepository repo;
    private final IEnrollmentService enrollmentService;
    private final ISubscribedUserService subscribedUserService;
    private final ITopicService topicService;
    private final IUserService userService;
    private final IUserAlternateSectionService userAlternateSectionService;

    @Override
    public Message<String> takeNotes(CreateUpdateTopicNotesRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Creating or updating the notes for a topic.");
        if (!enrollmentService.isEnrolled(request.getCourseId(), email)) {
            throw new BadRequestException("You are not enrolled in this course please enroll in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }
        boolean isFree = Objects.nonNull(subscribedUser.getSubscribedId());

        Topic topic = topicService.getTopicById(request.getTopicId());
        if(!topic.getSection().getCourse().getCreatedBy().equals(subscribedUser.getUser().getId())){
            if(topic.getSection().getCourse().getCourseType() != CourseType.PREMIUM_COURSE) {
                if (!topic.getSection().isFree() && !isFree) {
                    throw new BadRequestException("User have to get a paid subscription, this section isn't free.");
                }
            }
        }
        if (!isFree && !topic.getSection().isFree() && !topic.getSection().getCourse().getCourseType().equals(CourseType.PREMIUM_COURSE)) {
            throw new BadRequestException("You doesn't have access to this topic.");
        }

        TopicNotes note = null;
        if (Objects.isNull(request.getTopicNotesId())) {
            note = TopicNotes.builder()
                    .note(request.getNote())
                    .time(request.getTime())
                    .topic(topic)
                    .build();
            note.setCreatedBy(subscribedUser.getUser().getId());
            note.setCreationDate(new Date());
        }else{
            note = repo.findById(request.getTopicNotesId())
                    .orElseThrow(() -> new EntityNotFoundException("Provided topic note id is not valid."));
            note.setNote(request.getNote());
            note.setModifiedBy(subscribedUser.getUser().getId());
            note.setLastModifiedDate(new Date());
        }

        try {
            repo.save(note);
        } catch (Exception e) {
            throw new InternalServerException("Topic note "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully created/update a note.")
                .setData("Successfully created/update a note.");
    }

    @Override
    public Message<TopicNotesResponse> fetchAllTopicNotes(Long courseId, int pageNo, int pageSize, String email) throws BadRequestException, EntityNotFoundException {
        log.info("Fetch all topic notes by course: "+courseId);
        if(Objects.isNull(courseId)){throw new BadRequestException("Course Id cannot be null");}
        User user = userService.findByEmail(email);
        List<Long> courseIds = new ArrayList<>(List.of(courseId));

        UserAlternateSection userAlternateSection = this.userAlternateSectionService.findByCourseId(courseId, user.getId());
        if(Objects.nonNull(userAlternateSection)){
            courseIds.add(userAlternateSection.getFromCourse().getId());
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
        Page<TopicNotes> data = repo.findByTopic_Section_Course_IdInAndCreatedBy(courseIds, user.getId(), PageRequest.of(pageNo, pageSize, sort));
        if (data.isEmpty()) {
            throw new EntityNotFoundException("No topic notes present for the user.");
        }

        List<TopicNotesDetail> topicNotesDetail = TopicNotesDetail.from(data.getContent());

        TopicNotesResponse response = TopicNotesResponse.builder()
                .topicNotes(topicNotesDetail)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .build();

        return new Message<TopicNotesResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched all topic's notes successfully.")
                .setData(response);
    }

    @Override
    public Message<String> deleteTopicNote(Long topicNoteId, Long topicId, Long courseId, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Deleting topic notes by id: "+ topicNoteId);
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enroll in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }
        boolean isFree = subscribedUser.getSubscription().getPlanType() == PlanType.FREE;

        Topic topic = topicService.getTopicById(topicId);
        if (!isFree && !topic.getSection().isFree() && !topic.getSection().getCourse().getCourseType().equals(CourseType.PREMIUM_COURSE)) {
            throw new BadRequestException("You doesn't have access to this topic.");
        }

        try {
            Optional<TopicNotes> topicNotes = repo.findByIdAndCreatedBy(topicNoteId, subscribedUser.getUser().getId());
            if(topicNotes.isPresent()){
                repo.delete(topicNotes.get());
                log.info("Topic note is deleted successfully.");
                return new Message<String>()
                        .setStatus(HttpStatus.OK.value())
                        .setCode(HttpStatus.OK.name())
                        .setMessage("Topic note is deleted successfully.")
                        .setData("Topic note is deleted successfully.");
            }else {
                throw new EntityNotFoundException("Topic note is not found for the user.");
            }
        }
        catch (EntityNotFoundException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new InternalServerException("Topic notes "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

}
