package com.vinncorp.fast_learner.controllers.homepage;


import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.request.homepage.ViewAllReq;
import com.vinncorp.fast_learner.response.course.CourseDetailByPaginatedResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorPaginatedResponse;
import com.vinncorp.fast_learner.services.home_page.HomePageService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIUrls.HOME_PAGE)
@RequiredArgsConstructor
public class HomePageController {
    private final HomePageService service;

    @GetMapping(APIUrls.FETCH_NEW_COURSES)
    public ResponseEntity<Message<CourseDetailByPaginatedResponse>> getAllNewCourses(
            @RequestParam int pageNo,
            @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = service.getAllNewCourses(pageNo, pageSize,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_FREE_COURSES)
    public ResponseEntity<Message<CourseDetailByPaginatedResponse>> getAllFreeCourses(
            @RequestParam int pageNo,
            @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize) {
        var m = service.getAllFreeCourses(pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_TOP_INSTRUCTOR)
    public ResponseEntity<Message<InstructorPaginatedResponse>> getTopInstructor(
            @RequestParam int pageNo,
            @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize) {
        var m = service.getTopInstructor(pageNo, pageSize);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_TRENDING_COURSES)
    public ResponseEntity<Message<CourseDetailByPaginatedResponse>> getAllTrendingCourses(
            @RequestParam int pageNo,
            @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = service.getTrendingCourses(pageNo, pageSize,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_PREMIUM_COURSES)
    public ResponseEntity<Message<CourseDetailByPaginatedResponse>> getAllPremiumCourses(
            @RequestParam int pageNo,
            @RequestParam(defaultValue = "0") @Min(value = 1, message = "Page size must be greater than zero") int pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = service.getAllPremiumCourses(pageNo, pageSize,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.VIEW_ALL)
    public ResponseEntity<Message> getViewAll(@RequestBody ViewAllReq viewAllReq,
                                              Principal principal) throws EntityNotFoundException{
        var m = service.getByType(viewAllReq,viewAllReq.getPageNo(), viewAllReq.getPageSize(),principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }



}
