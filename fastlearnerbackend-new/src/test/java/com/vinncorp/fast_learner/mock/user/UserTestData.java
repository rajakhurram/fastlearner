package com.vinncorp.fast_learner.mock.user;

import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.enums.AuthProvider;

import java.util.Date;

public class UserTestData {

    public static User userData() {
        return User.builder()
                .id(1L)
                .fullName("Qasim Ali")
                .role(new Role(1L, "INSTRUCTOR"))
                .email("qasim@mailinator.com")
                .provider(AuthProvider.LOCAL)
                .password("no-password")
                .creationDate(new Date())
                .isSubscribed(true)
                .isActive(true)
                .build();
    }
}
