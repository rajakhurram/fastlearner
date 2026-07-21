package com.vinncorp.fast_learner.controllers.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.course.CourseVisitorResponse;
import com.vinncorp.fast_learner.services.course.ICourseVisitorService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.COURSE_VISITOR)
@RequiredArgsConstructor
public class CourseVisitorController {

    private final ICourseVisitorService courseVisitorService;

    @GetMapping(APIUrls.COURSE_VISITOR_GET_ALL)
    public ResponseEntity<Message<List<CourseVisitorResponse>>> fetchAllCourseVisitors(
            @RequestParam(required = false) Long courseId,
            Principal principal) throws EntityNotFoundException {
        var m = courseVisitorService.fetchAllVisitors(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
