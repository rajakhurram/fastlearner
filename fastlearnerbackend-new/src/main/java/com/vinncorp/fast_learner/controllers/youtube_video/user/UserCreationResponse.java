package com.vinncorp.fast_learner.controllers.youtube_video.user;

import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationResponse {

    private String fullName;
    private String email;
    private String provider;
    private Role role;

    public static UserCreationResponse fromUser(User user) {
        return UserCreationResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .provider(user.getProvider().name())
                .role(user.getRole())
                .build();
    }
}
