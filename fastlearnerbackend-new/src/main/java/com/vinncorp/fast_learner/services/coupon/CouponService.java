package com.vinncorp.fast_learner.services.coupon;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.coupon.CouponCourse;
import com.vinncorp.fast_learner.models.coupon.CouponEmailDomain;
import com.vinncorp.fast_learner.models.coupon.CouponUser;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
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
import com.vinncorp.fast_learner.util.enums.CouponAppliesTo;
import com.vinncorp.fast_learner.util.enums.CouponType;
import com.vinncorp.fast_learner.util.enums.DiscountUnit;
import com.vinncorp.fast_learner.util.exception.ExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Message<Page<Coupon>> fetchAll(Integer page, Integer size, Boolean isActive) throws EntityNotFoundException {
        log.info("Fetching all coupons.");
        Pageable pageable = PageRequest.of(page, size);
        Page<Coupon> coupons = (isActive != null)
                ? repo.findByIsActive(isActive, pageable)
                : repo.findAll(pageable);
        if (coupons.isEmpty())
            throw new EntityNotFoundException("No coupons found in the system.");

        log.info("Fetched all coupons.");
        return new Message<Page<Coupon>>()
                .setMessage("Fetching all coupons")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(coupons);
    }

    /**
     * Updated the coupon with respect to the new requirement.
     * */
    @Override
    public Message<String> create(CouponRequest request) throws InternalServerException, EntityAlreadyExistException {
        log.info("Creating coupon...");

        ensureRedeemCodeAvailable(request.getCoupon(), null);

        Message<com.vinncorp.fast_learner.models.subscription.Subscription> subscription = ExceptionUtils.safelyFetch(() -> subscriptionService.findBySubscriptionId(request.getSubscriptionId()));

        Coupon coupon = Coupon.builder()
                .redeemCode(request.getCoupon())
                .discount(request.getDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .subscription(Objects.nonNull(subscription) ? subscription.getData() : null)
                .discountUnit(request.getDiscountUnit())
                .appliesTo(request.getAppliesTo())
                .durationInMonth(request.getDurationInMonth())
                .couponType(request.getCouponType())
                .billingCycle(request.getBillingCycle())
                .isRestricted(Objects.nonNull(request.getSpecifiedUsers()) || Objects.nonNull(request.getSpecifiedCourses()) || Objects.nonNull(request.getSpecifiedEmailDomains()))
                .allowAllCourse(request.isAllowAllCourse())
                .isActive(true)
                .build();

        saveCoupon(coupon);

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
    public Message<String> update(CouponRequest request) throws EntityNotFoundException, InternalServerException, EntityAlreadyExistException {
        log.info("Updating coupon...");
        Coupon coupon = repo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("No coupon found by id."));

        ensureRedeemCodeAvailable(request.getCoupon(), request.getId());

        coupon.setDiscount(request.getDiscount());
        coupon.setRedeemCode(request.getCoupon());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setDiscountUnit(request.getDiscountUnit());
        coupon.setAppliesTo(request.getAppliesTo());
        coupon.setIsActive(request.getIsActive());

        // Update subscription if provided
        if (Objects.nonNull(request.getSubscriptionId())) {
            Message<com.vinncorp.fast_learner.models.subscription.Subscription> subscription = ExceptionUtils.safelyFetch(() -> subscriptionService.findBySubscriptionId(request.getSubscriptionId()));
            coupon.setSubscription(Objects.nonNull(subscription) ? subscription.getData() : null);
        }

        saveCoupon(coupon);

        // Update coupon courses if provided
        if (Objects.nonNull(request.getSpecifiedCourses())) {
            couponCourseRepo.deleteByCouponId(request.getId());

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

    @Override
    public Message<CouponRequest> fetchById(Long id) throws EntityNotFoundException {
        log.info("Fetching coupon by id: {}", id);
        Coupon coupon = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No coupon found by id."));

        List<String> users = couponUserRepo.findByCouponId(id).stream()
                .map(CouponUser::getEmail).toList();
        List<String> domains = couponEmailDomainRepo.findByCouponId(id).stream()
                .map(CouponEmailDomain::getDomain).toList();
        List<Long> courses = couponCourseRepo.findByCouponId(id).stream()
                .map(c -> c.getCourse().getId()).toList();

        CouponRequest request = CouponRequest.builder()
                .id(coupon.getId())
                .coupon(coupon.getRedeemCode())
                .discount(coupon.getDiscount())
                .discountUnit(coupon.getDiscountUnit())
                .appliesTo(coupon.getAppliesTo())
                .subscriptionId(coupon.getSubscription() != null ? coupon.getSubscription().getId() : null)
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .durationInMonth(coupon.getDurationInMonth())
                .allowAllCourse(coupon.getAllowAllCourse() != null && coupon.getAllowAllCourse())
                .isActive(coupon.getIsActive())
                .couponType(coupon.getCouponType())
                .billingCycle(coupon.getBillingCycle())
                .specifiedUsers(users.isEmpty() ? null : users)
                .specifiedEmailDomains(domains.isEmpty() ? null : domains)
                .specifiedCourses(courses.isEmpty() ? null : courses)
                .build();

        log.info("Coupon fetched by id successfully.");
        return new Message<CouponRequest>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Coupon fetched successfully.")
                .setData(request);
    }

    @Override
    public Message<String> toggleStatus(Long id) throws EntityNotFoundException {
        log.info("Toggling coupon status for id: {}", id);
        Coupon coupon = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No coupon found by id."));

        coupon.setIsActive(!coupon.getIsActive());
        repo.save(coupon);

        String status = Boolean.TRUE.equals(coupon.getIsActive()) ? "activated" : "deactivated";
        log.info("Coupon {} successfully.", status);
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Coupon " + status + " successfully.")
                .setData("Coupon " + status + " successfully.");
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

    private void ensureRedeemCodeAvailable(String redeemCode, Long excludeCouponId) throws EntityAlreadyExistException {
        if (redeemCode == null || redeemCode.isBlank()) {
            return;
        }
        var existing = repo.findByRedeemCode(redeemCode.trim());
        if (existing.isPresent() && (excludeCouponId == null || !excludeCouponId.equals(existing.get().getId()))) {
            throw new EntityAlreadyExistException("Promo code already exists.");
        }
    }

    private void saveCoupon(Coupon coupon) throws InternalServerException, EntityAlreadyExistException {
        try {
            repo.save(coupon);
        } catch (DataIntegrityViolationException e) {
            throw new EntityAlreadyExistException("Promo code already exists.");
        } catch (Exception e) {
            throw new InternalServerException("Coupon" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

}
