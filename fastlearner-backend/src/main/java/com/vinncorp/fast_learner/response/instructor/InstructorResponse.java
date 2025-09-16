package com.vinncorp.fast_learner.response.instructor;

import jakarta.persistence.Tuple;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InstructorResponse {

    private Long courseId;
    private String fullName;
    private String specialization;
    private String userProfileUrl;
    private Long userId;
    private String profilePicture;
    private String aboutMe;

    public static InstructorResponse from(Tuple e){
        return InstructorResponse.builder()
                .courseId((Long) e.get("course_id"))
                .fullName((String) e.get("full_name"))
                .specialization(e.get("specialization")==null ?" ":(String) e.get("specialization"))
                .userProfileUrl((String) e.get("profile_url"))
                .userId((Long) e.get("user_id"))
                .profilePicture(e.get("profile_picture")==null ?" ":(String) e.get("profile_picture"))
                .aboutMe(e.get("about_me")==null?" ":(String) e.get("about_me"))
                .build();
    }

}
