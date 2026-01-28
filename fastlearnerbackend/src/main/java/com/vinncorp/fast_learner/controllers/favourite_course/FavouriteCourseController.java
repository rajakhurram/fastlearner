package com.vinncorp.fast_learner.controllers.favourite_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.favourite_course.FavouriteCourseResponse;
import com.vinncorp.fast_learner.services.favourite_course.IFavouriteCourseService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.FAVOURITE_COURSE_API)
@RequiredArgsConstructor
public class FavouriteCourseController {

    private final IFavouriteCourseService service;

    @PostMapping(APIUrls.CREATE_FAVOURITE_COURSE)
    public ResponseEntity<Message<String>> create(@Valid @NotNull @RequestParam Long courseId, Principal principal)
            throws InternalServerException, EntityNotFoundException {
        Message<String> m = service.create(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_FAVOURITE_COURSES)
    public ResponseEntity<Message<FavouriteCourseResponse>> getFavouriteCourses(
            @RequestParam(required = false) String title,
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            Principal principal
    ) throws EntityNotFoundException {
        Message<FavouriteCourseResponse> m = service.getAllFavouriteCourses(title, pageSize, pageNo, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
