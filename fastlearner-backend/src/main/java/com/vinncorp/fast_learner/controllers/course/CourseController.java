package com.vinncorp.fast_learner.controllers.course;

import com.vinncorp.fast_learner.config.ApiSecretFilter;
import com.vinncorp.fast_learner.dtos.course.CourseDropdown;
import com.vinncorp.fast_learner.dtos.course.CourseSearchInput;
import com.vinncorp.fast_learner.dtos.course.UniqueCourseTitle;
import com.vinncorp.fast_learner.es_dto.CourseAutoComplete;
import com.vinncorp.fast_learner.es_dto.CourseContentResponse;
import com.vinncorp.fast_learner.es_models.CourseContent;
import com.vinncorp.fast_learner.es_services.IESCourseService;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.CreateCourseValidationException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.response.course.*;
import com.vinncorp.fast_learner.response.subscriptionpermission.SubscriptionPermissionResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.request.course.CourseByCategoryRequest;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.request.course.RelatedCoursesRequest;
import com.vinncorp.fast_learner.request.course.SearchCourseRequest;
import com.vinncorp.fast_learner.services.course.course_review.ICourseReviewService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.*;


@RestController
@RequestMapping(APIUrls.COURSE_API)
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService service;
    private final IESCourseService esService;
    private final ICourseReviewService crService;
    @PostMapping(APIUrls.CREATE_COURSE)
    public ResponseEntity<Message<CreateCourseRequest>> createCourse(@Valid @RequestBody CreateCourseRequest request,
                @RequestHeader(value = ApiSecretFilter.CLIENT_ID_HEADER, required = false) String clientId
            , Principal principal)

            throws EntityNotFoundException, InternalServerException, BadRequestException, IOException, CreateCourseValidationException {
        var m = service.createCourse(request,clientId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_PREMIUM_COURSE_AVAILABLE)
    public ResponseEntity<Message<SubscriptionPermissionResponse>> getPremiumCourseAvailable(Principal principal) throws EntityNotFoundException {
        var m = service.getPremiumCourseAvailable(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    // All below APIs are accessible publicly and privately.
    @PostMapping(APIUrls.GET_ALL_COURSES_BY_CATEGORY)
    public ResponseEntity<Message<CourseByCategoryPaginatedResponse>> getCourseByCategory(@RequestBody CourseByCategoryRequest request, Principal principal)
            throws EntityNotFoundException {
        var m = service.getCoursesByCategoryWithPagination(request, Objects.isNull(principal) ? null : principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_ALL_COURSES_BY_INSTRUCTOR_FOR_PROFILE)
    public ResponseEntity<Message<CourseByCategoryPaginatedResponse>> getCourseByCategory(
            @RequestParam(required = false) Long instructorId,
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            Principal principal)
            throws EntityNotFoundException {
        var m = service.getCoursesByInstructorForProfile(instructorId, pageNo, pageSize, Objects.nonNull(principal) ? principal.getName() : null);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_COURSES_BY_TEACHER)
    public ResponseEntity<Message<TeacherCoursesResponse>> getCoursesByTeacher(
            @RequestParam(required = false) String searchInput,
            @RequestParam(required = false) Integer sort,
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException {
        var m = service.findCoursesByTeacher(searchInput, sort, pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_COURSE_DETAILS)
    public ResponseEntity<Message<CourseDetailResponse>> getCourseDetailById(@PathVariable Long courseId, Principal principal)
            throws EntityNotFoundException, InternalServerException, BadRequestException {
        var m = service.getCourseDetailsById(courseId, Objects.isNull(principal) ? null : principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.GET_COURSE_FEEDBACK)
    public ResponseEntity<Message<CourseFeedbackResponse>> getAllComments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) throws EntityNotFoundException {
        Message<CourseFeedbackResponse> feedbackResponse = crService.findStudentFeedbackByCourseId(courseId, pageNo, pageSize);
        return ResponseEntity.status(feedbackResponse.getStatus()).body(feedbackResponse);
    }


    @PostMapping(APIUrls.GET_RELATED_COURSES)
    public ResponseEntity<Message<RelatedCoursesResponse>> getRelatedCoursesByPagination(@RequestBody RelatedCoursesRequest request, Principal principal)
            throws EntityNotFoundException, InternalServerException {
        var m = service.getRelatedCoursesByPagination(request, Objects.nonNull(principal) ? principal.getName() : null);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    // @PostMapping(APIUrls.SEARCH_BY_FILTER)
    public ResponseEntity<Message<CourseBySearchFilterResponse>> searchByFilter(@RequestBody SearchCourseRequest request, Principal principal)
            throws EntityNotFoundException {
        var m = service.searchCourse(request, Objects.nonNull(principal) ? principal.getName() : null);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.SEARCH_AUTOCOMPLETE)
    public ResponseEntity<Message<CourseAutoComplete>> autocompleteCourseSearch(
            @RequestBody CourseSearchInput courseSearchInput) throws EntityNotFoundException {
        var m = esService.autocompleteForCourseSearch(courseSearchInput.getInput());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.COURSE_SHARED)
    public ResponseEntity<Object> sendCourseSharedNotification(@RequestParam Long courseId, Principal principal) throws EntityNotFoundException {
        service.sendCourseSharedNotification(courseId, principal.getName());
        return ResponseEntity.ok(null);
    }

    @GetMapping(APIUrls.GET_COURSE_DETAILS_FOR_UPDATE_FIRST_STEP)
    public ResponseEntity<Message<CourseDetailForUpdateResponse>> fetchCourseDetailForUpdateFirstStep(@PathVariable Long courseId, Principal principal)
            throws EntityNotFoundException, BadRequestException {
        var m = service.fetchCourseDetailForUpdateForFirstStep(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.UNIQUE_COURSE_TITLE)
    public ResponseEntity<Message<String>> checkUniqueCourseTile(@RequestBody UniqueCourseTitle uniqueCourseTitle, Principal principal) throws BadRequestException, EntityNotFoundException {
        var m = this.service.checkUniqueCourseTile(uniqueCourseTitle.getCourseTitle(), uniqueCourseTitle.getCourseId(), principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.COURSE_URL)
    public ResponseEntity<?> getCourseByUrl(@RequestBody UniqueCourseTitle request, Principal principal) throws BadRequestException, EntityNotFoundException {
        var m = this.service.getCourseByUrl(request.getCourseUrl(), principal);
        if(m.getData().getActiveUrl().equalsIgnoreCase(request.getCourseUrl())){
            return ResponseEntity.status(m.getStatus()).body(m);
        }
        RedirectView redirectView = new RedirectView(m.getData().getActiveUrl());
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ResponseEntity<>(redirectView, HttpStatus.MOVED_PERMANENTLY);
    }

//@PostMapping(APIUrls.COURSE_URL)
//public ResponseEntity<?> getCourseByUrl(@RequestBody UniqueCourseTitle request, Principal principal,
//                                        @RequestHeader(value = "User-Agent", required = false) String userAgent) throws BadRequestException, EntityNotFoundException {
//    var m = this.service.getCourseByUrl(request.getCourseUrl(), principal);
//    if(m.getData().getActiveUrl().equalsIgnoreCase(request.getCourseUrl())){
//        return ResponseEntity.status(m.getStatus()).body(m);
//    }
//    List<String> bots = Arrays.asList(
//            "Googlebot", "Bingbot", "Slurp", "Baiduspider",
//            "YandexBot", "DuckDuckBot", "facebookexternalhit", "Twitterbot"
//    );
//    // If it's a search engine bot, return a proper 301 redirect
//    if (userAgent != null && bots.stream().anyMatch(userAgent::contains)) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setLocation(URI.create(m.getData().getActiveUrl())); // New URL
//        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
//    }
//    RedirectView redirectView = new RedirectView(m.getData().getActiveUrl());
//    redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
//    return new ResponseEntity<>(redirectView, HttpStatus.MOVED_PERMANENTLY);
//}

    @GetMapping(APIUrls.COURSE_TITLE_DROPDOWN_FOR_PERFORMANCE_PAGE)
    public ResponseEntity<Message<List<CourseDropdown>>> fetchAllCoursesTitleByInstructorForPerformancePage(Principal principal) throws EntityNotFoundException {
        var m = this.service.fetchAllCoursesTitleByInstructorForPerformance(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.CHANGE_COURSE_STATUS)
    public ResponseEntity<Message<String>> changeCourseStatus(@RequestParam Long courseId, @RequestParam String courseStatus, Principal principal) throws EntityNotFoundException, InternalServerException, BadRequestException {
        var m = this.service.changeCourseStatus(courseId, courseStatus, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping("/related")
    public ResponseEntity<List<Course>> getRelatedCourses(@RequestParam String title,
                                                          @RequestParam List<Long> tagIds,
                                                          @RequestParam CourseStatus courseStatus) {
        List<Course> relatedCourses = service.getRelatedCourses(title, tagIds, courseStatus);
        return ResponseEntity.ok(relatedCourses);
    }

    @GetMapping(APIUrls.UNIQUE_COURSE_URL)
    public ResponseEntity<Message<String>> checkUniqueCourseUrl(@RequestParam String url, @RequestParam Long courseId, Principal principal) throws BadRequestException, EntityNotFoundException {
        var m = this.service.checkUniqueCourseUrl(url, courseId, principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.ES_COURSE_SEARCH)
    public ResponseEntity<Message<Page<CourseContentResponse>>> searchCourses
            (@RequestParam String query, @RequestParam(required = false) int pageNo, @RequestParam(required = false)
            int pageSize, Principal principal) throws BadRequestException, EntityNotFoundException, InternalServerException {
        return ResponseEntity.ok(this.service.searchCourses(query, pageNo, pageSize, Objects.nonNull(principal) ? principal.getName() : null));
    }

    @GetMapping(APIUrls.COMPLETED_COURSE)
    public ResponseEntity<Message<CompletedCourseByPaginated>> getCompletedCourse(
    @RequestParam(required = false) int pageNo, @RequestParam(required = false)
    int pageSize, Principal principal) throws EntityNotFoundException {
        return ResponseEntity.ok(this.service.getCompletedCourse(pageNo,pageSize,principal.getName()));
    }

    @PutMapping(APIUrls.DUMB_DB_COURSE_TO_ES)
    public ResponseEntity<Message<String>> dumbDbCourseToEs() throws InternalServerException {
        return ResponseEntity.ok(this.service.dumbDbCourseToEs());
    }

    @GetMapping(APIUrls.PREMIUM_COURSES)
    public ResponseEntity<Message<Page<CourseDetailByType>>> getPremiumCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) throws EntityNotFoundException {
        var m = this.service.getPremiumCourses(search, principal.getName(), PageRequest.of(pageNo, pageSize));
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
