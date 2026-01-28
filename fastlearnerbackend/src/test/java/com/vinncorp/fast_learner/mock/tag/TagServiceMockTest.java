package com.vinncorp.fast_learner.mock.tag;

import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.repositories.tag.TagRepository;
import com.vinncorp.fast_learner.request.tag.CreateTagRequest;
import com.vinncorp.fast_learner.services.course.ICourseTagService;
import com.vinncorp.fast_learner.services.tag.TagService;
import com.vinncorp.fast_learner.util.Message;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class TagServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";
    private static Long COURSE_ID = 1L;
    @Mock
    private TagRepository repo;
    @Mock
    private ICourseTagService courseTagService;
    @InjectMocks
    private TagService tagService;
    private Course course;
    private List<CreateTagRequest> tagRequests;
    private List<Tag> tags;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        course = new Course();
        course.setId(1L);
        tagRequests = List.of(
                CreateTagRequest.builder().id(1L).name("Tag 1").active(true).build(),
                CreateTagRequest.builder().id(null).name("Tag 2").active(true).build(),
                CreateTagRequest.builder().id(2L).name("Tag 3").active(true).build()
        );
        tags = List.of(
                Tag.builder().id(1L).name("Tag 1").isActive(true).build(),
                Tag.builder().id(2l).name("Tag 3").isActive(true).build()
        );
        when(repo.saveAll(anyList())).thenReturn(tags);

    }

    @Test
    @DisplayName("Should fetch tags by course ID and return a successful message")
    void shouldFetchTagsByCourseIdAndReturnSuccessfulMessage() throws EntityNotFoundException {

        when(repo.findByCourseId(COURSE_ID)).thenReturn(tags);

        Message<List<Tag>> result = tagService.fetchTagsByCourse(COURSE_ID, EMAIL);

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Successfully fetched all tags.", result.getMessage());
        assertEquals(tags, result.getData());
        verify(repo, times(1)).findByCourseId(COURSE_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no tags are found by course ID")
    void shouldThrowEntityNotFoundExceptionWhenNoTagsFoundByCourseId() {
        when(repo.findByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            tagService.fetchTagsByCourse(COURSE_ID, EMAIL);
        });

        assertEquals("No tags are found for this course.", exception.getMessage());
        verify(repo, times(1)).findByCourseId(COURSE_ID);
    }

    @Test
    @DisplayName("Should handle exception when repository throws exception")
    void shouldHandleExceptionOnTagsByCourseWhenRepositoryThrowsException() {
            when(repo.findByCourseId(COURSE_ID)).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            tagService.fetchTagsByCourse(COURSE_ID, EMAIL);
        });

        assertEquals("Database error", exception.getMessage());
        verify(repo, times(1)).findByCourseId(COURSE_ID);
    }

    @Test
    @DisplayName("Should return tags when tags are found by course ID")
    void shouldReturnTagsWhenFoundByCourseId() {

        when(repo.findByCourseId(COURSE_ID)).thenReturn(tags);

        List<Tag> result = tagService.findByCourseId(COURSE_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repo, times(1)).findByCourseId(COURSE_ID);
    }

    @Test
    @DisplayName("Should return empty list when no tags are found by course ID")
    void shouldReturnEmptyListWhenNoTagsFoundByCourseId() {

        when(repo.findByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        List<Tag> result = tagService.findByCourseId(COURSE_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repo, times(1)).findByCourseId(COURSE_ID);
    }


    @Test
    @DisplayName("Should handle exception when repository throws exception")
    void shouldHandleExceptionWhenRepositoryThrowsException() {
        when(repo.findByCourseId(COURSE_ID)).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            tagService.findByCourseId(COURSE_ID);
        });

        assertEquals("Database error", exception.getMessage());
        verify(repo, times(1)).findByCourseId(COURSE_ID);
    }

    @Test
    @DisplayName("Should successfully delete tags by IDs")
    public void testDeleteTagsByIdsSuccess() {

        List<Long> tagIds = Arrays.asList(1L, 2L, 3L);

        doNothing().when(repo).deleteAllById(tagIds);

        repo.deleteAllById(tagIds);

        verify(repo, times(1)).deleteAllById(tagIds);
    }

    @Test
    @DisplayName("Should handle empty list of IDs")
    public void testDeleteTagsByIdsEmptyList() {

        List<Long> tagIds = Collections.emptyList();

        doNothing().when(repo).deleteAllById(tagIds);

        repo.deleteAllById(tagIds);

        verify(repo, times(1)).deleteAllById(tagIds);
    }

    @Test
    @DisplayName("Should handle null list of IDs")
    public void testDeleteTagsByIdsNullList() {

        List<Long> tagIds = null;

        doNothing().when(repo).deleteAllById(tagIds);

        repo.deleteAllById(tagIds);

        verify(repo, times(1)).deleteAllById(tagIds);
    }

    @Test
    @DisplayName("Should fetch all tags when a valid name is provided")
    void shouldFetchAllTagsWhenValidNameProvided() throws EntityNotFoundException, BadRequestException {

        String tagName = "Java";
        when(repo.findByIsActiveAndNameLike(true, "%" + tagName + "%")).thenReturn(tags);

        Message<List<Tag>> response = tagService.fetchTageByName(tagName);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully fetched all tags.", response.getMessage());
        assertFalse(response.getData().isEmpty());
        assertEquals(2, response.getData().size());
        verify(repo, times(1)).findByIsActiveAndNameLike(true, "%" + tagName + "%");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no tags are found")
    void shouldThrowEntityNotFoundExceptionWhenNoTagsFound() {
        String tagName = "NonExistentTag";
        when(repo.findByIsActiveAndNameLike(true, "%" + tagName + "%")).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            tagService.fetchTageByName(tagName);
        });

        assertEquals("No tags found.", thrown.getMessage());
        verify(repo, times(1)).findByIsActiveAndNameLike(true, "%" + tagName + "%");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no active tags match the provided name")
    void shouldThrowEntityNotFoundExceptionWhenNoActiveTagsMatchProvidedName() {
        String tagName = "InactiveTag";
        when(repo.findByIsActiveAndNameLike(true, "%" + tagName + "%")).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            tagService.fetchTageByName(tagName);
        });

        assertEquals("No tags found.", thrown.getMessage());
        verify(repo, times(1)).findByIsActiveAndNameLike(true, "%" + tagName + "%");
    }

    @Test
    @DisplayName("Should handle null name and return empty list if no tags are found")
    void shouldHandleNullNameAndReturnEmptyListIfNoTagsFound() {

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            tagService.fetchTageByName(null);
        });

        assertEquals("name cannot be null.", thrown.getMessage());
    }

    @DisplayName("Create all new and already exist tags success")
    @Test
    public void testCreateAllNewAndAlreadyExistsTags_Success() throws BadRequestException, InternalServerException {
        when(repo.findById(1L)).thenReturn(Optional.of(tags.get(0)));
        when(repo.findById(2L)).thenReturn(Optional.of(tags.get(1)));

        tagService.createAllNewAndAlreadyExistsTags(tagRequests, course);

        verify(repo, times(1)).saveAll(anyList());
        verify(courseTagService, times(1)).deleteAllCourseTagByTagIds(anyLong());
        verify(courseTagService, times(1)).createAllCourseTags(anyList());
    }

    @DisplayName("Create all new and already exist tags when no tags provided")
    @Test
    public void testCreateAllNewAndAlreadyExistsTags_NoTagsProvided() throws BadRequestException, InternalServerException {
        tagService.createAllNewAndAlreadyExistsTags(Collections.emptyList(), course);

        verify(courseTagService, never()).deleteAllCourseTagByTagIds(anyLong());
        verify(repo, never()).saveAll(anyList());
        verify(courseTagService, never()).createAllCourseTags(anyList());
    }

}
