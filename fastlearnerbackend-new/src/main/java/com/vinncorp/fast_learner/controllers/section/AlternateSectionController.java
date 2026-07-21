package com.vinncorp.fast_learner.controllers.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.section.AlternateSectionResponse;
import com.vinncorp.fast_learner.services.section.IUserAlternateSectionService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.ALTERNATE_SECTION)
@RequiredArgsConstructor
public class AlternateSectionController {

    private final IUserAlternateSectionService service;

    @GetMapping(APIUrls.GET_ALL_ALTERNATE_SECTIONS)
    public ResponseEntity<Message<AlternateSectionResponse>> fetchAlternateSection(
            @RequestParam long courseId,
            @RequestParam long sectionId,
            @RequestParam(required = false, defaultValue = "0") int pageNo,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            Principal principal) throws BadRequestException, EntityNotFoundException {
        var m  = service.fetchAlternateSection(courseId, sectionId, pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.PIN_ALTERNATE_SECTION)
    public ResponseEntity<Message<String>> pinAlternateSection(
            @RequestParam long courseId,
            @RequestParam long sectionId,
            @RequestParam long fromSectionId,
            @RequestParam long fromCourseId,
            Principal principal
    ) throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = service.pinAlternateSection(courseId, sectionId, fromSectionId, fromCourseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.UNPIN_ALTERNATE_SECTION)
    public ResponseEntity<Message<String>> unpinAlternateSection(
            @RequestParam long courseId,
            @RequestParam long sectionId,
            Principal principal) throws EntityNotFoundException, InternalServerException {
        var m = service.unpinAlternateSection(courseId, sectionId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
