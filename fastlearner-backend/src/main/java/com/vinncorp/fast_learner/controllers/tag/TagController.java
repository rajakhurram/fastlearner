package com.vinncorp.fast_learner.controllers.tag;

import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.services.tag.ITagService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.TAG_API)
@RequiredArgsConstructor
public class TagController {

    private final ITagService service;

    @GetMapping(APIUrls.GET_ALL_TAGS_BY_NAME)
    public ResponseEntity<Message<List<Tag>>> getAllTagByName(@RequestParam String name) throws EntityNotFoundException, BadRequestException {
        Message<List<Tag>> m = service.fetchTageByName(name);
        return ResponseEntity.ok(m);
    }

    @GetMapping(APIUrls.GET_ALL_TAGS_BY_COURSE)
    public ResponseEntity<Message<List<Tag>>> getAllTagsByCourseId(
            @Valid @NotNull(message = "courseId should not be null") @PathVariable Long courseId,
            Principal principal) throws EntityNotFoundException {
        var m = service.fetchTagsByCourse(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

}
