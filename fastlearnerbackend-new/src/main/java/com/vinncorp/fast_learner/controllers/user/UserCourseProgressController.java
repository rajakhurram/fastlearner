package com.vinncorp.fast_learner.controllers.user;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.user.CreateUserCourseProgressRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.ActiveStudentsResponse;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.USER_COURSE_PROGRESS)
@RequiredArgsConstructor
public class UserCourseProgressController {

    private final IUserCourseProgressService service;

    @PostMapping(APIUrls.CREATE_COURSE_PROGRESS)
    public ResponseEntity<Message<String>> markComplete(
            @RequestBody CreateUserCourseProgressRequest request,
            Principal principal)
            throws InternalServerException, BadRequestException, EntityNotFoundException {
        var m = service.markComplete(request, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_COURSE_PROGRESS)
    public ResponseEntity<Message<Double>> fetchCourseProgress(
            @PathVariable("courseId") Long courseId, Principal principal) throws EntityNotFoundException {
        var m = service.fetchCourseProgress(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_ALL_ACTIVE_STUDENTS)
    public ResponseEntity<Message<List<ActiveStudentsResponse>>> getAllActiveStudentsByCourseIdOrInstructorId(
            @RequestParam(required = false) Long courseId,
            Principal principal
    ) throws EntityNotFoundException, BadRequestException {
        var m = this.service.getAllActiveStudentsByCourseIdOrInstructorId(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
