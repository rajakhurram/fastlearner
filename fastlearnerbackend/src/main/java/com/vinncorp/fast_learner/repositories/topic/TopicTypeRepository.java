package com.vinncorp.fast_learner.repositories.topic;

import com.vinncorp.fast_learner.models.topic.TopicType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicTypeRepository extends JpaRepository<TopicType, Long> {
    List<TopicType> findByIsActive(boolean b);
}