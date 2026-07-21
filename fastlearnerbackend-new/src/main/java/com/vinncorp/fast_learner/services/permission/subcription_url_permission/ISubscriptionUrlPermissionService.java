package com.vinncorp.fast_learner.services.permission.subcription_url_permission;

import com.vinncorp.fast_learner.models.permission.SubscriptionUrlPermission;
import com.vinncorp.fast_learner.util.enums.GenericStatus;

public interface ISubscriptionUrlPermissionService {
    SubscriptionUrlPermission findBySubscriptionAndUrlPermissionAndStatus(Long subscriptionId, Long urlPermissionId, GenericStatus status);
}
