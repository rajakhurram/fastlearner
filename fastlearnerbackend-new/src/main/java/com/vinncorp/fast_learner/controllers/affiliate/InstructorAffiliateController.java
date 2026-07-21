package com.vinncorp.fast_learner.controllers.affiliate;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliatePremiumCourse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.services.affiliate.instructor_affiliate_service.IInstructorAffiliateService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.INSTRUCTOR_AFFILIATE)
public class InstructorAffiliateController {
    private final IInstructorAffiliateService service;

    public InstructorAffiliateController(IInstructorAffiliateService service) {
        this.service = service;
    }

    @GetMapping(APIUrls.PREMIUM_COURSES_WITH_AFFILIATE_REWARD)
    public ResponseEntity<Message<AffiliatePremiumCourse>> getPremiumCoursesWithAffiliateReward(
            @RequestParam Long affiliateId,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = this.service.getPremiumCoursesWithAffiliateReward(affiliateId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
