package com.vinncorp.fast_learner.mock.user.user_profile;

import com.vinncorp.fast_learner.dtos.user.UserProfileDto;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.test_util.Constants;

public class UserProfileTestData {

    public static UserProfile userProfile() {
        return UserProfile.builder()
                .id(1L)
                .aboutMe("Test about me")
                .profilePicture("https://www.gcp-storage.com/image1.png")
                .headline("Test headline")
                .experience("4+ years")
                .qualification("BSSE")
                .specialization("Software Engineering")
                .showProfile(true)
                .showCourses(true)
                .build();
    }

    public static UserProfileDto userProfileDto() {
        return UserProfileDto.builder()
                .fullName("Qasim Ali")
                .email("qasim@mailinator.com")
                .aboutMe("Test about me")
                .profilePicture("https://www.gcp-storage.com/image1.png")
                .headline("Test headline")
                .experience("4+ years")
                .qualification("BSSE")
                .specialization("Software Engineering")
                .showProfile(true)
                .showCourses(true)
                .build();
    }
}
