package com.vinncorp.fast_learner.services.payout.premium_course;

import com.stripe.exception.StripeException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutConfig;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutTransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.payout.premium_course.PremiumCoursePayoutConfigRepository;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import com.vinncorp.fast_learner.util.enums.StripeAccountStatus;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Component("self")
public class SelfAffiliatePayoutService implements PayoutStrategyService{

    private StringBuilder stringBuilder;

    @Autowired
    private PremiumCoursePayoutConfigRepository payoutConfigRepo;

    @Autowired
    private IPremiumCoursePayoutTransactionHistoryService service;

    @Override
    public void executePayout(Course course, User user, String transId, String uuid, Coupon coupon) throws BadRequestException, InternalServerException {
        log.info("Executing payout process.");
        String stripeId = course.getInstructor().getStripeAccountId();
        double payoutAmount = calculateAmountForInstructor(course.getPrice());
        stringBuilder = new StringBuilder();
        PremiumCoursePayoutTransactionHistory premiumCoursePayoutHistory = null;
        if(payoutAmount == 0.0) {
            stringBuilder.append("Amount calculation got error please contact administrator.");
            premiumCoursePayoutHistory = ifPayoutAmountError(course, user, transId);
        } else {
            if (Objects.isNull(stripeId)) {
                stringBuilder.append(" Stripe id is not found for the instructor please onboard the instructor.");
                premiumCoursePayoutHistory = stripeIdNotFound(course, user, transId, payoutAmount);
            } else {
                premiumCoursePayoutHistory = stripeIdFoundProcessPayout(course, user, transId, payoutAmount);
            }
        }
        premiumCoursePayoutHistory.setCoupon(coupon);
        service.save(premiumCoursePayoutHistory);
    }

    private double calculateAmountForInstructor(double amount) {
        log.info("Fetching premium course payout configuration for payout percentages.");
        PremiumCoursePayoutConfig data = payoutConfigRepo.findByPayoutTypeAndIsActiveTrue(PayoutType.SELF);
        if(Objects.isNull(data)) {
            log.error("Premium course payout config doesn't have active configuration for SELF payout to instructor.");
            return 0.0;
        }
        double percentage = data.getPercentageCut();
        return amount * percentage;
    }

    private PremiumCoursePayoutTransactionHistory ifPayoutAmountError(Course course, User user, String transId) {
        return PremiumCoursePayoutTransactionHistory.builder()
                .transactionId(transId)
                .description(stringBuilder.toString())
                .course(course)
                .student(user)
                .payoutType(PayoutType.SELF)
                .payoutStatus(PayoutStatus.SYSTEM_ERROR)
                .creationDate(new Date())
                .build();
    }

    private PremiumCoursePayoutTransactionHistory stripeIdFoundProcessPayout(Course course, User user, String transId, double payoutAmount) {
        log.info("Payout processing after checking the instructor is onboarded on our stripe account.");
        String payoutId = null;
//        try {
//            payoutId = sendPayout(course.getInstructor().getStripeAccountId(), payoutAmount);
//            stringBuilder.append("Payout Successful.");
//        } catch (StripeException e) {
//            log.error("ERROR: " + e.getMessage());
//            stringBuilder.append("Payout Error: " + e.getMessage());
//        }

        return PremiumCoursePayoutTransactionHistory.builder()
                .transactionId(transId)
//                .payoutId(payoutId)
                .description(stringBuilder.toString())
                .course(course)
                .student(user)
                .payoutType(PayoutType.SELF)
                .payoutStatus(PayoutStatus.PENDING)
                .creationDate(new Date())
                .stripeAccountStatus(StripeAccountStatus.CONNECTED)
                .amount(payoutAmount)
                .build();
    }

    private PremiumCoursePayoutTransactionHistory stripeIdNotFound(Course course, User user, String transId, double payoutAmount) {
        log.info("Instructor doesn't onboard into our stripe account.");
        return PremiumCoursePayoutTransactionHistory.builder()
                .course(course)
                .student(user)
                .transactionId(transId)
                .creationDate(new Date())
                .payoutType(PayoutType.SELF)
                .payoutStatus(PayoutStatus.PENDING)
                .amount(payoutAmount)
                .description(stringBuilder.toString())
                .stripeAccountStatus(StripeAccountStatus.DISCONNECTED)
                .build();
    }
}
