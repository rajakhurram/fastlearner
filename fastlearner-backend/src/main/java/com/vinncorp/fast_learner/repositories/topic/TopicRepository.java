package com.vinncorp.fast_learner.repositories.topic;

import com.vinncorp.fast_learner.models.topic.Topic;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Query(value = "select s.course_id, count(t.id) as total_topics, SUM(t.duration_in_sec) as duration from topic as t " +
            "INNER JOIN section as s ON s.id = t.section_id AND s.course_id IN :courseIdList " +
            "GROUP BY s.course_id", nativeQuery = true)
    List<Tuple> findAllTopicsByCourseIdList(List<Long> courseIdList);

    @Query(value = """
            SELECT
                json_agg(
                    json_build_object('id', doc.id, 'name', doc.name, 'url', doc.url, 'summary', doc.summary)
                ) AS document_object,
                t.id,
                t.section_id,
                t.sequence_number,
                t.name,
                t.duration_in_sec,
                t.topic_type_id,
                COALESCE(t.created_date, t.last_mod_date) as creation_date,
            	tt.name as type_name, v.id as video_id, v.filename, v.videourl, v.transcribe, v.vtt_content, v.summary as video_summary, q.id as 
            quiz_id, q.title, q.random_question,q.duration_in_minutes, q.passing_criteria, art.id as article_id, art.content, ucp.id as progress_id, ucp.is_completed, ucp.seek_time
            FROM
                topic AS t
            INNER JOIN topic_type AS tt ON tt.id = t.topic_type_id
            INNER JOIN section AS s ON s.id = t.section_id
            LEFT JOIN article AS art ON art.topic_id = t.id
            LEFT JOIN video AS v ON v.topic_id = t.id
            LEFT JOIN document AS doc ON doc.video_id = v.id OR doc.article_id = art.id
            LEFT JOIN quiz AS q ON q.topic_id = t.id
            LEFT JOIN user_course_progress AS ucp ON ucp.student_id = :userId AND ucp.topic_id = t.id
            WHERE
                t.section_id = :sectionId
            GROUP BY
                t.id, t.section_id, t.sequence_number, t.name, t.duration_in_sec, t.topic_type_id, 
            	tt.name, v.id, v.filename, v.videourl, q.id, q.title, art.id, art.content, ucp.id, ucp.is_completed, ucp.seek_time
            ORDER BY
                sequence_number ASC;
            """, nativeQuery = true)
    List<Tuple> findAllBySectionId(Long sectionId, Long userId);

    List<Topic> findBySectionId(Long sectionId);
}