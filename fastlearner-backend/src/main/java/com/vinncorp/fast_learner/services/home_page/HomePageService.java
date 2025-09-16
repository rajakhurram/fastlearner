package com.vinncorp.fast_learner.services.home_page;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.course.CourseByCategoryRequest;
import com.vinncorp.fast_learner.request.homepage.ViewAllReq;
import com.vinncorp.fast_learner.response.course.CourseDetailByPaginatedResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorPaginatedResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorResponse;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.course.ICourseCategoryService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomePageService implements IHomePageService{
    private final ICourseService courseService;

    private final UserService userService;

    private final ICourseCategoryService courseCategoryService;

    @Override
    public Message<CourseDetailByPaginatedResponse> getAllNewCourses(int pageNo, int pageSize,Principal principal) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return courseService.getAllNewCourse(pageable,principal);
    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getAllFreeCourses(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return courseService.getAllFreeCourse(pageable);
    }

    @Override
    public Message<InstructorPaginatedResponse> getTopInstructor(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return userService.getAllTopInstructor(pageable);
    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getTrendingCourses(int pageNo, int pageSize,Principal principal) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return courseService.getAllTrendingCourses(pageable,principal);
    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getAllPremiumCourses(int pageNo, int pageSize,Principal principal) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return courseService.getAllPremiumCourses(pageable,principal);
    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getByType( ViewAllReq viewAllReq,int pageNo, int pageSize, Principal principal) throws EntityNotFoundException {
        log.info("Received request: {}", viewAllReq);
        log.info("Pagination details: pageNo={}, pageSize={}", pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        String singleCategory="";
        Long rating;
        List<String> categoryList=new ArrayList<>();
            if (!viewAllReq.getCategoriesId().isEmpty() && viewAllReq.getCategoriesId().get(0) != null) {
                log.info("Processing category IDs: {}", viewAllReq.getCategoriesId());

                categoryList = viewAllReq.getCategoriesId().stream()
                        .map(e -> {
                            try {
                                CourseCategory category = courseCategoryService.findById(e);
                                log.info("Found category with ID {}: {}", e, category.getName());
                                return category.getName();
                            } catch (EntityNotFoundException ex) {
                                log.error("Category with ID {} not found.", e, ex);
                                throw new RuntimeException("Category with ID " + e + " not found.", ex);
                            }
                        })
                        .collect(Collectors.toList());

                if (!categoryList.isEmpty()) {
                    log.info("Selected single category: {}", singleCategory);
                    singleCategory = categoryList.get(0);

                }
            }else {
                singleCategory = null;
                log.info("No categories specified. Defaulting singleCategory to null.");
            }

        Double minRating =0.0;
        Double maxRating= 0.9;
        if (viewAllReq.getRating()==null || viewAllReq.getRating().equals(0L)){
             minRating =0.0;
            maxRating= 5.0;
            log.info("No rating specified. Defaulting rating range to minRating={} and maxRating={}", minRating, maxRating);
        }else {
            rating= viewAllReq.getRating();
            minRating=rating+minRating;
            maxRating = (rating == 5L) ? 5.0 : rating + maxRating;
            log.info("Rating filter applied. Rating range: minRating={}, maxRating={}", minRating, maxRating);
        }

        User user = null;
        if (principal!=null){
            log.info("Fetching user details for principal: {}", principal.getName());
            user =userService.findByEmail(principal.getName());
            log.info("User details fetched: {}", user);
        }
        Long userId = (user != null) ? user.getId() : null;
        log.info("Final filters - Categories: {}, Single Category: {}, Course Type: {}, Feature: {}, Rating Range: {}-{}, User ID: {}, Pageable: {}",
                categoryList, singleCategory, viewAllReq.getCourseType(), viewAllReq.getFeature(), minRating, maxRating, userId, pageable);

        Message<CourseDetailByPaginatedResponse> response = courseService.findCoursesByFilter(
                categoryList, singleCategory, viewAllReq.getCourseType(), viewAllReq.getSearch(),
                viewAllReq.getFeature(), minRating, maxRating, userId, viewAllReq.getContentType(), pageable);

        if (response.getData() == null || response.getData().getData().isEmpty()) {
            log.warn("No courses found for the given filters.");
            throw new EntityNotFoundException("No courses found matching the criteria.");
        }
        return response;

    }

}
