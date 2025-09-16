package com.vinncorp.fast_learner.services.affiliate.payout_premium_course;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.net.RequestOptions;
import com.stripe.param.PayoutCreateParams;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutTransactionHistory;
import com.vinncorp.fast_learner.repositories.payout.premium_course.PremiumCoursePayoutTransactionHistoryRepository;
import com.vinncorp.fast_learner.services.payout.premium_course.InstructorAffiliatePayoutService;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import com.vinncorp.fast_learner.util.enums.StripeAccountStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutPremiumCourseService {
    private final PremiumCoursePayoutTransactionHistoryRepository repo;

    private final InstructorAffiliatePayoutService service;

    @Value("${stripe.secret.key}")
    String API_KEY;
//    @Scheduled(cron = "*/1 * * * * ?")
//Run on 25th of every month at 12:00 AM
    @Scheduled(cron = "0 0 0 25 * ?")
    //self affiliate not working
    public void premiumCourseFundTransfer() {
        // Retrieve transaction histories excluding certain payout statuses
        List<PayoutStatus> excludedStatuses = Arrays.asList(PayoutStatus.SYSTEM_ERROR, PayoutStatus.PROCESSED);
        List<PremiumCoursePayoutTransactionHistory> transactionHistories = repo.
                findByPayoutStatusNotInAndStripeAccountStatus(excludedStatuses, StripeAccountStatus.CONNECTED);

        if (!transactionHistories.isEmpty()) {
            // Initialize maps for payouts
            Map<String, Double> payoutMap = new HashMap<>();
            Map<String, String> stripeAccPayoutId = new HashMap<>();
            Map<String, List<Long>> stripeAccTransId = new HashMap<>();


            transactionHistories.forEach(transaction -> {
                String stripeAccId = null;

                if (transaction.getPayoutType().equals(PayoutType.AFFILIATE)) {
                    if (transaction.getInstructorAffiliate() == null) {
                        // Instructor payout condition
                        if (transaction.getCourse().getInstructor().getStripeAccountId() != null) {
                            stripeAccId = transaction.getCourse().getInstructor().getStripeAccountId();
                        }
                    } else {
                        // Affiliate payout condition
                        if (transaction.getInstructorAffiliate().getAffiliateUser().getStripeAccountId() != null &&
                                PayoutStatus.ACTIVATED.equals(transaction.getInstructorAffiliate().getAffiliateUser().getOnboardStatus())) {
                            stripeAccId = transaction.getInstructorAffiliate().getAffiliateUser().getStripeAccountId();
                        }
                    }
                } else {
//                    if (transaction.getPayoutType().equals(PayoutType.DIRECT))
                    // Direct and Self payout condition
                    if (transaction.getCourse().getInstructor().getStripeAccountId() != null) {
                        stripeAccId = transaction.getCourse().getInstructor().getStripeAccountId();
                    }
                }
                // Add to payoutMap and transaction ID map if stripeAccId is valid
                if (stripeAccId != null) {
                    payoutMap.merge(stripeAccId, transaction.getAmount(), Double::sum);
                    stripeAccTransId.computeIfAbsent(stripeAccId, k -> new ArrayList<>()).add(transaction.getId());
                    log.info("Added transaction ID {} to Stripe account ID {}", transaction.getId(), stripeAccId);
                }
            });

            // Process payouts for each stripeAccId
            payoutMap.forEach((stripeAccountId, totalAmount) -> {
                List<Long> transactionListId= stripeAccTransId.get(stripeAccountId);
                try {

                    String payoutId = processPayout(stripeAccountId, totalAmount); // Call payout method
                    stripeAccPayoutId.put(stripeAccountId, payoutId);
                    log.info("Processed payout for Stripe account ID {}: Amount = {}, Payout ID = {}",
                            stripeAccountId, totalAmount, payoutId);

                    this.markTransactionsProcessed(transactionListId,payoutId, PayoutStatus.PROCESSED,"SETTLED");
                } catch (StripeException e) {
                    log.error("Failed to process payout for Stripe account ID {}: {}", stripeAccountId, e.getMessage());
                    this.markTransactionsProcessed(transactionListId,null, PayoutStatus.FAILED,e.getMessage());
//                    throw new RuntimeException(e);
                }
             });

            // Log all transaction IDs mapped to each Stripe account
            stripeAccTransId.forEach((accountId, transactionIds) ->
                    log.info("Stripe Account ID {}: Transactions = {}", accountId, transactionIds)
            );
        } else {
            log.info("No transactions found for processing.");
        }
    }

    //process the payout amount
    private String processPayout(String stripeId, Double amount) throws StripeException {
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
//        String payoutId=service.sendPayout(stripeAccountId,totalAmount);
//        String payoutId="abc1223";
//        return payoutId;
    }
    @Transactional
    public int markTransactionsProcessed(List<Long> successfulTransactionIds,String payoutId, PayoutStatus payoutProcess,String stripeResponse) {
        return repo.markTransactionsAsProcessed(successfulTransactionIds,payoutId, payoutProcess.name(),stripeResponse);
    }

}
