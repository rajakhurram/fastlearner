package com.vinncorp.fast_learner.controllers.subscription;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.subscription.CurrentSubscriptionResponse;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.SUBSCRIPTION_API)
@RequiredArgsConstructor
public class SubscriptionController {

    private final ISubscriptionService service;
    private final ISubscribedUserService subscribedUserService;

    @GetMapping(APIUrls.GET_ALL_SUBSCRIPTION)
    public ResponseEntity<Message<List<Subscription>>> fetchAllSubscription(Principal principal) throws EntityNotFoundException {
        Message<List<Subscription>> m = service.fetchAllSubscription(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_SUBSCRIPTION_BY_ID)
    public ResponseEntity<Message<Subscription>> fetchSubscriptionById(@PathVariable Long subscriptionId, Principal principal) throws EntityNotFoundException {
        return ResponseEntity.ok(service.findBySubscriptionId(subscriptionId));
    }

    @GetMapping(APIUrls.GET_CURRENT_SUBSCRIPTION)
    public ResponseEntity<Message<CurrentSubscriptionResponse>> getCurrentSubscription(Principal principal) throws EntityNotFoundException {
        Message<CurrentSubscriptionResponse> m = subscribedUserService.getCurrentSubscription(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.CANCEL_SUBSCRIPTION)
    public ResponseEntity<Message<String>> cancelSubscription(Principal principal)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        Message<String> m = subscribedUserService.cancelSubscription(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.VERIFY_SUBSCRIPTION)
    public ResponseEntity<Message<Boolean>> verifySubscription(Principal principal) throws EntityNotFoundException {
        Message<Boolean> m = service.isSubscibed(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
