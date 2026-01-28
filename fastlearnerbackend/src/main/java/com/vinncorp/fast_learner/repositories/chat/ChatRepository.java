package com.vinncorp.fast_learner.repositories.chat;

import com.vinncorp.fast_learner.models.chat.Chat;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Chat findByTimeAndVideo_IdAndCreatedBy(String time, Long videoId, Long userId);

    @Query(value = """
            SELECT s.id as section_id, s.sequence_number, s.name as section_name, 
            t.id as topic_id, t.name, t.sequence_number as topic_seq,
            v.id as video_id, c.id as chat_id, c.time as times,c.title 
            FROM section s 
            INNER JOIN topic t ON t.section_id = s.id 
            RIGHT JOIN video v ON v.topic_id = t.id 
            RIGHT JOIN chat c ON c.video_id = v.id 
            WHERE s.course_id = :courseId AND c.created_by = :userId
            Group By s.id, t.id, v.id, c.id
            Order By t.sequence_number
            """, nativeQuery = true)
    List<Tuple> findAllByCourseId(Long courseId, Long userId);
}
