package com.vinncorp.fast_learner.dtos.user;

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
public class UserProfileDto {

    private Long userId;
    @NotEmpty(message = "Name cannot be empty.")
    private String fullName;
    @NotEmpty(message = "Email cannot be empty.")
    private String email;
    private String profilePicture;
    private String userProfileUrl;
    private String specialization;
    private String qualification;
    private String experience;
    private String headline;
    private String aboutMe;
    private boolean showProfile;
    private boolean showCourses;
    private String websiteUrl;
    private String facebookUrl;
    private String twitterUrl;
    private String linkedInUrl;
    private String youtubeUrl;
    private Long totalStudents;
    private Long totalReviews;


    public static UserProfileDto fromTuple (Tuple userProfile){
        return UserProfileDto.builder()
                .userId((Long) userProfile.get("id"))
                .fullName((String) userProfile.get("full_name"))
                .email((String) userProfile.get("email"))
                .profilePicture((String) userProfile.get("profile_picture"))
                .userProfileUrl((String) userProfile.get("profile_url"))
                .specialization((String) userProfile.get("specialization"))
                .qualification((String) userProfile.get("qualification"))
                .experience((String) userProfile.get("experience"))
                .headline((String) userProfile.get("headline"))
                .aboutMe((String) userProfile.get("about_me"))
                .showProfile(userProfile.get("show_profile") != null && Boolean.parseBoolean("" + userProfile.get("show_profile")))
                .showCourses( userProfile.get("show_courses") != null && Boolean.parseBoolean("" + userProfile.get("show_courses")))
                .websiteUrl((String) userProfile.get("website_url"))
                .facebookUrl((String) userProfile.get("facebook_url"))
                .twitterUrl((String) userProfile.get("twitter_url"))
                .linkedInUrl((String) userProfile.get("linked_in_url"))
                .youtubeUrl((String)userProfile.get("youtube_url"))
                .totalReviews((Long) userProfile.get("total_reviews"))
                .totalStudents((Long) userProfile.get("total_students"))
                .build();
    }

}
