package com.vinncorp.fast_learner.services.affiliate.instructor_affiliate_service;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliatePremiumCourse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.InstructorAffiliateRepository;
import com.vinncorp.fast_learner.response.course.CourseDetailByType;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InstructorAffiliateService implements IInstructorAffiliateService{

    private final InstructorAffiliateRepository repo;
    private final IUserService userService;
    private final ICourseService courseService;

    public InstructorAffiliateService(InstructorAffiliateRepository repo, IUserService userService, ICourseService courseService) {
        this.repo = repo;
        this.userService = userService;
        this.courseService = courseService;
    }

    @Override
    public InstructorAffiliate getByInstructorIdAndAffiliateId(Long instructorId, Long affiliateId) throws BadRequestException {
        if(Objects.isNull(instructorId) || Objects.isNull(affiliateId)){throw new BadRequestException("required parameters are missing");}
        return this.repo.findActiveAffiliateByInstructorAndAffiliateUser(instructorId, affiliateId, GenericStatus.ACTIVE);
    }

    public Message<AffiliatePremiumCourse> getPremiumCoursesWithAffiliateReward(Long affiliateId, String name) throws EntityNotFoundException, BadRequestException {
        if(Objects.isNull(affiliateId) || Objects.isNull(name)){throw new BadRequestException("required parameters are missing");}
        User user = this.userService.findByEmail(name);
        List<CourseDetailByType> courses = this.courseService.getCourseDetailByInstructorIdAndType(user.getId(), CourseType.PREMIUM_COURSE);
        if(courses.isEmpty()){throw new EntityNotFoundException("No premium courses found");}
        InstructorAffiliate instructorAffiliate = this.getByInstructorIdAndAffiliateId(user.getId(), affiliateId);
        if(Objects.isNull(instructorAffiliate)){throw new EntityNotFoundException("Affiliate not found");}
        AffiliatePremiumCourse affiliatePremiumCourse = AffiliatePremiumCourse
                .builder()
                .reward(instructorAffiliate.getDefaultReward())
                .courseDetailsByType(courses)
                .build();

        return new Message<AffiliatePremiumCourse>()
                .setData(affiliatePremiumCourse)
                .setMessage("Instructor premium courses fetched successfully with reward.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    public InstructorAffiliate getByAffiliateUUIDAndStatus(String affiliateUUID, GenericStatus status) throws BadRequestException {
        if(Objects.isNull(affiliateUUID) || Objects.isNull(status)){ throw new BadRequestException("required parameters are missing");}
        return this.repo.findByAffiliateUUID(affiliateUUID, status);
    }

}
