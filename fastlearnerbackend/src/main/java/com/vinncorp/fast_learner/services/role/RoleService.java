package com.vinncorp.fast_learner.services.role;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.repositories.role.IRoleRepository;
import com.vinncorp.fast_learner.models.role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService{

    private final IRoleRepository repo;

    @Override
    public Role getByType(String roleType) throws EntityNotFoundException {
        log.info("Fetching role by type: " + roleType);
        return repo.findByType(roleType).orElseThrow(
                () -> new EntityNotFoundException("No role found by type: "+roleType));
    }
}