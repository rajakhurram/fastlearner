package com.vinncorp.fast_learner.services.payment.checkout;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.payment.checkout.ChargePayment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.AffiliatedCoursesRepository;
import com.vinncorp.fast_learner.repositories.affiliate.InstructorAffiliateRepository;
import com.vinncorp.fast_learner.request.payment_gateway.checkout.CheckoutRequest;
import com.vinncorp.fast_learner.response.payment_checkout.CreateTransactionResponse;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.services.affiliate.affiliate_course_service.IAffiliateCourseService;
import com.vinncorp.fast_learner.services.coupon.ICouponService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.payout.premium_course.InstructorAffiliatePayoutService;
import com.vinncorp.fast_learner.services.payout.premium_course.PayoutContextService;
import com.vinncorp.fast_learner.services.payout.premium_course.SelfAffiliatePayoutService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CouponType;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCheckoutService implements IPaymentCheckoutService {

    private final GenericRestClient restClient;
    private final IUserService userService;
    private final ICourseService courseService;
    private final IEnrollmentService enrollmentService;
    private final PayoutContextService payoutContextService;
    private final AffiliatedCoursesRepository repo;
    private final InstructorAffiliateRepository instructorAffiliateRepository;
    private final SelfAffiliatePayoutService selfAffiliatePayoutService;
    private final ICouponService couponService;
    private final InstructorAffiliatePayoutService instructorAffiliatePayoutService;
    private final IAffiliateCourseService affiliateCourseService;

    @Override
    @Transactional(rollbackFor = {InternalServerException.class, BadRequestException.class})
    public Message<String> chargePayment(ChargePayment chargePayment, String email) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Charging payment is in process...");

        User user = userService.findByEmail(email);

        double courseAmount = 0.0;

        Course course = courseService.findById(chargePayment.getCourseId());
        if (course.getCourseType() != CourseType.PREMIUM_COURSE)
            throw new BadRequestException("The course provided is not a premium course.");

        Coupon coupon = null;

        if (Objects.nonNull(chargePayment.getCoupon())) {
            coupon = couponService.validateCoupon(chargePayment.getCoupon(), user, CouponType.PREMIUM, chargePayment.getCourseId());

            if (coupon != null && coupon.getAllowAllCourse()) {
                double percentage = coupon.getDiscount() / 100.0;
                courseAmount = course.getPrice() - (course.getPrice() * percentage);
            } else if (coupon != null) {
                double percentage = coupon.getDiscount() / 100.0;
                courseAmount = course.getPrice() - (course.getPrice() * percentage);
            }        // Calculate course if the coupon is applied
        } else {
            courseAmount = course.getPrice();
        }


        // Hitting the Payment charge payment api
        CreateTransactionResponse response = chargeWithToken(chargePayment.getOpaqueData(), courseAmount, course.getId());

        if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
            if (response.getTransactionResponse().getErrors() != null) {
                if (!response.getTransactionResponse().getErrors().getError().isEmpty()) {
                    throw new BadRequestException("Transaction Error: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                }
            }
            // When the student checkout for course then enroll the student into the course
            enrollmentService.enrolled(course.getId(), email, false);

            // when the affiliateUUID is present save student onboarding details
            if (Objects.nonNull(chargePayment.getAffiliateUUID())) {
                Boolean isSaved = this.affiliateCourseService.saveStudentOnboardingDetails(chargePayment.getAffiliateUUID(), chargePayment.getCourseId());
                if (!isSaved) {
                    chargePayment.setAffiliateUUID(null);
                }
            }

            // Do payout to instructors using payout service
            //this method
            payoutProcess(course, user, response.getTransactionResponse().getTransId(), chargePayment.getAffiliateUUID(), coupon);

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Successfully charged the amount of $" + courseAmount + " for course: " + course.getTitle())
                    .setData("Successfully charged the amount of $" + courseAmount + " for course: " + course.getTitle());
        }

        String errorResp = null;

        if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null && !response.getTransactionResponse().getErrors().getError().isEmpty()) {
            errorResp = response.getTransactionResponse().getErrors().getError().get(0).getErrorText();
        } else {
            errorResp = response.getMessages().getMessage().get(0).getText();
        }

        return new Message<String>()
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setCode(HttpStatus.BAD_REQUEST.name())
                .setMessage(errorResp != null ? errorResp : response.getMessages().getMessage().get(0).getText())
                .setData(errorResp != null ? errorResp : response.getMessages().getMessage().get(0).getText());
    }

    private void payoutProcess(Course course, User user, String transId, String uuid, Coupon coupon) throws BadRequestException, InternalServerException {
        log.info("Payout process is initializing...");

        if (uuid != null && !uuid.trim().isEmpty()) {
            Tuple affiliatedCourses = repo.findByUuidAndCourseId(uuid, course.getId());

            String email = (String) affiliatedCourses.get("email");
            Long courseId = (Long) affiliatedCourses.get("course_id");
            Tuple isAffiliateAsInstructor = instructorAffiliateRepository.findByAffiliateId(courseId, email);
            if (isAffiliateAsInstructor != null) {
                payoutContextService.setPayoutStrategy(PayoutType.SELF);
                selfAffiliatePayoutService.executePayout(course, user, transId, null, coupon);
            } else {
                payoutContextService.setPayoutStrategy(PayoutType.AFFILIATE);
                instructorAffiliatePayoutService.executePayout(course, user, transId, uuid, coupon);
            }
        } else {
            payoutContextService.setPayoutStrategy(PayoutType.DIRECT);
            try {
                payoutContextService.executePayout(course, user, transId, null, coupon);
            } catch (BadRequestException | InternalServerException e) {
                // Payout history should be maintained
                log.error("ERROR: " + e.getLocalizedMessage());
            }
        }
    }

    public CreateTransactionResponse chargeWithToken(String opaqueData, double amount, Long courseId) throws InternalServerException {
        log.info("Charging the payment for the course with ID: " + courseId);

        CheckoutRequest request = new CheckoutRequest();
        request.setOpaqueData(opaqueData);
        request.setAmount(new BigDecimal(amount));

        return restClient.makeRequest("/api/v1/checkout/", HttpMethod.POST, request, CreateTransactionResponse.class);
    }
}
