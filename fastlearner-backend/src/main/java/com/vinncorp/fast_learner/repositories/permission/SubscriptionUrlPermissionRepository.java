package com.vinncorp.fast_learner.repositories.permission;

import com.vinncorp.fast_learner.models.permission.SubscriptionUrlPermission;
import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionUrlPermissionRepository extends JpaRepository<SubscriptionUrlPermission, Long> {

    @Query(value = "SELECT sup FROM SubscriptionUrlPermission sup " +
            "WHERE sup.subscription.id = :subscriptionId AND sup.urlPermission.id = :urlPermissionId AND sup.status = :status")
    Optional<SubscriptionUrlPermission> findBySubscriptionAndUrlPermissionAndStatus(@Param("subscriptionId") Long subscriptionId,
                                                                                    @Param("urlPermissionId") Long urlPermissionId,
                                                                                    @Param("status") GenericStatus status);
}
