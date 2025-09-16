package com.vinncorp.fast_learner.services.payout;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.net.RequestOptions;
import com.stripe.param.PayoutCreateParams;
import com.vinncorp.fast_learner.models.payout.PayoutWatchTime;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.payout.PayoutWatchTimeRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerSubscriptionPayoutService {
    @Value("${stripe.secret.key}")
    String API_KEY;
    private final PayoutWatchTimeRepository payoutWatchTimeRepository;
    private final UserRepository userRepository;

    //    @Scheduled(cron = "0 0 0 25 * *") // 25 date of every month
    @Scheduled(cron = "0 0 * * * *")// every hour for testing purpose
    public void InstructorPayoutForSubscription(){
        String payoutStatus=PayoutStatus.PROCESSED.name();
        log.info("Starting scheduled instructor payout job.");

        Map<String, Double> stripeAccAndAmount= new HashMap<>();
        Map<String, List<Long>> stripeAccTransId = new HashMap<>();
        Map<String, String> stripeAccPayoutId = new HashMap<>();


        List<PayoutWatchTime> payoutWatchTime= payoutWatchTimeRepository.findPendingPayoutsWithinLastThreeMonths(payoutStatus,new Date());
        log.info("Total pending payout records found: {}", payoutWatchTime.size());
            payoutWatchTime.forEach(payoutWatchTime1 -> {
                Optional<User> optionalUser = userRepository.findById(payoutWatchTime1.getInstructorId());
                if (optionalUser.isEmpty()) {
                    log.warn("User with ID {} not found. Skipping.", payoutWatchTime1.getInstructorId());
                    return;
                }
                User user = optionalUser.get();
                if (StringUtils.hasText(user.getStripeAccountId())){
                    stripeAccAndAmount.merge(user.getStripeAccountId(), payoutWatchTime1.getAmountSharePerDay(), Double::sum);
                    stripeAccTransId.computeIfAbsent(user.getStripeAccountId(), k -> new ArrayList<>())
                            .add(payoutWatchTime1.getId());
                    log.info("Prepared payout data for user {}: StripeAcc={}, Amount={}", user.getId(), user.getStripeAccountId(), payoutWatchTime1.getAmountSharePerDay());
                }else {
                    log.warn("User {} does not have a Stripe account. Skipping payout.", user.getId());
                }
            });
        stripeAccAndAmount.forEach((stripeAccId, totalAmount) -> {
            List<Long> transactionListId= stripeAccTransId.get(stripeAccId);
            try {
                log.info("Processing payout for Stripe account {}: TotalAmount={}", stripeAccId, totalAmount);
                String payoutId = processPayout(stripeAccId, totalAmount); // Call payout method
                stripeAccPayoutId.put(stripeAccId, payoutId);
                this.markPayoutTransactionsProcessed(transactionListId,payoutId, PayoutStatus.PROCESSED,"SETTLED");
                log.info("Payout successful for Stripe account {}. Payout ID: {}", stripeAccId, payoutId);
            } catch (StripeException e) {
                this.markPayoutTransactionsProcessed(transactionListId,null, PayoutStatus.FAILED,e.getMessage());
                log.error("Payout failed for Stripe account {}. Error: {}", stripeAccId, e.getMessage());
//                throw new RuntimeException(e);
            }
        });
        log.info("Instructor payout job completed.");
    }

    public void markPayoutTransactionsProcessed(List<Long> transactionListId, String payoutId, PayoutStatus processed, String settled) {
       int payoutTransactionId= payoutWatchTimeRepository.updatePayoutDetails(processed.name(),payoutId, LocalDateTime.now(),settled,transactionListId);
        log.info("Marked transactions as {}. PayoutTransactionId: {}", processed.name(), payoutTransactionId);

    }

    //process the payout amount
    public String processPayout(String stripeId, Double amount) throws StripeException {
            // Logic to call Stripe API or your payout processing system
            long total = Math.round(amount * 100);
            Stripe.apiKey = API_KEY; // Load from environment variable
            PayoutCreateParams payoutParams = PayoutCreateParams.builder()
                    .setAmount(total) // Amount in cents
                    .setCurrency("usd") // Currency code, e.g., "usd"
                    .setMethod(PayoutCreateParams.Method.STANDARD) // Payout method
                    .build();

            // Initiate payout on behalf of the connected account
            var payout = Payout.create(payoutParams,
                    RequestOptions.builder().setStripeAccount(stripeId).build());
            System.out.println("Processing payout to Stripe Account: " + stripeId + " with amount: " + amount);

            return payout.getId();

        }


}
