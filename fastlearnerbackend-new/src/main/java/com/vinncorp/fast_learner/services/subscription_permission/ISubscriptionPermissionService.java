package com.vinncorp.fast_learner.services.subscription_permission;

import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;

import java.util.List;

public interface ISubscriptionPermissionService {
   List<SubscriptionPermission> findBySubscriptionAndIsActive(Long subscriptionId);
}
