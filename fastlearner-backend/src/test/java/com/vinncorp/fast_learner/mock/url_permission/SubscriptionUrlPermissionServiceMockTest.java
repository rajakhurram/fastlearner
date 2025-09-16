package com.vinncorp.fast_learner.mock.url_permission;

import com.vinncorp.fast_learner.models.permission.SubscriptionUrlPermission;
import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.repositories.permission.SubscriptionUrlPermissionRepository;
import com.vinncorp.fast_learner.services.permission.subcription_url_permission.SubscriptionUrlPermissionService;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@Slf4j
public class SubscriptionUrlPermissionServiceMockTest {

    @Mock
    private SubscriptionUrlPermissionRepository repo;
    @InjectMocks
    private SubscriptionUrlPermissionService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Positive Test: Find SubscriptionUrlPermission with valid data")
    void testFindBySubscriptionAndUrlPermissionAndStatus_Positive() {
        Long subscriptionId = 1L;
        Long urlPermissionId = 2L;
        GenericStatus status = GenericStatus.ACTIVE;
        SubscriptionUrlPermission mockPermission = SubscriptionUrlPermission.builder()
                .id(3L)
                .subscription(Subscription.builder().id(2L).build())
                .urlPermission(UrlPermission.builder().id(1L).build())
                .status(status)
                .build();
        when(repo.findBySubscriptionAndUrlPermissionAndStatus(subscriptionId, urlPermissionId, status))
                .thenReturn(Optional.of(mockPermission));
        SubscriptionUrlPermission result = service.findBySubscriptionAndUrlPermissionAndStatus(subscriptionId, urlPermissionId, status);
        assertNotNull(result);
        verify(repo, times(1)).findBySubscriptionAndUrlPermissionAndStatus(subscriptionId, urlPermissionId, status);
    }

    @Test
    @DisplayName("Negative Test: Find SubscriptionUrlPermission with no matching data")
    void testFindBySubscriptionAndUrlPermissionAndStatus_Negative() {
        Long subscriptionId = 1L;
        Long urlPermissionId = 2L;
        GenericStatus status = GenericStatus.INACTIVE;
        when(repo.findBySubscriptionAndUrlPermissionAndStatus(subscriptionId, urlPermissionId, status))
                .thenReturn(Optional.empty());
        SubscriptionUrlPermission result = service.findBySubscriptionAndUrlPermissionAndStatus(subscriptionId, urlPermissionId, status);
        assertNull(result, "Result should be null for no matching data");
        verify(repo, times(1)).findBySubscriptionAndUrlPermissionAndStatus(subscriptionId, urlPermissionId, status);
    }

}
