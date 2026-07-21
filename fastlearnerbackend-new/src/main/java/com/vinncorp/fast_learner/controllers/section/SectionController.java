package com.vinncorp.fast_learner.controllers.section;


import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.section.SectionDetailForUpdateResponse;
import com.vinncorp.fast_learner.response.section.SectionDetailResponse;
import com.vinncorp.fast_learner.services.section.ISectionService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.SECTION)
@RequiredArgsConstructor
public class SectionController {

    private final ISectionService service;

    @GetMapping(APIUrls.GET_SECTION)
    public ResponseEntity<Message<SectionDetailResponse>> fetchAllSectionByCourseId(
            @PathVariable("courseId") Long courseId, Principal principal) throws BadRequestException, EntityNotFoundException {
        Message<SectionDetailResponse> m = service.getAllSectionsByCourseId(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_SECTION_FOR_UPDATE)
    public ResponseEntity<Message<List<SectionDetailForUpdateResponse>>> fetchAllSectionForUpdate(@PathVariable Long courseId, Principal principal)
            throws EntityNotFoundException, BadRequestException {
        var m = service.fetchAllSectionForUpdate(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}

