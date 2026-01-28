package com.vinncorp.fast_learner.dtos.user.user_profile_visit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InstructorProfileSearchDto {
    private Long id;
    private String fullName;
    private String profilePicture;
    private String profileUrl;

    public InstructorProfileSearchDto(Long id, String fullName, String profilePicture) {
        this.id = id;
        this.fullName = fullName;
        this.profilePicture = profilePicture;
    }
}
