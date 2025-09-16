package com.vinncorp.fast_learner.repositories.topic;

import com.vinncorp.fast_learner.models.topic.TopicNotes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicNotesRepository extends JpaRepository<TopicNotes, Long> {

    Page<TopicNotes> findByTopic_Section_Course_IdInAndCreatedBy(List<Long> courseIds, Long id, Pageable page);

    Optional<TopicNotes> findByIdAndCreatedBy(Long topicNoteId, Long userId);
}
