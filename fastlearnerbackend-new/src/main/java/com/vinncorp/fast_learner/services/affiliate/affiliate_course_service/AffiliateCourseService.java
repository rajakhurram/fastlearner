package com.vinncorp.fast_learner.services.affiliate.affiliate_course_service;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto;
import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseRequest;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.models.affiliate.Affiliate;
import com.vinncorp.fast_learner.models.affiliate.AffiliatedCourses;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutConfig;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.AffiliatedCoursesRepository;
import com.vinncorp.fast_learner.services.affiliate.affiliate_service.IAffiliateService;
import com.vinncorp.fast_learner.services.affiliate.instructor_affiliate_service.IInstructorAffiliateService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.payout.premium_course.SelfAffiliatePayoutService;
import com.vinncorp.fast_learner.services.payout.premium_course.DirectPurchasePayoutService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionValidationsService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Constants.UpgradePlanTemplate;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.ObjectUtils;
import com.vinncorp.fast_learner.util.enums.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
@Slf4j
public class AffiliateCourseService implements IAffiliateCourseService {

    private final IUserService userService;
    private final ICourseService courseService;
    private final IInstructorAffiliateService instructorAffiliateService;
    private final AffiliatedCoursesRepository repo;
    private final ICourseUrlService courseUrlService;
    private final SelfAffiliatePayoutService selfAffiliatePayoutService;
    private final DirectPurchasePayoutService directPurchasePayoutService;
    private final IAffiliateService affiliateService;
    private final ISubscriptionValidationsService subscriptionValidationsService;
    private final ISubscribedUserService subscribedUserService;

    public AffiliateCourseService(IUserService userService, ICourseService courseService, IInstructorAffiliateService instructorAffiliateService, AffiliatedCoursesRepository repo, ICourseUrlService courseUrlService, SelfAffiliatePayoutService selfAffiliatePayoutService, DirectPurchasePayoutService directPurchasePayoutService, IAffiliateService affiliateService, ISubscriptionValidationsService subscriptionValidationsService, ISubscribedUserService subscribedUserService) {
        this.userService = userService;
        this.courseService = courseService;
        this.instructorAffiliateService = instructorAffiliateService;
        this.repo = repo;
        this.courseUrlService = courseUrlService;
        this.selfAffiliatePayoutService = selfAffiliatePayoutService;
        this.directPurchasePayoutService = directPurchasePayoutService;
        this.affiliateService = affiliateService;
        this.subscriptionValidationsService = subscriptionValidationsService;
        this.subscribedUserService = subscribedUserService;
    }

