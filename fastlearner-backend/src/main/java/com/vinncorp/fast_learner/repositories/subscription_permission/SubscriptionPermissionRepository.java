package com.vinncorp.fast_learner.repositories.subscription_permission;

import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPermissionRepository extends JpaRepository<SubscriptionPermission,Long> {
    List<SubscriptionPermission> findBySubscription_IdAndIsActive(Long subscriptionId, Boolean isActive);


}

