package com.vinncorp.fast_learner.mock.static_pages;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.static_pages.StaticPages;
import com.vinncorp.fast_learner.repositories.static_pages.StaticPagesRepository;
import com.vinncorp.fast_learner.response.static_pages.StaticPagesResponse;
import com.vinncorp.fast_learner.services.static_pages.StaticPagesService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaticPagesServiceTest {
    @Mock
    private StaticPagesRepository staticPagesRepository;

    @InjectMocks
    private StaticPagesService staticPagesService;

    @Test
    @DisplayName("Test CreateStaticPage when provided valid data")
    void testCreateStaticPage_whenProvidedValidData() throws InternalServerException {
        when(staticPagesRepository.save(Mockito.<StaticPages>any())).thenReturn(new StaticPages());
        Message<String> actualCreateStaticPageResult = staticPagesService.createStaticPage(new StaticPages());
        assertEquals("OK", actualCreateStaticPageResult.getCode());
        assertEquals(200, actualCreateStaticPageResult.getStatus());
        assertEquals("Static Page Created Successfully.", actualCreateStaticPageResult.getMessage());
        verify(staticPagesRepository).save(Mockito.<StaticPages>any());
    }

    @Test
    @DisplayName("Test CreateStaticPage when invalid data provided")
    void testCreateStaticPage_whenInvalidDataProvided() {
        when(staticPagesRepository.save(Mockito.<StaticPages>any()))
                .thenThrow(new RuntimeException("StaticPage " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR));

        Exception exception = assertThrows(InternalServerException.class, () -> {
            staticPagesService.createStaticPage(new StaticPages());
        });

        assertTrue(exception.getMessage().contains(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("Test GetStaticPageBySlugify when provided valid data")
    void testGetStaticPageBySlugify_whenProvidedValidData() throws EntityNotFoundException {
        when(staticPagesRepository.findBySlugify(Mockito.<String>any())).thenReturn(Optional.of(new StaticPages()));
        StaticPagesResponse actualStaticPageBySlugify = staticPagesService.getStaticPageBySlugify("Slugify");
        assertNull(actualStaticPageBySlugify.getContent());
        assertNull(actualStaticPageBySlugify.getType());
        assertNull(actualStaticPageBySlugify.getSlugify());
        assertNull(actualStaticPageBySlugify.getCreationDate());
        verify(staticPagesRepository).findBySlugify(Mockito.<String>any());
    }

    @Test
    @DisplayName("Test GetStaticPageBySlugify2 when provided valid data")
    void testGetStaticPageBySlugify2_whenProvidedValidData() throws EntityNotFoundException {
        when(staticPagesRepository.findBySlugify(Mockito.<String>any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> staticPagesService.getStaticPageBySlugify("Slugify"));
        verify(staticPagesRepository).findBySlugify(Mockito.<String>any());
    }
}

