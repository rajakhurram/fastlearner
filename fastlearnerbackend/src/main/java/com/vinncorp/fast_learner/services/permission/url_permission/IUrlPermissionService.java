package com.vinncorp.fast_learner.services.permission.url_permission;

import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.stereotype.Service;

@Service
public interface IUrlPermissionService {
    UrlPermission findByMethodAndStatusAndStartsWithUrl(String method, GenericStatus status, String url);
}
