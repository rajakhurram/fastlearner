package com.vinncorp.fast_learner.mock.subscription.subscribed_user_profile;

import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;

public class SubscribedUserProfileTestData {

    public static SubscribedUserProfile subscribedUserProfile() {
        return SubscribedUserProfile.builder()
                .id(1L)
                .isDefault(false)
                .build();
    }
}
