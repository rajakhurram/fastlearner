package com.vinncorp.fast_learner.services.subscription_permission;

import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;
import com.vinncorp.fast_learner.repositories.subscription_permission.SubscriptionPermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SubscriptionPermissionService implements ISubscriptionPermissionService {

    public SubscriptionPermissionService(SubscriptionPermissionRepository subscriptionPermissionRepository) {
        this.subscriptionPermissionRepository = subscriptionPermissionRepository;
    }
    private SubscriptionPermissionRepository subscriptionPermissionRepository;
    @Override
    public List<SubscriptionPermission> findBySubscriptionAndIsActive(Long subscriptionId) {
        return subscriptionPermissionRepository.findBySubscription_IdAndIsActive(subscriptionId,true);
    }
}
