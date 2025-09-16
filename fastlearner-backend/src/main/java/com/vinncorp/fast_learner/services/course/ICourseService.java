package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.dtos.course.CourseDetailForCertificate;
import com.vinncorp.fast_learner.dtos.course.CourseDropdown;
import com.vinncorp.fast_learner.es_dto.CourseContentResponse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.CreateCourseValidationException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.course.CourseUrlDto;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.request.course.SearchCourseRequest;
import com.vinncorp.fast_learner.request.course.CourseByCategoryRequest;
import com.vinncorp.fast_learner.request.course.RelatedCoursesRequest;
import com.vinncorp.fast_learner.response.course.*;
import com.vinncorp.fast_learner.response.subscriptionpermission.SubscriptionPermissionResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

public interface ICourseService {
    Message<CreateCourseRequest> createCourse(CreateCourseRequest request,String clientId, String email) throws EntityNotFoundException, InternalServerException, BadRequestException, IOException, CreateCourseValidationException;

    Message<CourseByCategoryPaginatedResponse> getCoursesByCategoryWithPagination(CourseByCategoryRequest request, String email) throws EntityNotFoundException;

    Message<CourseByCategoryPaginatedResponse> getCoursesByInstructorForProfile(
            Long instructorId, int pageNo, int pageSize, String email) throws EntityNotFoundException;

    Course findById(Long courseId) throws EntityNotFoundException;

    Message<CourseDetailResponse> getCourseDetailsById(Long courseId, String email) throws EntityNotFoundException, InternalServerException, BadRequestException;

    Message<RelatedCoursesResponse> getRelatedCoursesByPagination(RelatedCoursesRequest request, String email) throws EntityNotFoundException, InternalServerException;

    Message<CourseBySearchFilterResponse> searchCourse(SearchCourseRequest request, String email) throws EntityNotFoundException;

    Message<TeacherCoursesResponse> findCoursesByTeacher(String searchInput, Integer sort, int pageNo, int pageSize, String name) throws EntityNotFoundException, BadRequestException;

    void sendCourseSharedNotification(Long courseId, String email) throws EntityNotFoundException;

    Message<CourseDetailForUpdateResponse> fetchCourseDetailForUpdateForFirstStep(Long courseId, String email) throws EntityNotFoundException, BadRequestException;

    boolean isCourseOwnedByUser(String email);

    CourseDetailForCertificate getCourseDetailForCertificate(Long courseId) throws EntityNotFoundException;

    boolean isExistCertificateEnabledByCourseId(Long courseId);

    Course findByDocVector(String docVector);

    Message<String> checkUniqueCourseTile(String courseTitle, Long courseId, Principal principal) throws BadRequestException, EntityNotFoundException;

    Message<CourseUrlDto> getCourseByUrl(String courseUrl, Principal principal) throws EntityNotFoundException;

    Message<List<CourseDropdown>> fetchAllCoursesTitleByInstructorForPerformance(String email) throws EntityNotFoundException;
    Message<String> changeCourseStatus(Long courseId, String courseStatus, String email) throws EntityNotFoundException, InternalServerException, BadRequestException;

    List<Course> getRelatedCourses(String title, List<Long> tagIds, CourseStatus courseStatus);
    Message<String> checkUniqueCourseUrl(String url, Long courseId, Principal principal) throws BadRequestException, EntityNotFoundException;

    Message<Page<CourseContentResponse>> searchCourses(String query, int pageNo, int pageSize, String email) throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<String> dumbDbCourseToEs() throws InternalServerException;
    List<Course> findAllByStatuses(List<CourseStatus> courseStatuses);
    List<CourseDetailByType> getCourseDetailByInstructorIdAndType(Long instructorId, CourseType courseType) throws BadRequestException;
    Message<Page<CourseDetailByType>> getPremiumCourses(String search, String name, Pageable pageable) throws EntityNotFoundException;

    Message<CourseDetailByPaginatedResponse> findCoursesByFilter(List<String> multipleCategories, String singleCat, String courseType,String search , String feature, double minRating, double maxRating, Long userId, String contentType,
                                Pageable pageable);

    Message<CourseDetailByPaginatedResponse> getAllNewCourse(Pageable pageable, Principal principal);

    Message<CourseDetailByPaginatedResponse> getAllFreeCourse(Pageable pageable);

    Message<CourseDetailByPaginatedResponse> getAllTrendingCourses(Pageable pageable, Principal principal);

    Message<CourseDetailByPaginatedResponse> getAllPremiumCourses(Pageable pageable, Principal principal);

    Message<CompletedCourseByPaginated> getCompletedCourse(int pageNo, int pageSize, String name) throws EntityNotFoundException;

    List<Course> findByInstructorIdAndCourseStatus(User instructor, CourseStatus courseStatus);

    List<Course> saveAll(List<Course> course);
    Message<SubscriptionPermissionResponse> getPremiumCourseAvailable(String email) throws EntityNotFoundException;
}
