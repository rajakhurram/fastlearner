package com.vinncorp.fast_learner.mock.subscription_permission;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;
import com.vinncorp.fast_learner.repositories.subscription_permission.SubscriptionPermissionRepository;

import java.util.ArrayList;
import java.util.List;

import com.vinncorp.fast_learner.services.subscription_permission.SubscriptionPermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionPermissionServiceTest {
    @Mock
    private SubscriptionPermissionRepository subscriptionPermissionRepository;

    @InjectMocks
    private SubscriptionPermissionService subscriptionPermissionService;

    /**
     * Method under test: {@link SubscriptionPermissionService#findBySubscriptionAndIsActive(Long)}
     */
    @Test
    @DisplayName("Test find by subscription and is active")
    void testFindBySubscriptionAndIsActive() {
        ArrayList<SubscriptionPermission> subscriptionPermissionList = new ArrayList<>();
        when(subscriptionPermissionRepository.findBySubscription_IdAndIsActive(Mockito.<Long>any(), Mockito.<Boolean>any()))
                .thenReturn(subscriptionPermissionList);
        List<SubscriptionPermission> actualFindBySubscriptionAndIsActiveResult = subscriptionPermissionService
                .findBySubscriptionAndIsActive(1L);
        assertSame(subscriptionPermissionList, actualFindBySubscriptionAndIsActiveResult);
        assertTrue(actualFindBySubscriptionAndIsActiveResult.isEmpty());
        verify(subscriptionPermissionRepository).findBySubscription_IdAndIsActive(Mockito.<Long>any(),
                Mockito.<Boolean>any());
    }
}

