package com.vinncorp.fast_learner.controllers.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseLevel;
import com.vinncorp.fast_learner.services.course.ICourseLevelService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.COURSE_LEVEL_API)
@RequiredArgsConstructor
public class CourseLevelController {

    private final ICourseLevelService service;

    @GetMapping(APIUrls.GET_ALL_COURSE_LEVEL)
    public ResponseEntity<Message<List<CourseLevel>>> fetchAllCourseLevel(Principal principal) throws EntityNotFoundException {
        Message<List<CourseLevel>> m = service.fetchAllCourseLevel(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
