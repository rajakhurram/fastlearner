package com.vinncorp.fast_learner.services.payout;

import com.vinncorp.fast_learner.dtos.payout.PaidUser;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.repositories.user.UserCourseProgressRepository;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@AllArgsConstructor
public class PayoutCalculationService implements IPayoutCalculationService{

    private final IPayoutWatchTimeService payoutWatchTimeService;
    private final IInstructorSalesService instructorSalesService;
    private final ISubscribedUserService subscribedUserService;
    private final UserCourseProgressRepository userCourseProgressRepo;
    public final Map<Long, InstructorSales> instructorSales = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Number of threads to use


    @Override
    public void calculateAllInstructorsPayout() {
        log.info("Calculating the payouts...");
        Date creationDate = new Date();
        List<PaidUser> listOfUsers = subscribedUserService.fetchAllPaidSubscriptionAfterTrialPeriod();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (PaidUser user : listOfUsers) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                processUser(user, creationDate);
                return null;
            }, executorService);
            futures.add(future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.thenRun(() -> populateIntoDb());
        allFutures.join();
        executorService.shutdown();
    }

    public void processUser(PaidUser user, Date creationDate) {
        List<Tuple> instructors = userCourseProgressRepo.fetchAllInstructorSalesByStudentId(user.getUserId(), user.getSubscriptionFee());
        if (CollectionUtils.isEmpty(instructors)) {
            log.warn("No data found for processing the sales for student: " + user.getUserId());
            return;
        }

        for (Tuple tuple : instructors) {
            log.info("Calculating payout for instructor ID: " +tuple.get("instructor_id") + " by student ID: " + user.getUserId());
            saveInMap(tuple, creationDate);
        }
    }

    public void saveInMap(Tuple tuple, Date creationDate) {
        Long instructorId = Long.parseLong("" + tuple.get("instructor_id"));
        Double sales = Double.parseDouble(Objects.isNull(tuple.get("relative_seek_time")) ? "0" : "" + tuple.get("relative_seek_time"));
        String stripeAccountId = (String) tuple.get("stripe_account_id");

        instructorSales.compute(instructorId, (id, existingSale) -> {
            if (existingSale == null) {
                return InstructorSales.builder()
                        .creationDate(creationDate)
                        .instructorId(instructorId)
                        .stripeAccountId(stripeAccountId)
                        .status(PayoutStatus.PENDING)
                        .totalSales(sales)
                        .build();
            } else {
                return InstructorSales.builder()
                        .creationDate(creationDate)
                        .instructorId(instructorId)
                        .stripeAccountId(stripeAccountId)
                        .status(PayoutStatus.PENDING)
                        .totalSales(sales + existingSale.getTotalSales())
                        .build();
            }
        });
    }

    public void populateIntoDb() {
        log.info("Populating into db.");
        int batchSize = Math.min(instructorSales.size(), 100);
        while(instructorSales.size() > 0) {
            try {
                log.info("Saving batch of " + batchSize + " items into database.");
                instructorSalesService.saveAll(fetchInBatch(batchSize));
            } catch (InternalServerException e) {
                log.error("PAYOUT CALCULATION ERROR: " + e.getLocalizedMessage());
            }
        }
    }

    private List<InstructorSales> fetchInBatch(int batchSize) {
        List<InstructorSales> removedItems = new ArrayList<>();
        Iterator<Map.Entry<Long, InstructorSales>> iterator = instructorSales.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext() && count < batchSize) {
            Map.Entry<Long, InstructorSales> entry = iterator.next();
            removedItems.add(entry.getValue());
            iterator.remove(); // Remove the entry from the map
            count++;
        }
        return removedItems;
    }

    /**
     * Payout for subscription based course will be calculated by below scheduled method. After calculation it will persist
     * the data into instructor sales table from which withdrawal process will be done.
     * */
    @Scheduled(cron = "0 0 0 25 * ?")
    @Override
    public void subscriptionBasedPayoutProcess() {
        log.info("Subscription based payout processing started...");
        List<Tuple> payouts = payoutWatchTimeService.fetchPayoutForEachInstructor();
        if(payouts.isEmpty()) {
            log.error("No payouts present for sending.");
            return;
        }
        Date currentDate = new Date();
        List<InstructorSales> instructorSales = new ArrayList<>();
        for (Tuple tuple : payouts) {
            instructorSales.add(InstructorSales.builder()
                    .stripeAccountId(tuple.get("stripe_id", String.class))
                    .status(PayoutStatus.PENDING)
                    .instructorId(tuple.get("instructor_id", Long.class))
                    .totalSales(tuple.get("overall_total_amount", Double.class))
                    .creationDate(currentDate)
                    .build());
        }
        try {
            instructorSalesService.saveAll(instructorSales);
            // Update the payout_watch_time with payout_calculation_date
            payoutWatchTimeService.updatePayoutCalculationDate();
        } catch (InternalServerException e) {
            log.error("PAYOUT CALCULATION ERROR: " + e.getLocalizedMessage());
        }
    }
}
