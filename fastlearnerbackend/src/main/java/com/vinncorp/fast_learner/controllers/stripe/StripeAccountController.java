package com.vinncorp.fast_learner.controllers.stripe;

import com.stripe.exception.StripeException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.stripe.AccountDetailResponse;
import com.vinncorp.fast_learner.response.stripe.PaymentWithdrawalHistoryResponse;
import com.vinncorp.fast_learner.services.stripe.IPaymentWithdrawalHistoryService;
import com.vinncorp.fast_learner.services.stripe.IStripeAccountService;
import com.vinncorp.fast_learner.services.stripe.IStripeService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.STRIPE_ACCOUNT)
@RequiredArgsConstructor
public class StripeAccountController {

    private final IStripeAccountService service;
    private final IStripeService stripeService;
    private final IPaymentWithdrawalHistoryService paymentWithdrawalHistoryService;

    @GetMapping(APIUrls.STRIPE_ACCOUNT_CREATE)
    public ResponseEntity<Message<String>> connectAccount(Principal principal) throws StripeException, InternalServerException, EntityNotFoundException {
        var m = service.createAccountLink(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.STRIPE_ACCOUNT_DETAILS)
    public ResponseEntity<Message<AccountDetailResponse>> fetchConnectedAccountDetails(Principal principal)
            throws InternalServerException, EntityNotFoundException {
        var m = service.fetchAccountDetailByEmail(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.STRIPE_ACCOUNT_DELETE)
    public ResponseEntity<Message<String>> deleteConnectedAccount(Principal principal)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        var m = service.deleteAccountByEmail(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.STRIPE_ACCOUNT_WITHDRAW)
    public ResponseEntity<Message<String>> withdrawAmount(
            @RequestParam String bankName,
            @RequestParam(required = true) double amount,
            Principal principal)
            throws EntityNotFoundException {
        var m = stripeService.sendPayoutToExternalAccount(bankName, principal.getName(), amount);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.STRIPE_ACCOUNT_HISTORY)
    public ResponseEntity<Message<PaymentWithdrawalHistoryResponse>> fetchConnectedAccountHistory(
            @RequestParam int pageNo, @RequestParam int pageSize,
            Principal principal)
            throws EntityNotFoundException {
        var m = paymentWithdrawalHistoryService.fetchConnectedAccountHistory(principal.getName(), pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
