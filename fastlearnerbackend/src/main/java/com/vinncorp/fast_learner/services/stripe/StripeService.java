package com.vinncorp.fast_learner.services.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.PayoutCreateParams;
import com.stripe.param.TransferCreateParams;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.stripe.PaymentWithdrawalHistory;
import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payout.IInstructorSalesService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class StripeService implements IStripeService{

    private final IPaymentWithdrawalHistoryService paymentWithdrawalHistoryService;
    private final IInstructorSalesService instructorSalesService;
    private final IUserService userService;

    public StripeService(@Value("${stripe.secret.key}") String API_KEY, IPaymentWithdrawalHistoryService paymentWithdrawalHistoryService,
                         IInstructorSalesService instructorSalesService, IUserService userService) {
        Stripe.apiKey = API_KEY;
        this.paymentWithdrawalHistoryService = paymentWithdrawalHistoryService;
        this.instructorSalesService = instructorSalesService;
        this.userService = userService;
    }


    // Method to handle payouts to multiple connected accounts concurrently
    @Scheduled(cron = "0 0 0 26 * ?")
    public void transferFundsToMultipleConnectedAccounts() {
        log.info("Transferring fund to stripe connected accounts of instructors...");
        // Use ConcurrentHashMap to store the result (thread-safe for concurrent access)
        Map<String, String> result = new ConcurrentHashMap<>();

        // Fetch all instructor sales via current month and status pending and stripe account id is not null
        try {
            List<InstructorSales> sales = instructorSalesService.findAllForPayoutProcess(PayoutStatus.PENDING);
            sales.parallelStream().forEach(request -> {
                try {
                    long total = Math.round(request.getTotalSales() * 100);
                    sendFundsToConnectedAccount(request.getStripeAccountId(), total);
                    request.setStatus(PayoutStatus.PROCESSED);
                    instructorSalesService.save(request);
                } catch (StripeException e) {
                    // Log error and store failure result
                    result.put(request.getStripeAccountId(), "Error: " + e.getMessage());
                    log.error("Error processing payout for account: " + request.getStripeAccountId(), e);
                }
            });
        } catch (EntityNotFoundException e) {
            log.error("No payouts for process.");
            return;
        }

    }

    // Transfer funds from your platform account to the connected account
    @Override
    public Transfer sendFundsToConnectedAccount(String connectedAccountId, long amount) throws StripeException {
        TransferCreateParams transferParams = TransferCreateParams.builder()
                .setAmount(amount) // Amount in cents
                .setCurrency("usd") // Currency code, e.g., "usd"
                .setDestination(connectedAccountId) // The connected account's ID
                .build();

        return Transfer.create(transferParams);
    }

    // Create a payout from the connected account's balance to their bank or debit card
    @Override
    public Message<String> sendPayoutToExternalAccount(String bankName, String email, double amount) throws EntityNotFoundException {
        long total = Math.round(amount * 100);
        User user = userService.findByEmail(email);
        if(Objects.isNull(user.getStripeAccountId()))
            throw new EntityNotFoundException("No account is connected, please connect a stripe connect account.");
        String errorMessage;
        try {
            PayoutCreateParams payoutParams = PayoutCreateParams.builder()
                    .setAmount(total) // Amount in cents
                    .setCurrency("usd") // Currency code, e.g., "usd"
                    .setMethod(PayoutCreateParams.Method.STANDARD) // Payout method
                    .build();

            // Initiate payout on behalf of the connected account
            var payout = Payout.create(payoutParams,
                    RequestOptions.builder().setStripeAccount(user.getStripeAccountId()).build());

            paymentWithdrawalHistoryService.save(PaymentWithdrawalHistory.builder()
                            .bankName(bankName)
                            .amount(amount)
                            .payoutId(payout.getId())
                            .withdrawalAt(new Date())
                            .user(user)
                    .build());

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Withdrawal of amount $" + amount + " has been completed.")
                    .setData("Withdrawal of amount $" + amount + " has been completed.");
        } catch (StripeException e) {
            log.error("Stripe payout to external account issue: " + e.getMessage());
            errorMessage = e.getMessage();
        } catch (InternalServerException e) {
            log.error(e.getMessage());
            errorMessage = e.getMessage();
        }
        return new Message<String>()
                .setMessage(errorMessage)
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setCode(HttpStatus.BAD_REQUEST.name())
                .setData(errorMessage);
    }
}
