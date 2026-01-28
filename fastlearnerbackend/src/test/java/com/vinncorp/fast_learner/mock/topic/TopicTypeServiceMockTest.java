package com.vinncorp.fast_learner.mock.topic;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.topic.TopicType;
import com.vinncorp.fast_learner.repositories.topic.TopicTypeRepository;
import com.vinncorp.fast_learner.services.topic.TopicTypeService;
import com.vinncorp.fast_learner.util.Message;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
public class TopicTypeServiceMockTest {

    private static final String EMAIL = "qasim@mailinator.com";
    private static Long TOPIC_TYPE_D = 1L;
    @InjectMocks
    private TopicTypeService topicTypeService;
    @Mock
    private TopicTypeRepository repo;
    List<TopicType> topicTypes;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        topicTypes = List.of(
                TopicType.builder().id(1L).name("Type1").isActive(true).build(),
                TopicType.builder().id(2L).name("Type2").isActive(true).build()
        );
    }

    @Test
    @DisplayName("Should fetch topic type by ID successfully")
    void testFindByIdSuccess() throws EntityNotFoundException {
        TopicType topicType = TopicType.builder().id(TOPIC_TYPE_D).name("Type1").isActive(true).build();
        when(repo.findById(TOPIC_TYPE_D)).thenReturn(Optional.of(topicType));
        TopicType result = topicTypeService.findById(TOPIC_TYPE_D);
        assertNotNull(result);
        assertEquals(TOPIC_TYPE_D, result.getId());
        assertEquals("Type1", result.getName());
        verify(repo, times(1)).findById(TOPIC_TYPE_D);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when topic type not found by ID")
    void testFindByIdNotFound() {
        when(repo.findById(TOPIC_TYPE_D)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicTypeService.findById(TOPIC_TYPE_D)
        );
        assertEquals("Topic type is not found.", exception.getMessage());
        verify(repo, times(1)).findById(TOPIC_TYPE_D);
    }

    @Test
    @DisplayName("Should log error when topic type not found by ID")
    void testFindByIdLogsErrorWhenNotFound() {
        when(repo.findById(TOPIC_TYPE_D)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicTypeService.findById(TOPIC_TYPE_D)
        );
        assertEquals("Topic type is not found.", exception.getMessage());
        verify(repo, times(1)).findById(TOPIC_TYPE_D);
    }

    @Test
    @DisplayName("Should fetch all active topic types successfully")
    void testFetchAllTopicTypeSuccess() throws EntityNotFoundException {
        when(repo.findByIsActive(true)).thenReturn(topicTypes);
        Message<List<TopicType>> result = topicTypeService.fetchAllTopicType(EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("All topic type is fetched successfully.", result.getMessage());
        assertEquals(2, result.getData().size());
        verify(repo, times(1)).findByIsActive(true);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no active topic types are found")
    void testFetchAllTopicTypeWhenNoTypesFound() {
        when(repo.findByIsActive(true)).thenReturn(List.of());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicTypeService.fetchAllTopicType(EMAIL)
        );
        assertEquals("No topic type found.", exception.getMessage());
        verify(repo, times(1)).findByIsActive(true);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when repository returns null")
    void testFetchAllTopicTypeWhenRepositoryReturnsNull() {
        when(repo.findByIsActive(true)).thenReturn(null);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicTypeService.fetchAllTopicType(EMAIL)
        );
        assertEquals("No topic type found.", exception.getMessage());
        verify(repo, times(1)).findByIsActive(true);
    }

}
