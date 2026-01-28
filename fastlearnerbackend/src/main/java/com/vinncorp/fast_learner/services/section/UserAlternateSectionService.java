package com.vinncorp.fast_learner.services.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.section.UserAlternateSection;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.repositories.section.UserAlternateSectionRepository;
import com.vinncorp.fast_learner.response.section.AlternateSectionResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.tag.ITagService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.dtos.section.AlternateSectionDetail;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAlternateSectionService implements IUserAlternateSectionService{

    private final UserAlternateSectionRepository repo;

    private final ITagService tagService;
    private final ISectionService sectionService;
    private final ICourseService courseService;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;
    private final IUserService userService;
    private final IUserCourseProgressService courseProgressService;

    @Override
    public Message<AlternateSectionResponse> fetchAlternateSection(Long courseId, Long sectionId, int pageNo, int pageSize, String email)
            throws BadRequestException, EntityNotFoundException {
        log.info("Fetching alternate section for user: "+ email);
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        Course course = courseService.findById(courseId);
        List<Tag> tags = tagService.findByCourseId(course.getId());
        Section section = sectionService.findById(sectionId);
        String tagStringify = tags.stream().map(Tag::getName).collect(Collectors.joining(" "));

        Page<Tuple> rawData = repo.findAlternateSections(courseId,
                course.getTitle() + " " + course.getCourseCategory().getName() + " " + tagStringify ,
                section.getName(), subscribedUser.getUser().getId(), PageRequest.of(pageNo, pageSize));

        if (rawData.isEmpty()) {
            throw new EntityNotFoundException("Alternate section not found.");
        }

        AlternateSectionResponse response = AlternateSectionResponse.builder()
                .details(AlternateSectionDetail.from(rawData.getContent(), subscribedUser))
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(rawData.getTotalElements())
                .totalPages(rawData.getTotalPages())
                .build();
        return new Message<AlternateSectionResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched alternate sections successfully.")
                .setData(response);
    }


    @Override
    public Message<String> pinAlternateSection(long courseId, long sectionId, long fromSectionId, long fromCourseId, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Pinning the alternate section.");
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        if (repo.existsByCourse_IdAndFromSection_IdAndUser_Email(courseId, fromCourseId, email)) {
            throw new BadRequestException("Already pinned topic cannot be pinned.");
        }

        User user = userService.findByEmail(email);
        Section fromSection = sectionService.findById(fromSectionId);
        Course course = courseService.findById(courseId);
        Section section = sectionService.findById(sectionId);
        Course fromCourse = courseService.findById(fromCourseId);

        UserAlternateSection data = UserAlternateSection.builder()
                .fromSection(fromSection)
                .section(section)
                .course(course)
                .fromCourse(fromCourse)
                .user(user)
                .build();

        try {
            repo.save(data);
            log.info("Alternate section is pinned.");
            courseProgressService.markCompletedAllTopicsOfASection(sectionId, user.getId());
        } catch (Exception e) {
            throw new InternalServerException("Users alternate section "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully pinned the section.")
                .setData("Successfully pinned the section.");
    }

    @Override
    public Message<String> unpinAlternateSection(long courseId, long sectionId, String email)
            throws EntityNotFoundException, InternalServerException {
        log.info("Unpinning the alternate section.");
        List<UserAlternateSection> alternateSections = repo.findByCourse_IdAndFromSection_IdAndUser_Email(courseId, sectionId, email);
        try {
            if(!CollectionUtils.isEmpty(alternateSections))
                repo.deleteAll(alternateSections);
            log.info("Alternate section is unpinned successfully.");
        } catch (Exception e) {
            throw new InternalServerException("Alternate section cannot be deleted from db.");
        }
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Alternate section is unpinned successfully.")
                .setData("Alternate section is unpinned successfully.");
    }

    public UserAlternateSection findByCourseId(Long courseId, Long userId){
        return this.repo.findByCourseId(courseId, userId).orElse(null);
    }
}
