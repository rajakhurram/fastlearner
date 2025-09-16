package com.vinncorp.fast_learner.mock.user;

import com.vinncorp.fast_learner.models.role.Role;

public class RoleTestData {

    public static Role roleData(String role){
        return Role.builder()
                .id(1L)
                .type(role)
                .build();
    }

}
