package com.vinncorp.fast_learner.controllers.payment;

import com.vinncorp.fast_learner.dtos.payment.checkout.ChargePayment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.payout.PremiumCoursePayoutTransactionHistoryResponse;
import com.vinncorp.fast_learner.services.payment.checkout.IPaymentCheckoutService;
import com.vinncorp.fast_learner.services.payout.premium_course.IPremiumCoursePayoutTransactionHistoryService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.COURSE_CHECKOUT_API)
@RequiredArgsConstructor
public class PaymentCheckoutController {

    private final IPaymentCheckoutService service;
    private final IPremiumCoursePayoutTransactionHistoryService payoutHistoryService;

    @PostMapping(APIUrls.CHARGE_PAYMENT)
    public ResponseEntity<Message<String>> chargePayment(@RequestBody ChargePayment chargePayment, Principal principal)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        var m = service.chargePayment(chargePayment, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.GET_ALL_PAYOUT_HISTORY)
    public ResponseEntity<Message<PremiumCoursePayoutTransactionHistoryResponse>> fetchAllPayoutHistoryByInstructor(
            @RequestParam("pageSize") int pageSize,
            @RequestParam("pageNo") int pageNo,
            Principal principal) throws EntityNotFoundException {
        Message<PremiumCoursePayoutTransactionHistoryResponse> m = payoutHistoryService.fetchAllPremiumCoursePayoutTransactionHistoryForInstructor(pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}