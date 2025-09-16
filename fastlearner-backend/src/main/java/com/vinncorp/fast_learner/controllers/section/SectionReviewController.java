package com.vinncorp.fast_learner.controllers.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.section.CreateSectionReviewRequest;
import com.vinncorp.fast_learner.response.section.SectionReviewResponse;
import com.vinncorp.fast_learner.services.section.ISectionReviewService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.SECTION_REVIEW)
@RequiredArgsConstructor
public class SectionReviewController {

    private final ISectionReviewService service;

    @PostMapping(APIUrls.CREATE_SECTION_REVIEW)
    public ResponseEntity<Message<String>> createSectionReview(@RequestBody CreateSectionReviewRequest request, Principal principal)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        Message<String> m = service.createSectionReview(request, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_SECTION_REVIEW)
    public ResponseEntity<Message<SectionReviewResponse>> getSectionReviewBySection(@PathVariable Long sectionId, Principal principal)
            throws BadRequestException, EntityNotFoundException {
        Message<SectionReviewResponse> m = service.findBySectionId(sectionId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
