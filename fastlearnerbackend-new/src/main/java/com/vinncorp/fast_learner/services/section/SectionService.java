package com.vinncorp.fast_learner.services.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.repositories.section.SectionRepository;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.dtos.section.SectionDetail;
import com.vinncorp.fast_learner.dtos.section.SectionDetailForContent;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.response.section.SectionDetailForUpdateResponse;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import com.vinncorp.fast_learner.response.section.SectionDetailResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SectionService implements ISectionService{

    private final SectionRepository repo;
    private final IEnrollmentService enrollmentService;
    private final IUserCourseProgressService userCourseProgressService;
    private final IUserService userService;
    private final ICourseService courseService;
    private final ISubscribedUserService subscribedUserService;
    private final ICourseUrlService courseUrlService;

    public SectionService(SectionRepository repo, IEnrollmentService enrollmentService,
                          IUserCourseProgressService userCourseProgressService,
                          IUserService userService, @Lazy ICourseService courseService, ISubscribedUserService subscribedUserService, ICourseUrlService courseUrlService) {
        this.repo = repo;
        this.enrollmentService = enrollmentService;
        this.userCourseProgressService = userCourseProgressService;
        this.userService = userService;
        this.courseService = courseService;
        this.subscribedUserService = subscribedUserService;
        this.courseUrlService = courseUrlService;
    }

    @Override
    public Section save(Section section) throws InternalServerException {
        log.info("Creating section.");
        if(Objects.isNull(section.getDelete())){
            section.setDelete(false);
        }
        try {
            if (Objects.nonNull(section.getId()) && section.getDelete()) {
                repo.deleteById(section.getId());
                return null;
            }else {
                return repo.save(section);
            }
        } catch (Exception e) {
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("Section"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<SectionDetail> fetchSectionDetailByCourseId(Long courseId) throws EntityNotFoundException {
        log.info("Fetching section details by course id: "+courseId);
        List<Tuple> sectionDetail = repo.fetchSectionDetailByCourseId(courseId);
        if (CollectionUtils.isEmpty(sectionDetail)) {
            throw new EntityNotFoundException("No section detail found.");
        }

        return SectionDetail.from(sectionDetail);
    }

    @Override
    public Message<SectionDetailResponse> getAllSectionsByCourseId(Long courseId, String email) throws EntityNotFoundException, BadRequestException {
        log.info("Fetching all sections by course.");

        Course course = courseService.findById(courseId);

        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        User user = userService.findByEmail(email);

        List<Tuple> sections = repo.findAllByCourseId(courseId, user.getId());
        if(sections.isEmpty()){
            throw new EntityNotFoundException("Sections not found.");
        }

        SubscribedUser subscribedUser = subscribedUserService.findByUser(user.getEmail());

        List<SectionDetailForContent> contentDetails = SectionDetailForContent.from(sections, courseId, subscribedUser);

        UserCourseProgress userCourseProgress = userCourseProgressService.getPreviousTopicByUserAndCourse(courseId, email);
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        SectionDetailResponse response = new SectionDetailResponse();
        response.setSectionDetails(contentDetails);
        if(Objects.nonNull(userCourseProgress)) {
            response.setPrevSectionId(userCourseProgress.getSection().getId());
            response.setPrevTopicId(userCourseProgress.getTopic().getId());
        }
        response.setTitle(course.getTitle());
        response.setCourseUrl(courseUrl.getUrl());
        response.setCategory(course.getCourseCategory().getName());
        response.setHasCertificate(course.getCertificateEnabled());

        return new Message<SectionDetailResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched all sections by course and user successfully.")
                .setData(response);
    }

    @Override
    public Section findById(Long sectionId) throws EntityNotFoundException {
        log.info("Fetching section by section id: "+ sectionId);
        return repo.findById(sectionId).orElseThrow(() -> new EntityNotFoundException("No section found by provided section id."));
    }
    @Override
    public Message<List<SectionDetailForUpdateResponse>> fetchAllSectionForUpdate(Long courseId, String email)
            throws EntityNotFoundException, BadRequestException {
        log.info("Fetching all section for update.");

        if(Objects.isNull(courseId)){throw new BadRequestException("Course ID cannot be null");}

        if(Objects.isNull(email)){throw new BadRequestException("Email cannot be null");}

        User user = userService.findByEmail(email);
        List<Tuple> data = repo.findAllByCourseIdAndUserId(courseId, user.getId());
        if (CollectionUtils.isEmpty(data)) {
            throw new EntityNotFoundException("The user cannot access this course.");
        }
        List<SectionDetailForUpdateResponse> sectionDetailForUpdate = SectionDetailForUpdateResponse.from(data);
        return new Message<List<SectionDetailForUpdateResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully fetched all sections for update.")
                .setData(sectionDetailForUpdate);
    }

    public List<Section> getAllSectionsByCourseId(Long courseId){
        return this.repo.findAllByCourseId(courseId);
    }
}