    @Override
    public Message<String> assignCourseToAffiliate(List<AffiliateCourseRequest> requests, String name) throws EntityNotFoundException, BadRequestException, InternalServerException, LimitExceedException {
        User user = this.userService.findByEmail(name);
        Long courseId = requests.get(0).getCourseId();
        Long affiliateCount = this.repo.getCountOfAssignedAffiliatesByInstructorAndCourse(user.getId(), courseId, GenericStatus.ACTIVE.name(),  GenericStatus.ACTIVE.name());
        SubscribedUser subscribedUser = this.subscribedUserService.findByUser(name);

        if(!subscribedUser.getSubscription().getPlanType().equals(PlanType.ULTIMATE)){
            SubscriptionValidations subscriptionValidations = this.subscriptionValidationsService.findByValidationNameAndSubscriptionAndIsActive(SubscriptionValidation.ASSIGN_COURSE.name(), subscribedUser.getSubscription(), true);
            if(Objects.isNull(subscriptionValidations)){
                throw new BadRequestException("No subscription validation found");
            }

            if(affiliateCount >= subscriptionValidations.getValue()){
                throw new LimitExceedException("You’ve reached the limit of "+subscriptionValidations.getValue()+" Affiliates in this course. To add more, please "+ UpgradePlanTemplate.TEXT +" your plan.");
            }else if(affiliateCount.equals(0L) && requests.size() > subscriptionValidations.getValue()){
                throw new LimitExceedException("You cannot add more than  " +subscriptionValidations.getValue()+" affiliates in this courses. To add more, "+ UpgradePlanTemplate.TEXT +" your plan.");
            }else if(affiliateCount + requests.size() > subscriptionValidations.getValue()) {
                throw new LimitExceedException("You've added " +affiliateCount+" affiliates out of "+subscriptionValidations.getValue()+" in this courses. To select more, "+ UpgradePlanTemplate.TEXT +" your plan.");
            }
        }

        List<AffiliatedCourses> affiliatedCourses = new ArrayList<>();
        for(AffiliateCourseRequest request: requests){
            InstructorAffiliate instructorAffiliate = this.instructorAffiliateService.getByInstructorIdAndAffiliateId(user.getId(), request.getAffiliateId());
            Course course = this.courseService.findById(request.getCourseId());
            if(!course.getCourseType().equals(CourseType.PREMIUM_COURSE)){throw new BadRequestException("Course should be Premium");}
            if(Objects.isNull(instructorAffiliate)){throw new EntityNotFoundException("Affiliate not found");}
            if(!course.getCreatedBy().equals(user.getId())){throw new EntityNotFoundException("Course not found by instructor "+user.getEmail());}

            if(!Objects.isNull(this.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliate.getId(), course.getId(), GenericStatus.ACTIVE))){
                throw new BadRequestException("Course already assigned");
            }
            CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
            AffiliateCourseDto affiliateCourseDto = this.repo.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliate.getId(), courseId, GenericStatus.INACTIVE).orElse(null);
            if(Objects.nonNull(affiliateCourseDto)){
                AffiliatedCourses affiliatedCourse = this.repo.findById(affiliateCourseDto.getId()).get();
                affiliatedCourse.setStatus(GenericStatus.ACTIVE);
                affiliatedCourse.setUrl(courseUrl.getUrl() +"?affiliate="+instructorAffiliate.getAffiliateUuid());
                affiliatedCourse.setReward(request.getReward());
                this.repo.save(affiliatedCourse);
            }else {
                affiliatedCourses.add(AffiliatedCourses
                        .builder()
                        .instructorAffiliate(instructorAffiliate)
                        .course(course)
                        .assignDate(new Date())
                        .reward(request.getReward())
                        .url(courseUrl.getUrl() +"?affiliate="+instructorAffiliate.getAffiliateUuid())
                        .status(GenericStatus.ACTIVE)
                        .build());
            }
        }
        try {
            this.repo.saveAll(affiliatedCourses);
        }catch(Exception ex){
            throw new InternalServerException(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Course assigned successfully");
    }

    @Override
    public AffiliateCourseDto getByInstructorAffiliateIdAndCourseIdAndStatus(Long instructorAffiliateId, Long courseId, GenericStatus status) throws BadRequestException {
        if(Objects.isNull(instructorAffiliateId) || Objects.isNull(courseId) || Objects.isNull(status)){throw new BadRequestException("required parameters are missing");}
        return this.repo.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status).orElse(null);
    }

    @Override
    public Message<Page<AffiliateCourseDto>> getAllCoursesByAffiliate(Long affiliateId, GenericStatus status, Pageable pageable, String name) throws EntityNotFoundException, BadRequestException {
        User user = this.userService.findByEmail(name);
        InstructorAffiliate instructorAffiliate = this.instructorAffiliateService.getByInstructorIdAndAffiliateId(user.getId(), affiliateId);
        if(Objects.isNull(instructorAffiliate)){throw new EntityNotFoundException("Affiliate not found");}
        Page<AffiliateCourseDto> data = this.repo.getByInstructorAffiliateIdAndStatus(instructorAffiliate.getId(),
//                status,
                pageable);
        if(!data.getContent().isEmpty()){
            return new Message<Page<AffiliateCourseDto>>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Courses fetch successfully")
                    .setData(data);
        }else{
            throw new EntityNotFoundException("Courses not found");
        }
    }

    @Override
    public Message<String> deleteAffiliateCourse(Long affiliateId, Long affiliateCourseId, String name) throws EntityNotFoundException, AuthenticationException, BadRequestException {
        User user = this.userService.findByEmail(name);
        InstructorAffiliate instructorAffiliate = this.instructorAffiliateService.getByInstructorIdAndAffiliateId(user.getId(), affiliateId);

        if(Objects.isNull(instructorAffiliate)){throw new EntityNotFoundException("Affiliate not found");}

        AffiliatedCourses affiliatedCourses = this.getById(affiliateCourseId);

        if(Objects.isNull(affiliatedCourses)){throw new EntityNotFoundException("No assigned course found by affiliate "+affiliateId);}

        if(instructorAffiliate.getId().equals(affiliatedCourses.getInstructorAffiliate().getId())){
            affiliatedCourses.setStatus(GenericStatus.INACTIVE);
            this.repo.save(affiliatedCourses);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Course deleted successfully");
        }else{
            throw new AuthenticationException("You are unauthorized to delete affiliate course");
        }
    }

    @Override
    public AffiliatedCourses getById(Long id) {
        return this.repo.findById(id).orElse(null);
    }

    @Override
    public Message<Page<AffiliateCourseDto>> getAllAffiliatesByCourse(
            Long courseId,
            String name,
            GenericStatus instructorAffiliateStatus,
            GenericStatus affiliateCourseStatus,
            Pageable pageable) throws EntityNotFoundException {
        User user = this.userService.findByEmail(name);
        Page<AffiliateCourseDto> data = this.repo.getAllAffiliatesByCourseAndStatus(
                user.getId(),
                courseId,
                user.getEmail(),
                instructorAffiliateStatus,
                affiliateCourseStatus,
                pageable);
        if(data.getContent().isEmpty() || data.getContent().stream()
                .allMatch(ObjectUtils::areAllFieldsNull)){
            throw new EntityNotFoundException("Data not found");
        }
        return new Message<Page<AffiliateCourseDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(data)
                .setMessage("Course affiliates fetched successfully");
    }

    public Boolean saveStudentOnboardingDetails(String affiliateUUID, Long courseId) throws BadRequestException, EntityNotFoundException, InternalServerException {
        InstructorAffiliate instructorAffiliate = this.instructorAffiliateService.getByAffiliateUUIDAndStatus(affiliateUUID, GenericStatus.ACTIVE);
        if(Objects.isNull(instructorAffiliate)){ return false; }

        AffiliateCourseDto affiliateCourseDto = this.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliate.getId(), courseId, GenericStatus.ACTIVE);
        if(Objects.isNull(affiliateCourseDto)){ return false; }

        Affiliate affiliate = this.affiliateService.findById(instructorAffiliate.getAffiliateUser().getId());
        User user = this.userService.findByEmail(instructorAffiliate.getInstructor().getEmail());
        PayoutType payoutType = affiliate.getEmail().equals(user.getEmail()) ? PayoutType.SELF : PayoutType.AFFILIATE;


        Course course = this.courseService.findById(courseId);
        PremiumCoursePayoutConfig premiumCoursePayoutConfig = this.directPurchasePayoutService.findByPayoutTypeAndIsActiveTrue(payoutType);
        try {
            Double calculateRevenue = this.calculateAffiliateRevenue(course.getPrice(), premiumCoursePayoutConfig.getPercentageCut(), affiliateCourseDto.getReward());
            Double revenue = affiliateCourseDto.getRevenue();
            revenue = Objects.isNull(revenue) || revenue.equals(0.0) ? calculateRevenue : revenue + calculateRevenue;
            Long students = affiliateCourseDto.getStudents();
            students = Objects.isNull(students) || students.equals(0L) ? 1 : students + 1;
                    this.repo.save(
                    AffiliatedCourses
                            .builder()
                            .id(affiliateCourseDto.getId())
                            .instructorAffiliate(instructorAffiliate)
                            .course(course)
                            .assignDate(affiliateCourseDto.getAssignDate())
                            .reward(affiliateCourseDto.getReward())
                            .revenue(revenue)
                            .onboardedStudents(students)
                            .url(affiliateCourseDto.getUrl())
                            .status(GenericStatus.ACTIVE)
                            .build()
                    );

            return true;

        }catch(Exception ex){
            throw new InternalServerException(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<String> updateAssignCourseActiveAndInActive(Long affiliatedCourseId, Long instructorAffiliateId, GenericStatus status, Principal principal) throws BadRequestException, EntityNotFoundException, LimitExceedException {
        log.info("Request to update status of affiliated course with ID: {} by user: {}", affiliatedCourseId, principal.getName());

        Optional<AffiliatedCourses> isAffiliatedCourses = repo.findById(affiliatedCourseId);

        if(isAffiliatedCourses.isEmpty()){throw new BadRequestException("Affiliate course not found");}

        AffiliatedCourses affiliatedCourses = isAffiliatedCourses.get();
        if(Objects.nonNull(this.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, affiliatedCourses.getCourse().getId(), GenericStatus.ACTIVE)) && status.equals(GenericStatus.ACTIVE)){
            throw new BadRequestException("Course already assigned");
        }

        User user = this.userService.findByEmail(principal.getName());
        Long affiliateCount = this.repo.getCountOfAssignedAffiliatesByInstructorAndCourse(user.getId(), affiliatedCourses.getCourse().getId(), GenericStatus.ACTIVE.name(),  GenericStatus.ACTIVE.name());
        SubscribedUser subscribedUser = this.subscribedUserService.findByUser(user.getEmail());

        if(!subscribedUser.getSubscription().getPlanType().equals(PlanType.ULTIMATE) && status.equals(GenericStatus.ACTIVE)){
            SubscriptionValidations subscriptionValidations = this.subscriptionValidationsService.findByValidationNameAndSubscriptionAndIsActive(SubscriptionValidation.ASSIGN_COURSE.name(), subscribedUser.getSubscription(), true);
            if(Objects.isNull(subscriptionValidations)){
                throw new BadRequestException("No subscription validation found");
            }

            if(affiliateCount >= subscriptionValidations.getValue()){
                throw new LimitExceedException("You’ve reached the limit of "+subscriptionValidations.getValue()+" Affiliates in this course. To add more, please "+ UpgradePlanTemplate.TEXT +" your plan.");
            }
        }


        affiliatedCourses.setStatus(status);
        repo.save(affiliatedCourses);

        log.info("Successfully updated status of affiliated course with ID: {} to {}", affiliatedCourseId, status);
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Affiliate course successfully updated");

    }

    public Double calculateAffiliateRevenue(Double coursePrice, Double percentageCut, Double affiliateReward) {
        Double cutValue = coursePrice * percentageCut;
        return cutValue * (affiliateReward / 100);
    }

}
