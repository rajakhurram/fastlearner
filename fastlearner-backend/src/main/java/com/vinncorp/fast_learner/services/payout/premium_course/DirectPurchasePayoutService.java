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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Component("direct")
public class DirectPurchasePayoutService implements PayoutStrategyService {

    private StringBuilder stringBuilder;

    @Autowired
    private PremiumCoursePayoutConfigRepository payoutConfigRepo;

    @Autowired
    private IPremiumCoursePayoutTransactionHistoryService service;

    @Override
    public void executePayout(Course course, User user, String transId, String uuid, Coupon coupon) throws InternalServerException {
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

    /**
     * Calculating the payout amount with respect to the saved configuration of the percentage cuts of instructor in
     * our database.
     * */
    private double calculateAmountForInstructor(double amount) {
        log.info("Fetching premium course payout configuration for payout percentages.");
        PremiumCoursePayoutConfig data = payoutConfigRepo.findByPayoutTypeAndIsActiveTrue(PayoutType.DIRECT);
        if(Objects.isNull(data)) {
            log.error("Premium course payout config doesn't have active configuration for DIRECT payout to instructor.");
            return 0.0;
        }
        double percentage = data.getPercentageCut();
        return amount * percentage;
    }

    /**
     * If payout amount is 0.0 then the premium course payout transaction history will be generated with
     * description of error message
     * */
    private PremiumCoursePayoutTransactionHistory ifPayoutAmountError(Course course, User user, String transId) {
        return PremiumCoursePayoutTransactionHistory.builder()
                .transactionId(transId)
                .description(stringBuilder.toString())
                .course(course)
                .student(user)
                .payoutType(PayoutType.DIRECT)
                .payoutStatus(PayoutStatus.SYSTEM_ERROR)
                .creationDate(new Date())
                .build();
    }

    /**
     * Below method will run the payout process by requesting the payout api of stripe but if the payout process is not
     * run successfully from stripe side then the error will also be generated and save in our premium course payout
     * transaction history.
     * */
    private PremiumCoursePayoutTransactionHistory stripeIdFoundProcessPayout(Course course, User user, String transId, double payoutAmount) {
        log.info("Payout processing after checking the instructor is onboarded on our stripe account.");
//        String payoutId = null;
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
                .payoutType(PayoutType.DIRECT)
                .payoutStatus(PayoutStatus.PENDING)
                .stripeAccountStatus(StripeAccountStatus.CONNECTED)
                .creationDate(new Date())
                .amount(payoutAmount)
                .build();
    }

    /**
     * If stripe id not found for the instructor then the premium course payout transaction history will be generated with
     * description of error message
     * */
    private PremiumCoursePayoutTransactionHistory stripeIdNotFound(Course course, User user, String transId, double payoutAmount) {
        log.info("Instructor doesn't onboard into our stripe account.");
        return PremiumCoursePayoutTransactionHistory.builder()
                .course(course)
                .student(user)
                .transactionId(transId)
                .creationDate(new Date())
                .payoutType(PayoutType.DIRECT)
                .payoutStatus(PayoutStatus.PENDING)
                .amount(payoutAmount)
                .stripeAccountStatus(StripeAccountStatus.DISCONNECTED)
                .description(stringBuilder.toString())
                .build();
    }

    public PremiumCoursePayoutConfig findByPayoutTypeAndIsActiveTrue(PayoutType payoutType){
        return this.payoutConfigRepo.findByPayoutTypeAndIsActiveTrue(payoutType);
    }
}
