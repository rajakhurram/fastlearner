package com.vinncorp.fast_learner.controllers.newsletter_subscription;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.newsletter_subscription.INewsletterSubscriptionService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIUrls.NEWSLETTER_SUBSCRIPTION)
@RequiredArgsConstructor
public class NewsletterSubscriptionController {

    private final INewsletterSubscriptionService service;

    @PostMapping(APIUrls.SUBSCRIBE_NEWSLETTER)
    public ResponseEntity<Message<String>> subscribeNewsletter(@Valid @NotBlank @Email @RequestParam String email)
            throws InternalServerException, EntityAlreadyExistException, BadRequestException {
        var m = service.subscribeToNewsletter(email);
        return ResponseEntity.ok(m);
    }
}
