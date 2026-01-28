package com.vinncorp.fast_learner.models.user;


import com.vinncorp.fast_learner.dtos.user.UserProfileDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profile")
public class UserProfile extends UserSocialProfile{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String profilePicture;
    @Column(columnDefinition = "TEXT")
    private String profileUrl;  // it will be unique
    @Column(name = "headline", columnDefinition = "TEXT")
    private String headline;
    @Column(name = "about_me", columnDefinition = "TEXT")
    private String aboutMe;
    @Column(name = "qualification", columnDefinition = "TEXT")
    private String qualification;
    @Column(name = "experience", columnDefinition = "TEXT")
    private String experience;
    @Column(name = "specialization", columnDefinition = "TEXT")
    private String specialization;

    @Column(name = "show_profile")
    private boolean showProfile = true;

    @Column(name = "show_courses")
    private boolean showCourses=true;

    public static UserProfile mapToUser(UserProfileDto requestDto, User user, UserProfile userProfile){
        userProfile.setSpecialization(requestDto.getSpecialization());
        userProfile.setQualification(requestDto.getQualification());
        userProfile.setExperience(requestDto.getExperience());
        userProfile.setHeadline(requestDto.getHeadline());
        userProfile.setAboutMe(requestDto.getAboutMe());
        userProfile.setProfilePicture(requestDto.getProfilePicture());
        userProfile.setShowProfile(requestDto.isShowProfile());
        userProfile.setShowCourses(requestDto.isShowCourses());
        userProfile.setWebsiteUrl(requestDto.getWebsiteUrl());
        userProfile.setTwitterUrl(requestDto.getTwitterUrl());
        userProfile.setFacebookUrl(requestDto.getFacebookUrl());
        userProfile.setLinkedInUrl(requestDto.getLinkedInUrl());
        userProfile.setYoutubeUrl(requestDto.getYoutubeUrl());
        userProfile.setModifiedBy(user.getId());
        userProfile.setLastModifiedDate(new Date());

        return userProfile;
    }
}
