package com.vinncorp.fast_learner.services.permission.url_permission;

import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.repositories.permission.UrlPermissionRepository;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.stereotype.Service;

@Service
public class UrlPermissionService implements IUrlPermissionService{
    private final UrlPermissionRepository repo;

    public UrlPermissionService(UrlPermissionRepository repo) {
        this.repo = repo;
    }

    public UrlPermission findByMethodAndStatusAndStartsWithUrl(String method, GenericStatus status, String url){
        return this.repo.findByMethodAndStatusAndStartsWithUrl(method, status, url).orElse(null);
    }

}
