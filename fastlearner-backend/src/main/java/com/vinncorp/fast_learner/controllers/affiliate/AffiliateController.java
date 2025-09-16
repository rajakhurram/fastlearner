package com.vinncorp.fast_learner.controllers.affiliate;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.request.affiliate.CreateAffiliateReq;
import com.vinncorp.fast_learner.services.affiliate.affiliate_service.IAffiliateService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(APIUrls.AFFILIATE)
@RequiredArgsConstructor
public class AffiliateController {

    @Value("${frontend.domain.url}")
    private String DOMAIN_URL;

    private final IAffiliateService service;

    @PostMapping(APIUrls.CREATE_AFFILIATE_USER)
    public ResponseEntity<?> createAffiliate(@Valid @RequestBody CreateAffiliateReq request, Principal principal) throws EntityNotFoundException, StripeException, JsonProcessingException {
        var m = this.service.createAffiliateUser(request,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.FETCH_AFFILIATE_USER)
    public ResponseEntity<?> getAffiliateByInstructor(@RequestParam(required = false) String search , Principal principal,
    @RequestParam int pageNo,
    @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var m = this.service.getAffiliateByInstructor(search ,principal,pageable);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_AFFILIATE_USER)
    public ResponseEntity<?> deleteAffiliate(@RequestParam Long instructorAffiliateId, Principal principal) throws EntityNotFoundException {
        var m = this.service.deleteAffiliateByInstructor(instructorAffiliateId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PutMapping(APIUrls.UPDATE_AFFILIATE_USER)
    public ResponseEntity<?> updateAffiliate(@Valid @RequestBody CreateAffiliateReq request, Principal principal) throws EntityNotFoundException {
        var m = this.service.updateAffiliateUser(request,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.AFFILIATE_DETAIL_BY_USER_ID)
    public ResponseEntity<?> getAffiliateDetailByInstructor(@RequestParam Long instructorAffiliateId , Principal principal) {
        var m = this.service.getAffiliateUserByAffiliateId(instructorAffiliateId ,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.STRIPE_RESEND_LINK)
    ResponseEntity<Message<String>> stripeResendLinkForAffiliate(@RequestParam String email, Principal principal) throws StripeException, JsonProcessingException {
        var m = this.service.stripeResendLinkForAffiliate(email,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.STRIPE_REDIRECT_URL)
    ResponseEntity<Message<String>> stripeResendLinkForAffiliate(@RequestParam String accountUrl, @RequestParam String status, HttpServletResponse response) throws IOException {
        var m = this.service.stripeRedirectUrlForAffiliate(accountUrl,status);
        String redirectUrl = DOMAIN_URL; // Website URL to redirect the user
        response.sendRedirect(redirectUrl);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
