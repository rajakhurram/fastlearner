package com.vinncorp.fast_learner.controllers.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.services.course.ICourseCategoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(APIUrls.COURSE_CATEGORY_API)
@RequiredArgsConstructor
public class CourseCategoryController {

    private final ICourseCategoryService service;

    @GetMapping(APIUrls.GET_ALL_COURSE_CATEGORY)
    public ResponseEntity<Message<List<CourseCategory>>> fetchAllCourseCategory() throws EntityNotFoundException {
        Message<List<CourseCategory>> m = service.fetchAllCourseCategory();
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
