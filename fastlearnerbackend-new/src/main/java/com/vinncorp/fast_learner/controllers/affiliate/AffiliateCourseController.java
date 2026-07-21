package com.vinncorp.fast_learner.controllers.affiliate;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto;
import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseRequest;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.services.affiliate.affiliate_course_service.IAffiliateCourseService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.AFFILIATE_COURSE)
public class AffiliateCourseController {

    private final IAffiliateCourseService service;

    public AffiliateCourseController(IAffiliateCourseService service) {
        this.service = service;
    }

    @PostMapping(APIUrls.CREATE_AFFILIATE_COURSE)
    public ResponseEntity<Message<String>> assignCourseToAffiliate(@Valid @RequestBody List<AffiliateCourseRequest> affiliateCourseRequests, Principal principal) throws EntityNotFoundException, BadRequestException, InternalServerException, LimitExceedException {
        var m = service.assignCourseToAffiliate(affiliateCourseRequests, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_AFFILIATE_COURSES)
    public ResponseEntity<Message<Page<AffiliateCourseDto>>> getAllCoursesByAffiliate(
            @RequestParam(required = true) Long affiliateId,
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException {

        var m = service.getAllCoursesByAffiliate(
                affiliateId,
                GenericStatus.ACTIVE,
                PageRequest.of(pageNo, pageSize),
                principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_AFFILIATE_COURSE)
    public ResponseEntity<Message<String>> deleteAffiliateCourse(
            @RequestParam(required = true) Long affiliateId,
            @RequestParam(required = true) Long affiliateCourseId,
            Principal principal) throws EntityNotFoundException, AuthenticationException, BadRequestException {

        var m = this.service.deleteAffiliateCourse(affiliateId, affiliateCourseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_AFFILIATES_BY_COURSE)
    public ResponseEntity<Message<Page<AffiliateCourseDto>>> getAllAffiliatesByCourse(
            @RequestParam("courseId") Long courseId,
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) throws EntityNotFoundException {
        var m = this.service.getAllAffiliatesByCourse(courseId, principal.getName(),
                GenericStatus.ACTIVE, GenericStatus.ACTIVE,
                PageRequest.of(pageNo, pageSize));
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.ASSIGN_COURSE_ACTIVE_INACTIVE)
    public ResponseEntity<Message<?>> assignCourseActiveAndInActive(
            @RequestParam("affiliateCourseId") Long affiliateCourseId,
            @RequestParam("instructorAffiliateId") Long instructorAffiliateId,
            @RequestParam("status") GenericStatus status,
            Principal principal) throws BadRequestException, EntityNotFoundException, LimitExceedException {
        var m = this.service.updateAssignCourseActiveAndInActive(affiliateCourseId, instructorAffiliateId, status,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
