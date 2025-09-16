package com.vinncorp.fast_learner.repositories.video;

import com.vinncorp.fast_learner.models.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByTopicId(Long topicId);

    boolean existsByVideoURL(String videoURL);
}