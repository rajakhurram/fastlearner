package com.vinncorp.fast_learner.mock.course;

import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.es_dto.CourseAutoComplete;
import com.vinncorp.fast_learner.es_dto.SearchCourses;
import com.vinncorp.fast_learner.es_models.Course;
import com.vinncorp.fast_learner.es_repository.ESCourseRepository;
import com.vinncorp.fast_learner.es_services.ESCourseService;
import com.vinncorp.fast_learner.es_services.course_content.IESCourseContentService;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ESCourseServiceTest {

    @Mock
    private CourseService courseService;

    @Mock
    private ESCourseRepository repo;
    @Mock
    private IUserProfileService userProfileService;
    @Mock
    private IESCourseContentService esCourseContentService;

    @InjectMocks
    private ESCourseService esCourseService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("ESCourse saving when provided valid data")
    public void testCourseSavingInElasticsearch_Success() throws InternalServerException {
        Course course = new Course();
        course.setId("123");
        course.setDbId(1L);
        course.setTitle("Test Course");
        course.setDocVector("Sample vector");

        when(repo.save(any(Course.class))).thenReturn(course);

        Message<String> result = esCourseService.save(course);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Successfully saved data.", result.getMessage());

        verify(repo, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("ESCourse saving when database error occurred")
    public void testCourseSavingInElasticsearch_InternalServerException() {
        Course course = new Course();
        when(repo.save(any(Course.class))).thenThrow(new RuntimeException("Database error"));

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            esCourseService.save(course);
        });

        assertEquals("ES Course cannot be saved due to database error.", thrown.getMessage());

        verify(repo, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("Find by course id of db when provided valid id")
    public void testFindByDBId_Success() throws EntityNotFoundException {
        Long dbId = 1L;
        Course course = new Course();
        course.setDbId(dbId);
        course.setTitle("Test Course");

        when(repo.findByDbId(dbId)).thenReturn(Optional.of(course));

        Course result = esCourseService.findByDBId(dbId);

        assertNotNull(result);
        assertEquals(dbId, result.getDbId());
        assertEquals("Test Course", result.getTitle());

        verify(repo, times(1)).findByDbId(dbId);
    }

    @Test
    @DisplayName("Find by course id of db when provided invalid id")
    public void testFindByDBId_EntityNotFoundException() {
        Long dbId = 2L;
        when(repo.findByDbId(dbId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            esCourseService.findByDBId(dbId);
        });

        assertEquals("No course present in elastic search db.", thrown.getMessage());

        verify(repo, times(1)).findByDbId(dbId);
    }

    @Test
    @DisplayName("Auto complete for course search when input is Capitalized")
    public void testAutocompleteForCourseSearch_InputLowercase() throws EntityNotFoundException {
        String input = "JAVA";
        SearchCourses course1 = new SearchCourses("1", 1L, "Java Basics", "java-basics", "");
        when(esCourseContentService.getCourseTitles(input.toLowerCase(), 0, 5)).thenReturn(Arrays.asList(course1));
        InstructorProfileSearchDto profile1 = new InstructorProfileSearchDto();
        profile1.setId(1L);
        profile1.setProfilePicture("");
        profile1.setFullName("test");
        profile1.setProfileUrl("instructor-1");
        when(userProfileService.getSearchInstructorProfiles(input.toLowerCase(), 0, 5)).thenReturn(Arrays.asList(profile1));
        Message<CourseAutoComplete> result = esCourseService.autocompleteForCourseSearch(input);
        assertNotNull(result.getData());
        assertEquals(HttpStatus.OK.value(), result.getStatus());
    }

    @Test
    @DisplayName("Autocomplete for course search when duplicate course titles are found")
    public void testAutocompleteForCourseSearch_RemoveDuplicateTitles() throws EntityNotFoundException {
        String input = "java";
        SearchCourses course1 = new SearchCourses("1", 1L, "Java Basics", "java-basics", "");
        when(esCourseContentService.getCourseTitles(input.toLowerCase(), 0, 5)).thenReturn(Arrays.asList(course1));
        InstructorProfileSearchDto profile1 = new InstructorProfileSearchDto();
        profile1.setId(1L);
        profile1.setProfilePicture("");
        profile1.setFullName("test");
        profile1.setProfileUrl("instructor-1");
        when(userProfileService.getSearchInstructorProfiles(input.toLowerCase(), 0, 5)).thenReturn(Arrays.asList(profile1));
        Message<CourseAutoComplete> result = esCourseService.autocompleteForCourseSearch(input);
        assertNotNull(result.getData());
        assertEquals(HttpStatus.OK.value(), result.getStatus());
    }

}
