package com.vinncorp.fast_learner.services.payout.premium_course;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutConfig;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutTransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.AffiliatedCoursesRepository;
import com.vinncorp.fast_learner.repositories.affiliate.InstructorAffiliateRepository;
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
import java.util.Optional;

@Slf4j
@Component("affiliate")
public class InstructorAffiliatePayoutService implements PayoutStrategyService{


    private StringBuilder stringBuilder;

    @Autowired
    private PremiumCoursePayoutConfigRepository payoutConfigRepo;

    @Autowired
    private  AffiliatedCoursesRepository affiliatedCoursesRepository;
    @Autowired
    private InstructorAffiliateRepository instructorAffiliateRepository;


    @Autowired
    private IPremiumCoursePayoutTransactionHistoryService service;

    public InstructorAffiliatePayoutService() {
    }

    @Override
    public void executePayout(Course course, User user, String transId, String uuid, Coupon coupon) throws BadRequestException, InternalServerException {
        log.info("Executing payout process.");
        String instructorStripeId = course.getInstructor().getStripeAccountId();
        String affiliateStripeId = null;
        double payoutAmount = calculateAmountForInstructor(course.getPrice());
        stringBuilder = new StringBuilder();
        PremiumCoursePayoutTransactionHistory premiumCoursePayoutHistoryForAffiliate = null;
        // uuid is mandatory to find out affiliate stripe account
        double affiliatePayoutAmount = 0.0;
        if (uuid != null) {
            Tuple affiliatedCourses = affiliatedCoursesRepository.findByUuidAndCourseId(uuid, course.getId());
            Long instructorAffiliateId = (Long) affiliatedCourses.get("id");
            affiliateStripeId = (String) affiliatedCourses.get("stripe_account_id");
            affiliatePayoutAmount = calculateAmountForAffiliate(payoutAmount, (Double) affiliatedCourses.get("reward"));
            if (affiliatePayoutAmount==0.0) {
                stringBuilder.append("Amount calculation got error please contact administrator in instructor affiliate.");
                premiumCoursePayoutHistoryForAffiliate = ifPayoutAmountError(course, user, transId);
            }else{
                if (affiliateStripeId == null) {
                    stringBuilder.append("Affiliate Stripe account not found due to invalid Uuid and course id");
                    premiumCoursePayoutHistoryForAffiliate = stripeIdNotFound(course, user, transId, affiliatePayoutAmount);
                } else {
                    premiumCoursePayoutHistoryForAffiliate = stripeIdFoundProcessPayout(course, user, transId, affiliatePayoutAmount);
                }
            }
            InstructorAffiliate instructorAffiliate = instructorAffiliateRepository.findByAffiliateUser(instructorAffiliateId);
            premiumCoursePayoutHistoryForAffiliate.setInstructorAffiliate(instructorAffiliate);
            service.save(premiumCoursePayoutHistoryForAffiliate);
        }
        PremiumCoursePayoutTransactionHistory premiumCoursePayoutHistory = null;
        payoutAmount = payoutAmount - affiliatePayoutAmount;
        if (payoutAmount == 0.0) {
            stringBuilder.append("Amount calculation got error please contact administrator.");
            premiumCoursePayoutHistory = ifPayoutAmountError(course, user, transId);
        } else {
            if (Objects.isNull(instructorStripeId)) {
                stringBuilder.append(" Stripe id is not found for the instructor please onboard the instructor.");
                premiumCoursePayoutHistory = stripeIdNotFound(course, user, transId, payoutAmount);
            } else {
                premiumCoursePayoutHistory = stripeIdFoundProcessPayout(course, user, transId, payoutAmount);
            }
        }
        premiumCoursePayoutHistory.setCoupon(coupon);
        service.save(premiumCoursePayoutHistory);
    }

    private double calculateAmountForAffiliate(double amount,double affiliatePercentage) {
        log.info("Fetching premium course payout configuration for payout percentages.");
        double percentage = affiliatePercentage/100;
        return amount * percentage;
    }


    private double calculateAmountForInstructor(double amount) {
        log.info("Fetching premium course payout configuration for payout percentages.");
        PremiumCoursePayoutConfig data = payoutConfigRepo.findByPayoutTypeAndIsActiveTrue(PayoutType.AFFILIATE);
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
                .payoutType(PayoutType.AFFILIATE)
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
                .payoutType(PayoutType.AFFILIATE)
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
                .payoutType(PayoutType.AFFILIATE)
                .payoutStatus(PayoutStatus.PENDING)
                .amount(payoutAmount)
                .stripeAccountStatus(StripeAccountStatus.DISCONNECTED)
                .description(stringBuilder.toString())
                .build();
    }


}
