package com.vinncorp.fast_learner.repositories.permission;

import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UrlPermissionRepository extends JpaRepository<UrlPermission, Long> {

    @Query("SELECT u FROM UrlPermission u WHERE u.method = :method AND u.status = :status AND u.url = :url")
    Optional<UrlPermission> findByMethodAndStatusAndStartsWithUrl(@Param("method") String method,
                                                                  @Param("status") GenericStatus status,
                                                                  @Param("url") String url);

}
