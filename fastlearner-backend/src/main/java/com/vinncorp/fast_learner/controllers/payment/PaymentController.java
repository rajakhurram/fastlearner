package com.vinncorp.fast_learner.controllers.payment;


import com.vinncorp.fast_learner.dtos.payment.BillingHistoryRequest;
import com.vinncorp.fast_learner.dtos.payment.BillingHistoryResponse;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailResponse;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.services.payment.IPaymentSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.CreateProces;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.PAYMENT_GATEWAY)
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentSubscriptionService paymentSubscriptionService;
    private final ISubscribedUserProfileService subscribedUserProfileService;

    private final CreateProces createProcessService;

    @PostMapping(APIUrls.CREATE_SUBSCRIPTION)
    public ResponseEntity<Message<String>> createSubscription(@Valid @RequestBody SubscriptionRequest requestDTO, Principal principal) throws InternalServerException, BadRequestException, EntityNotFoundException, EntityNotUpdateException {
        Message<String> m = paymentSubscriptionService.create(requestDTO, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
    @PostMapping(APIUrls.FREE_SUBSCRIPTION_FOR_SIGNUP)
    public ResponseEntity<Message<String>> freeSignUpSubscription(@RequestParam Long subscriptionId , Principal principal) throws InternalServerException, BadRequestException, EntityNotFoundException, EntityNotUpdateException {
        Message<String> m = paymentSubscriptionService.freeSignUpSubscription(subscriptionId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_SAVED_PAYMENT_PROFILE)
    public ResponseEntity<Message<PaymentProfileDetailResponse>> fetchSavedUser(Principal principal) throws InternalServerException,EntityNotFoundException {
        Message<PaymentProfileDetailResponse> m = subscribedUserProfileService.getDefaultSubscribedUserProfile(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_ALL_PAYMENT_PROFILE)
    public ResponseEntity<Message<List<PaymentProfileDetailResponse>>> getAllPaymentProfile(Principal principal) throws InternalServerException,EntityNotFoundException {
        Message<List<PaymentProfileDetailResponse>> m = subscribedUserProfileService.getAllPaymentProfiles(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.UPDATE_PAYMENT_PROFILE)
    public ResponseEntity<Message<String>> updatePaymentProfile(@Valid @RequestBody  PaymentProfileDetailRequest detailRequest, Principal principal) throws InternalServerException, EntityNotFoundException, EntityAlreadyExistException, BadRequestException {
        Message<String> m = subscribedUserProfileService.addUpdateCustomerProfile(principal.getName(),detailRequest);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.ADD_PAYMENT_PROFILE)
    public ResponseEntity<Message<String>> addPaymentProfile(@Valid @RequestBody  PaymentProfileDetailRequest detailRequest, Principal principal) throws InternalServerException, EntityNotFoundException, EntityAlreadyExistException, BadRequestException {
        Message<String> m = subscribedUserProfileService.addUpdateCustomerProfile(principal.getName(),detailRequest);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.DELETE_PAYMENT_PROFILE)
    public ResponseEntity<Message<String>> deletePaymentProfile(@Valid @PathVariable Long profileId) throws InternalServerException, EntityNotFoundException, BadRequestException {
        Message<String> m = subscribedUserProfileService.deleteSubscribedUserProfile(profileId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.GET_BILLING_HISTORY)
    public ResponseEntity<Message<List<BillingHistoryResponse>>> getBillingHistory(@Valid @RequestBody BillingHistoryRequest request, Principal principal) throws InternalServerException, EntityNotFoundException, BadRequestException {
        Message<List<BillingHistoryResponse>> m = paymentSubscriptionService.getBillingHistory(request,principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.UPDATE_SAVED_PAYMENT_PROFILE_STATUS)
    public ResponseEntity<Message<String>> updatePaymentProfileDefaultStatus(@Valid @RequestParam("id") Long profileId, @Valid @RequestParam("status") String status) throws InternalServerException,EntityNotFoundException {
        Message<String> m = subscribedUserProfileService.updatePaymentProfileDefaultStatus(profileId,Boolean.parseBoolean(status));
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_PAYMENT_PROFILE)
    public ResponseEntity<Message<PaymentProfileDetailResponse>> getPaymentProfileById(@Valid @PathVariable Long profileId) throws InternalServerException, EntityNotFoundException {
        Message<PaymentProfileDetailResponse> m = subscribedUserProfileService.getPaymentProfileById(profileId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.UPDATE_SUBSCRIPTION)
    public ResponseEntity<Message<String>> updateSubscriptionWithNewPaymentProfile(@Valid @RequestParam("paymentProfileId") Long paymentProfileId, Principal principal)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        var m = paymentSubscriptionService.updateSubscription(paymentProfileId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
