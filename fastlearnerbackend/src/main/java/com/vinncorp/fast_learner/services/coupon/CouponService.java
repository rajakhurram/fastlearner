package com.vinncorp.fast_learner.services.coupon;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.coupon.CouponCourse;
import com.vinncorp.fast_learner.models.coupon.CouponEmailDomain;
import com.vinncorp.fast_learner.models.coupon.CouponUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.coupon.CouponCourseRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponEmailDomainRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponUserRepository;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CouponType;
import com.vinncorp.fast_learner.util.exception.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponService implements ICouponService{

    private final CouponRepository repo;
    private final CouponEmailDomainRepository couponEmailDomainRepo;
    private final CouponUserRepository couponUserRepo;
    private final CouponCourseRepository couponCourseRepo;
    private final CourseService courseService;
    private final ISubscriptionService subscriptionService;
    private final IUserService userService;

    @Override
    public Coupon findByCouponCode(String code) throws EntityNotFoundException {
        log.info("Fetching coupon by coupon code...");

        return repo.findByRedeemCode(code)
                .orElseThrow(() -> new EntityNotFoundException("No coupon found with coupon code: " + code));
    }

    @Override
    public Message<List<Coupon>> fetchAll() throws EntityNotFoundException {
        log.info("Fetching all coupons.");
        List<Coupon> coupons = repo.findAll();
        if (coupons.isEmpty())
            throw new EntityNotFoundException("No coupons found in the system.");

        log.info("Fetched all coupons.");
        return new Message<List<Coupon>>()
                .setMessage("Fetching all coupons")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(coupons);
    }

    /**
     * Updated the coupon with respect to the new requirement.
     * */
    @Override
    public Message<String> create(CouponRequest request) throws InternalServerException {
        log.info("Creating coupon...");

        Message<Subscription> subscription = ExceptionUtils.safelyFetch(() -> subscriptionService.findBySubscriptionId(request.getSubscriptionId()));

        Coupon coupon = Coupon.builder()
                .redeemCode(request.getCoupon())
                .discount(request.getDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .subscription(Objects.nonNull(subscription) ? subscription.getData() : null)
                .couponType(CouponType.valueOf(request.getCouponType()))
                .durationInMonth(request.getDurationInMonth())
                .isRestricted(Objects.nonNull(request.getSpecifiedUsers()) || Objects.nonNull(request.getSpecifiedCourses()) || Objects.nonNull(request.getSpecifiedEmailDomains()))
                .allowAllCourse(request.isAllowAllCourse())
                .isActive(true)
                .build();

        try {
            repo.save(coupon);
        } catch (Exception e) {
            throw new InternalServerException("Coupon" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        if (Objects.nonNull(request.getSpecifiedEmailDomains())) {
            var listOfSpecifiedDomain = request.getSpecifiedEmailDomains().stream().map(e ->
                    CouponEmailDomain.builder()
                            .coupon(coupon)
                            .domain(e.trim())
                            .isActive(true)
                            .build()
            ).toList();
            couponEmailDomainRepo.saveAll(listOfSpecifiedDomain);
        }

        if (Objects.nonNull(request.getSpecifiedUsers())) {
            var listOfSpecifiedUsers = request.getSpecifiedUsers().stream().map(e ->
                    CouponUser.builder()
                            .coupon(coupon)
                            .email(e.trim().toLowerCase())
                            .isActive(true)
                            .build()
            ).toList();
            couponUserRepo.saveAll((listOfSpecifiedUsers));
        }

        if (Objects.nonNull(request.getSpecifiedCourses())) {
            List<CouponCourse> couponCourses = request.getSpecifiedCourses().stream()
                    .map(courseId -> ExceptionUtils.safelyFetch(() -> courseService.findById(courseId)))
                    .filter(Objects::nonNull)
                    .map(course -> CouponCourse.builder()
                            .coupon(coupon)
                            .course(course)
                            .isActive(true)
                            .build())
                    .collect(Collectors.toList());

            if (!couponCourses.isEmpty()) {
                couponCourseRepo.saveAll(couponCourses);
            }
        }
        log.info("Coupon created successfully.");
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Coupon created successfully.")
                .setData("Coupon created successfully.");
    }

    @Override
    public Message<String> update(CouponRequest request) throws EntityNotFoundException, InternalServerException {
        log.info("Updating coupon...");
        Coupon coupon = repo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("No coupon found by id."));

        Subscription subscription = subscriptionService.findBySubscriptionId(request.getSubscriptionId()).getData();

        coupon.setDiscount(request.getDiscount());
        coupon.setRedeemCode(request.getCoupon());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setSubscription(subscription);
        coupon.setIsActive(request.getIsActive());

        try {
            repo.save(coupon);
        } catch (Exception e) {
            throw new InternalServerException("Coupon" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
        log.info("Coupon updated successfully.");
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Coupon updated successfully.")
                .setData("Coupon updated successfully.");
    }

    @Transactional
    @Override
    public Message<String> delete(Long id) {
        log.info("Deleting coupon by id...");

        couponUserRepo.deleteByCouponId(id);
        couponEmailDomainRepo.deleteByCouponId(id);
        couponCourseRepo.deleteByCouponId(id);
        repo.deleteById(id);
        log.info("Deleted coupon successfully.");

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Coupon deleted successfully.")
                .setData("Coupon deleted successfully.");
    }

    @Transactional(readOnly = true)
    @Override
    public Coupon validateCoupon(String couponCode, User user, CouponType couponType, Long courseId) throws EntityNotFoundException {
        log.info("Validating coupon...");
        Coupon coupon = repo.validateCoupon(couponCode, user.getEmail(), user.getEmail().split("@")[1], couponType.name(), courseId)
                .orElseThrow(() -> new EntityNotFoundException("Coupon code is not valid or expired."));
        return repo.findById(coupon.getId()).orElse(null);
    }

    @Override
    public Message<Coupon> validateDiscount(String couponCode, String couponType, Long courseId,Long subscriptionId ,String email) throws EntityNotFoundException, BadRequestException {
        log.info("Validating coupon...");
        User user = userService.findByEmail(email);
        Coupon coupon = this.validateCoupon(couponCode, user, CouponType.valueOf(couponType), courseId);

        if(subscriptionId != null && !coupon.getSubscription().getId().equals(subscriptionId)) {
            String duration;
            if (coupon.getSubscription().getDuration() == 1) {
                duration = "Monthly";
            } else {
                duration = "Annual";
            }
            throw new BadRequestException("This coupon is only applicable on "
                    + coupon.getSubscription().getName() + " (" + duration + ")");

        }

        return new Message<Coupon>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Coupon Fetched successfully.")
                .setData(coupon);
    }

}
