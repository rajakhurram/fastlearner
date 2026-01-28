package com.vinncorp.fast_learner.services.permission.subcription_url_permission;

import com.vinncorp.fast_learner.models.permission.SubscriptionUrlPermission;
import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.repositories.permission.SubscriptionUrlPermissionRepository;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionUrlPermissionService implements ISubscriptionUrlPermissionService{
    private final SubscriptionUrlPermissionRepository repo;

    public SubscriptionUrlPermissionService(SubscriptionUrlPermissionRepository repo) {
        this.repo = repo;
    }

    public SubscriptionUrlPermission findBySubscriptionAndUrlPermissionAndStatus(Long subscriptionId, Long urlPermissionId, GenericStatus status){
      return this.repo.findBySubscriptionAndUrlPermissionAndStatus(subscriptionId, urlPermissionId, status).orElse(null);
    }

}
