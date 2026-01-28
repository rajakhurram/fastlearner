package com.vinncorp.fast_learner.services.role;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.role.Role;

public interface IRoleService {

    Role getByType(String roleType) throws EntityNotFoundException;
}
