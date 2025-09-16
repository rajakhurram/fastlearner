package com.vinncorp.fast_learner.mock.url_permission;

import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.repositories.permission.UrlPermissionRepository;
import com.vinncorp.fast_learner.services.permission.url_permission.UrlPermissionService;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

@Slf4j
public class UrlPermissionServiceMockTest {

    @Mock
    private UrlPermissionRepository repo;

    @InjectMocks
    private UrlPermissionService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Positive Test: Find UrlPermission with valid data")
    void testFindByMethodAndStatusAndStartsWithUrl_Positive() {
        String method = "GET";
        GenericStatus status = GenericStatus.ACTIVE;
        String url = "/api/resource";
        UrlPermission mockPermission = UrlPermission.builder()
                .id(1L)
                .url("/api/resource")
                .method(method)
                .status(status)
                .build();
        when(repo.findByMethodAndStatusAndStartsWithUrl(method, status, url))
                .thenReturn(Optional.of(mockPermission));
        UrlPermission result = service.findByMethodAndStatusAndStartsWithUrl(method, status, url);
        assertNotNull(result);
        verify(repo, times(1)).findByMethodAndStatusAndStartsWithUrl(method, status, url);
    }

    @Test
    @DisplayName("Negative Test: Find UrlPermission with no matching data")
    void testFindByMethodAndStatusAndStartsWithUrl_Negative() {
        String method = "POST";
        GenericStatus status = GenericStatus.INACTIVE;
        String url = "/api/invalid";
        when(repo.findByMethodAndStatusAndStartsWithUrl(method, status, url))
                .thenReturn(Optional.empty());
        UrlPermission result = service.findByMethodAndStatusAndStartsWithUrl(method, status, url);
        assertNull(result);
        verify(repo, times(1)).findByMethodAndStatusAndStartsWithUrl(method, status, url);
    }

}
