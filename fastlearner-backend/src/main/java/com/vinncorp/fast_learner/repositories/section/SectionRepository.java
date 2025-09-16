package com.vinncorp.fast_learner.repositories.section;

import com.vinncorp.fast_learner.models.section.Section;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {


    @Query(value = """
            SELECT Distinct s.id AS section_id, s.name AS section_name, s.sequence_number as section_level, s.is_free,  
            t.id AS topic_id, t.name AS topic_name, t.duration_in_sec as duration, tt.name as topic_type_name, 
            t.sequence_number as topic_level, a.content, v.id AS video_id, v.filename AS video_filename, 
            v.videourl as video_url, q.id AS quiz_id, q.title AS quiz_title,
            COALESCE(msr.max_rating, 0) AS sec_reviews, 
            COALESCE(msr.total_review, 0) AS total_sec_reviews
            FROM section as s  
            LEFT JOIN 
            	(SELECT sr.section_id, COUNT(sr.section_id) as total_review, AVG(sr.rating) as max_rating 
            	FROM section_review as sr GROUP BY sr.section_id) AS msr ON s.id = msr.section_id 
            LEFT JOIN topic t ON s.id = t.section_id 
            LEFT JOIN video v ON t.id = v.topic_id 
            LEFT JOIN article a ON a.topic_id = t.id
            LEFT JOIN quiz q ON t.id = q.topic_id 
            LEFT JOIN quiz_question qq ON qq.quiz_id = q.id 
            LEFT JOIN topic_type as tt ON tt.id = t.topic_type_id 
            WHERE s.course_id = :courseId AND s.is_active = true 
            ORDER BY s.sequence_number, t.sequence_number
            """, nativeQuery = true)
    List<Tuple> fetchSectionDetailByCourseId(Long courseId);

    @Query(value = """
    WITH user_alternate_sections AS (
        SELECT uas.from_section_id AS section_id
        FROM user_alternate_section uas
        WHERE uas.user_id = :userId AND uas.course_id = :courseId
    ), user_excluded_sections AS (
        SELECT uas.section_id
        FROM user_alternate_section uas
        WHERE uas.user_id = :userId AND uas.course_id = :courseId
    )
    SELECT * FROM (
        SELECT
            s.id,
            s.name,
            s.sequence_number,
            s.is_free,
            COUNT(DISTINCT t.id) AS topics,
            COUNT(DISTINCT ucp.id) AS completed,
            SUM(t.duration_in_sec) AS duration,
            COALESCE(msr.max_rating, 0) AS sec_reviews,
            COALESCE(msr.total_review, 0) AS total_sec_reviews,
            s.course_id,
            c.course_type AS course_type,
            CASE WHEN EXISTS (
                SELECT 1 FROM enrollment e WHERE e.course_id = s.course_id AND e.student_id = :userId
            ) THEN true ELSE false END AS is_enrolled
        FROM
            section s
        LEFT JOIN
            (SELECT sr.section_id, COUNT(sr.section_id) AS total_review, AVG(sr.rating) AS max_rating
             FROM section_review sr
             GROUP BY sr.section_id) AS msr ON s.id = msr.section_id
        INNER JOIN
            topic t ON t.section_id = s.id
        LEFT JOIN
            user_course_progress ucp ON ucp.topic_id = t.id AND ucp.student_id = :userId AND ucp.is_completed = true
        INNER JOIN 
            course AS c on c.id = s.course_id    
        WHERE
            s.id IN (SELECT section_id FROM user_alternate_sections)
        GROUP BY
            s.id, s.name, s.sequence_number, s.is_free, msr.max_rating, msr.total_review, s.course_id, c.course_type

        UNION ALL

        SELECT
            s.id,
            s.name,
            s.sequence_number,
            s.is_free,
            COUNT(DISTINCT t.id) AS topics,
            COUNT(DISTINCT ucp.id) AS completed,
            SUM(t.duration_in_sec) AS duration,
            COALESCE(msr.max_rating, 0) AS sec_reviews,
            COALESCE(msr.total_review, 0) AS total_sec_reviews,
            s.course_id,
            c.course_type AS course_type,
            CASE WHEN EXISTS (
                SELECT 1 FROM enrollment e WHERE e.course_id = s.course_id AND e.student_id = :userId
            ) THEN true ELSE false END AS is_enrolled
        FROM
            section s
        LEFT JOIN
            (SELECT sr.section_id, COUNT(sr.section_id) AS total_review, AVG(sr.rating) AS max_rating
             FROM section_review sr
             GROUP BY sr.section_id) AS msr ON s.id = msr.section_id
        INNER JOIN
            topic t ON t.section_id = s.id
        LEFT JOIN
            user_course_progress ucp ON ucp.topic_id = t.id AND ucp.student_id = :userId AND ucp.is_completed = true
        INNER JOIN course AS c on c.id = s.course_id    
        WHERE
            s.course_id = :courseId
            AND s.id NOT IN (SELECT section_id FROM user_excluded_sections)
        GROUP BY
            s.id, s.name, s.sequence_number, s.is_free, msr.max_rating, msr.total_review, s.course_id, c.course_type
    ) AS combined_result
    ORDER BY combined_result.sequence_number
    """,
            nativeQuery = true
    )
    List<Tuple> findAllByCourseId(Long courseId, Long userId);


    @Query(value = """
            SELECT s.id, s.name, s.sequence_number as sequence, s.is_free as isFree FROM course as c
            INNER JOIN section as s ON s.course_id = c.id
            WHERE c.id = :courseId AND c.created_by = :userId ORDER BY s.sequence_number ASC
            """, nativeQuery = true)
    List<Tuple> findAllByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Long userId);
    List<Section> findAllByCourseId(Long courseId);
}