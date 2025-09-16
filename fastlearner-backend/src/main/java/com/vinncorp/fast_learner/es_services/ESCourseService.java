package com.vinncorp.fast_learner.es_services;

import com.vinncorp.fast_learner.es_dto.CourseAutoComplete;
import com.vinncorp.fast_learner.es_dto.SearchRelatedCourses;
import com.vinncorp.fast_learner.es_repository.ESCourseRepository;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.es_models.Course;
import com.vinncorp.fast_learner.es_services.course_content.IESCourseContentService;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.course.SearchRelatedCoursesRequest;
import com.vinncorp.fast_learner.response.course.ListOfSearchRelatedCourses;
import com.vinncorp.fast_learner.response.course.SearchRelatedCoursesResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.util.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class ESCourseService implements IESCourseService{

    private final ESCourseRepository repo;
    private final ICourseService courseService;
    private final IUserProfileService userProfileService;
    private final IESCourseContentService esCourseContentService;
    private final RestTemplate restTemplate;

    public ESCourseService(ESCourseRepository repo, @Lazy ICourseService courseService, IUserProfileService userProfileService, IESCourseContentService esCourseContentService, RestTemplate restTemplate) {
        this.repo = repo;
        this.courseService = courseService;
        this.userProfileService = userProfileService;
        this.esCourseContentService = esCourseContentService;
        this.restTemplate = restTemplate;
    }

    @Override
    public Message<String> save(Course course) throws InternalServerException {
        log.info("Saving course into elastic search db.");
        try {
            Course c = repo.save(course);
            System.out.printf("Course: [%s, %s, %s, %s]", c.getId(), c.getDbId(), c.getTitle(), c.getDocVector());
        } catch (Exception e) {
            throw new InternalServerException("ES Course "+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully saved data.");
    }

    @Override
    public Course findByDBId(Long id) throws EntityNotFoundException{
        log.info("Fetching course from elastic search using database id.");
        return repo.findByDbId(id).orElseThrow(() -> new EntityNotFoundException("No course present in elastic search db."));
    }

    @Override
    public Message<CourseAutoComplete> autocompleteForCourseSearch(String input) throws EntityNotFoundException {
        log.info("Searching by input: "+ input);
        CourseAutoComplete courseAutoComplete = new CourseAutoComplete();
        courseAutoComplete.setSearchCourses(this.esCourseContentService.getCourseTitles(input, 0, 5));
        List<InstructorProfileSearchDto> instructorProfileSearch = this.userProfileService.getSearchInstructorProfiles(input, 0, 5);
        instructorProfileSearch.forEach(profile -> profile.setProfileUrl("user/profile?url="+profile.getProfileUrl()));

        courseAutoComplete.setInstructorProfiles(instructorProfileSearch);

        return new Message<CourseAutoComplete>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(courseAutoComplete)
                .setMessage("Fetched the search results.");
    }
}
