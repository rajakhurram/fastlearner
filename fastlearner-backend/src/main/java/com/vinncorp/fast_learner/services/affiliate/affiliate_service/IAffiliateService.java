package com.vinncorp.fast_learner.services.affiliate.affiliate_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.affiliate.Affiliate;
import com.vinncorp.fast_learner.request.affiliate.CreateAffiliateReq;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface IAffiliateService {
    Message createAffiliateUser(CreateAffiliateReq request, Principal principal) throws EntityNotFoundException, StripeException, JsonProcessingException;

    Message getAffiliateByInstructor(String search ,Principal principal, Pageable pageable);

    Message deleteAffiliateByInstructor(Long affiliateId);

    Message updateAffiliateUser(CreateAffiliateReq request,Principal principal);

    Message getAffiliateUserByAffiliateId(Long instructorAffiliateId, Principal principal);

    Message stripeResendLinkForAffiliate(String email, Principal principal) throws StripeException, JsonProcessingException;
    Affiliate findById(Long id) throws EntityNotFoundException;

    Message stripeRedirectUrlForAffiliate(String accountUrl, String status);
}
