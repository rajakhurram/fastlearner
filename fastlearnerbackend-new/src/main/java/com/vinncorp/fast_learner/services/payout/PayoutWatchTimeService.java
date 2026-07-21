package com.vinncorp.fast_learner.services.payout;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.PayoutWatchTime;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.repositories.payout.PayoutWatchTimeRepository;
import com.vinncorp.fast_learner.repositories.payout.premium_course.PremiumCoursePayoutConfigRepository;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionRepository;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutWatchTimeService implements IPayoutWatchTimeService{

    private final PayoutWatchTimeRepository repo;
    private final IUserService userService;
    private final ICourseService courseService;
    private final ITransactionHistoryService transactionHistoryService;
    private final PremiumCoursePayoutConfigRepository payoutConfigRepo;
    private final SubscriptionRepository subscriptionRepository;

    public void save(PayoutWatchTime payoutWatchTime) throws InternalServerException {
        log.info("Saving payout watch time in the db.");
        try {
            repo.save(payoutWatchTime);
        } catch (Exception e) {
            throw new InternalServerException("Payout watch time " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Message<String> create(Long courseId, long watchTime, String email) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Creating the payout watch time...");

        var course = courseService.findById(courseId);
        log.info("Course found: {}", course);
        var student = userService.findByEmail(email);
        log.info("Student found: {}", student);

        if (course.getCourseType() != CourseType.STANDARD_COURSE) {
            throw new BadRequestException("Payout cannot be made for this course.");
        }

        var transactionHistory = transactionHistoryService.fetchCurrentSubscription(student.getId());
        log.info("Transaction history for student {}: {}", student.getId(), transactionHistory);
        var payoutConfig = payoutConfigRepo.findByPayoutTypeAndIsActiveTrue(PayoutType.DIRECT);
        if (Objects.isNull(payoutConfig)) {
            throw new EntityNotFoundException("No payout config present for DIRECT payout.");
        }
        log.info("Payout config for DIRECT payout: {}", payoutConfig);

        LocalDate today = LocalDate.now();
        log.info("Today's date: {}", today);
        if (today.getDayOfMonth() > 25 && today.getDayOfMonth() <= today.lengthOfMonth()) {
            today = today.plusMonths(1);
        }
        int payoutMonth = today.getMonthValue();
        int payoutYear = today.getYear();
        log.info("Payout month: {}, Payout year: {}", payoutMonth, payoutYear);

        // Fetch all today's payout records for this student
        List<PayoutWatchTime> todayAllPayoutRecords =
                repo.findAllByStudentIdAndCreatedAt(
                        student.getId(),
                        java.sql.Date.valueOf(LocalDate.now())
                );
        log.info("Today's payout records for student {}: {}", student.getId(), todayAllPayoutRecords);

        // Step 1: Check if record already exists for same course + instructor + subscription today
        PayoutWatchTime existingRecord = null;
        for (PayoutWatchTime record : todayAllPayoutRecords) {
            if (record.getInstructorId().equals(course.getInstructor().getId())) {
                existingRecord = record;
                log.info("Updated existing payout record: {}", existingRecord);
                break;
            }
        }

        if (existingRecord != null) {
            // Update existing record
            existingRecord.setTimeSpend(existingRecord.getTimeSpend() + watchTime);
            existingRecord.setUpdatedAt(new Date());
            save(existingRecord);
        } else {
            // Insert new record
            PayoutWatchTime newRecord = PayoutWatchTime.builder()
                    .timeSpend(watchTime)
                    .noOfDays(1)
                    .amountSharePerDay(0.0) // Temporary, will update after distributing
                    .subscriptionId(transactionHistory.getSubscription().getId())
                    .studentId(student.getId())
                    .instructorId(course.getInstructor().getId())
                    .payoutStatus(PayoutStatus.PENDING)
//                    .courseId(course.getId())
//                    .stripeId(course.getInstructor().getStripeAccountId())
                    .payoutForCurrentYear(payoutYear)
                    .payoutForCurrentMonth(payoutMonth)
//                    .watchDate(today)
                    .createdAt(new Date())
                    .build();
            save(newRecord);
            log.info("Inserted new payout record: {}", newRecord);
        }

        // Step 2: Recalculate the distribution

        // Fetch again after insert
        List<PayoutWatchTime> updatedTodayRecords =
                repo.findAllByStudentIdAndCreatedAt(
                        student.getId(),
                        java.sql.Date.valueOf(LocalDate.now())
                );
        log.info("Updated today's payout records for student {}: {}", student.getId(), updatedTodayRecords);

        // Calculate total watch time of today
        long totalWatchTimeToday = updatedTodayRecords.stream()
                .mapToLong(PayoutWatchTime::getTimeSpend)
                .sum();
        log.info("Total watch time for today: {}", totalWatchTimeToday);

        if (totalWatchTimeToday == 0) {
            throw new BadRequestException("Total watch time for today is zero. Cannot calculate payout.");
        }

        // Calculate amountSharePerDay
        double totalAmountShare = calculateAmountSharePerDay(
                transactionHistory.getSubscriptionAmount(),transactionHistory.getSubscription(),
                payoutConfig.getPercentageCut()
        );
        log.info("Total amount share to be distributed: {}", totalAmountShare);

        // Distribute proportionally
        for (PayoutWatchTime record : updatedTodayRecords) {
            double instructorShare = ((double) record.getTimeSpend() / totalWatchTimeToday) * totalAmountShare;
            record.setAmountSharePerDay(instructorShare);
            save(record);
            log.info("Distributed amount ={} to instructorId={} for studentId={}, timeSpend={} sec, subscriptionId={}",
                    instructorShare,
                    record.getInstructorId(),
                    record.getStudentId(),
                    record.getTimeSpend(),
                    record.getSubscriptionId()
            );
        }

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully saved the watch time and distributed payout.");
    }

    private double calculateAmountSharePerDay(double subscriptionAmount, Subscription subscription, double payoutPercentage) {
        log.info("Calculating the payout amount per day share for an instructor...");
        double totalShareForInstructor = payoutPercentage * subscriptionAmount;
        if (subscription.getDuration()==12){
            totalShareForInstructor=totalShareForInstructor/12;
        }

        return totalShareForInstructor/30;
    }

    /**
     * Check If the current date is greater than 25 and less than the LAST DAY OF THE MONTH
     * i.e. currentDate = 28-1-2024, trialEndDate should be less than and equals to 25-1-2024
     * if not then calculate the no of days from trialEndDate i.e 26-1-2024 to the current date hence 2days
     * Else if the current date i.e currentDate 02-02-2024 then check the trialEndDate is less than and equals to
     * previous month's 25th i.e 25-01-2024 if yes then calculate the days from 25-1-2024 else calculate from trialEndDate
     *
     * @param currentDate
     * @param trialEndDate
     *
     * @return totalNoOfDays a user have been in this subscription
     * */
    public int calculateNoOfDays(LocalDate currentDate, Date trialEndDate, Date subscriptionNextCycle) {
        log.info("Calculating total number of days the current user has been in the subscription.");

        // Convert trialEndDate from Date to LocalDate
        LocalDate trialEndLocalDate = trialEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Convert subscriptionNextCycleLocalDate from Date to LocalDate
        LocalDate subscriptionNextCycleLocalDate = subscriptionNextCycle.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Calculate the 25th of the current month
        LocalDate currentMonth25th = currentDate.withDayOfMonth(25);

        // Calculate the last day of the current month
        LocalDate lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth());

        // If the current date is between 25th and the last day of the current month
        if (currentDate.isAfter(currentMonth25th) && currentDate.isBefore(lastDayOfMonth.plusDays(1))) {
            LocalDate nextMonth25th = currentMonth25th.plusMonths(1);
            // If trialEndDate is less than or equal to the 25th of the month
            if (!trialEndLocalDate.isAfter(currentMonth25th)) {
                log.info("Trial end date is before or on the 25th of the month.");

                // Calculate days from the 25th of the current month to subscriptionNextCycleLocalDate only if the
                // subscriptionNextCycleLocalDate less than the next month's 25th day else calculate to the next month's
                // 25th day
                int totalNoOfDays = 0;
                if (subscriptionNextCycleLocalDate.isBefore(nextMonth25th)) {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(currentMonth25th, subscriptionNextCycleLocalDate);
                } else {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(currentMonth25th, nextMonth25th);
                }

                log.info("User has been in the subscription from the 25th of the month for {} days.", totalNoOfDays);
                return totalNoOfDays;
            } else {
                // Calculate days from trialEndDate to currentDate
                int totalNoOfDays = 0;
                if (subscriptionNextCycleLocalDate.isBefore(nextMonth25th)) {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(trialEndLocalDate, subscriptionNextCycleLocalDate);
                } else {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(trialEndLocalDate, nextMonth25th);
                }
                log.info("User has been in the subscription for {} days.", totalNoOfDays);
                return totalNoOfDays;
            }
        }

        // If the current date is in the next month (or earlier in the same month)
        LocalDate previousMonth25th = currentDate.minusMonths(1).withDayOfMonth(25);

        if (!currentDate.isAfter(lastDayOfMonth) || currentDate.isAfter(lastDayOfMonth)) {
            if (!trialEndLocalDate.isAfter(previousMonth25th)) {
                // Calculate days from the 25th of the previous month to currentDate
                int totalNoOfDays = 0; // (int) java.time.temporal.ChronoUnit.DAYS.between(previousMonth25th, currentDate);
                if (subscriptionNextCycleLocalDate.isBefore(currentMonth25th)) {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(previousMonth25th, subscriptionNextCycleLocalDate);
                } else {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(previousMonth25th, currentMonth25th);
                }
                log.info("User has been in the subscription from previous month's 25th for {} days.", totalNoOfDays);
                return totalNoOfDays;
            } else {
                // Calculate days from trialEndDate to currentDate
                int totalNoOfDays = 0; // c(int) java.time.temporal.ChronoUnit.DAYS.between(trialEndLocalDate, currentDate);
                if (subscriptionNextCycleLocalDate.isBefore(currentMonth25th)) {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(trialEndLocalDate, subscriptionNextCycleLocalDate);
                } else {
                    totalNoOfDays = (int) ChronoUnit.DAYS.between(trialEndLocalDate, currentMonth25th);
                }
                log.info("User has been in the subscription from trial end date for {} days.", totalNoOfDays);
                return totalNoOfDays;
            }
        }

        log.info("No additional days calculated.");
        return 0;
    }

    @Override
    public List<Tuple> fetchPayoutForEachInstructor() {
        log.info("Fetching all payout amount for each instructors...");
        return repo.findAllPayoutAmountForEachInstructors();
    }

    @Override
    public void updatePayoutCalculationDate() {
        log.info("Updating the payout calculated at time...");
        repo.updateAllPayoutForCurrentPeriod();
    }
}
