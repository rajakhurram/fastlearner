package com.vinncorp.fast_learner.integration.user;

import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.auth.LocalRegisterRequest;
import com.vinncorp.fast_learner.test_util.Constants;
import com.vinncorp.fast_learner.util.enums.AuthProvider;

import java.util.Date;
import java.util.Random;

public class UserIntegrationTestData {

    public static User userData() {
        return User.builder()
                .id(1L)
                .fullName("Qasim Ali")
                .role(new Role(1L, "INSTRUCTOR"))
                .email(Constants.EMAIL)
                .provider(AuthProvider.LOCAL)
                .password(Constants.PASSWORD)
                .creationDate(new Date())
                .isSubscribed(true)
                .isActive(true)
                .build();
    }

    public static LocalRegisterRequest localRegisterRequest(){
        return LocalRegisterRequest.builder()
                .email(getRandomEmail())
                .name(userData().getFullName())
                .password(userData().getPassword())
                .subscribeNewsletter(true)
                .build();
    }

    public static String getRandomEmail(){
        int min = 1000;
        int max = 9999999;
        Random random = new Random();
        int num = random.nextInt(max - min + 1) + min;
        return "user"+String.valueOf(num)+"@mailinator.com";
    }

}
