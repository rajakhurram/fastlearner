package com.vinncorp.fast_learner.controllers.enrollment;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.enrollment.EnrolledCourseResponse;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.ENROLLMENT)
@RequiredArgsConstructor
public class EnrollmentController {

    private final IEnrollmentService service;

    @PostMapping(APIUrls.CREATE_ENROLLMENT)
    public ResponseEntity<Message<String>> enrolled(Long courseId, Principal principal)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        var m = service.enrolled(courseId, principal.getName(), true);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_ENROLLMENT)
    public ResponseEntity<Message<EnrolledCourseResponse>> getAllEnrolledCourses(
            @RequestParam int sortBy,
            @RequestParam(required = false) String title,
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = service.getEnrolledCourseByUserId(sortBy, title, pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
