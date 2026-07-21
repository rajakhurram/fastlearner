package com.vinncorp.fast_learner.models.user;

import com.vinncorp.fast_learner.models.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@MappedSuperclass
public class UserSocialProfile extends Auditable<Long> {

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;
    @Column(name = "facebook_url", columnDefinition = "TEXT")
    private String facebookUrl;
    @Column(name = "twitter_url", columnDefinition = "TEXT")
    private String twitterUrl;
    @Column(name = "linked_in_url", columnDefinition = "TEXT")
    private String linkedInUrl;
    @Column(name = "youtube_url", columnDefinition = "TEXT")
    private String youtubeUrl;
}
