package com.vinncorp.fast_learner.repositories.topic;

import com.vinncorp.fast_learner.models.topic.UserAlternateTopic;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserAlternateTopicRepository extends JpaRepository<UserAlternateTopic, Long> {
    Optional<UserAlternateTopic> findByCourse_IdAndFromTopic_IdAndUser_Email(Long id, Long id1, String email);

    boolean existsByCourse_IdAndFromTopic_IdAndUser_Email(Long id, Long id1, String email);

    @Query(value = """
            WITH TagAggregates AS (
                SELECT
                    c.id AS course_id,
                    STRING_AGG(t.name, ' ' ORDER BY t.name) AS tag_names
                FROM
                    course c
                    INNER JOIN course_tag ct ON ct.course_id = c.id
                    INNER JOIN tag t ON t.id = ct.tag_id
                GROUP BY
                    c.id
            )
                        
            SELECT
                c.id AS course_id,
                c.title AS course_name,
                cc.name AS course_category,
                u.id AS instructor_id,
                u.full_name AS instructor_name,
                up.profile_picture,
                s.id AS section_id,
                s.name AS section_name,
                MIN(tp.id) AS topic_id,
                tp.name AS topic_name,
                COUNT(DISTINCT sr.id) AS total_reviews,
                COALESCE(AVG(sr.rating), 0) AS avg_section_rating,
                ta.tag_names AS tag_names
            FROM
                course c
                INNER JOIN course_category cc ON c.course_category_id = cc.id
                INNER JOIN section s ON s.course_id = c.id
                INNER JOIN topic tp ON tp.section_id = s.id
                INNER JOIN users u ON u.id = c.instructor_id
                LEFT JOIN user_profile up ON up.created_by = u.id
                LEFT JOIN section_review sr ON sr.section_id = s.id
                LEFT JOIN TagAggregates ta ON ta.course_id = c.id
            WHERE
                c.id != :courseId
                AND tp.id NOT IN (
                    SELECT from_topic_id FROM user_alternate_topic
                    WHERE user_id = :userId AND course_id = :courseId
                )
            GROUP BY
                c.id, c.title, cc.name, u.id, u.full_name, up.profile_picture, s.id, s.name, tp.name, ta.tag_names
            HAVING
                SIMILARITY(CAST((c.title || ' ' || ta.tag_names) AS TEXT), :courseDetail) > 0.6
                AND SIMILARITY(CAST(tp.name AS TEXT), :topicName) > 0.6;
            """, countQuery = """
            WITH TagAggregates AS (
                SELECT
                    c.id AS course_id,
                    STRING_AGG(t.name, ' ' ORDER BY t.name) AS tag_names
                FROM
                    course c
                    INNER JOIN course_tag ct ON ct.course_id = c.id
                    INNER JOIN tag t ON t.id = ct.tag_id
                GROUP BY
                    c.id
            )
                        
            SELECT
                c.id AS course_id,
                c.title AS course_name,
                cc.name AS course_category,
                u.id AS instructor_id,
                u.full_name AS instructor_name,
                up.profile_picture,
                s.id AS section_id,
                s.name AS section_name,
                MIN(tp.id) AS topic_id,
                tp.name AS topic_name,
                COUNT(DISTINCT sr.id) AS total_reviews,
                COALESCE(AVG(sr.rating), 0) AS avg_section_rating,
                ta.tag_names AS tag_names
            FROM
                course c
                INNER JOIN course_category cc ON c.course_category_id = cc.id
                INNER JOIN section s ON s.course_id = c.id
                INNER JOIN topic tp ON tp.section_id = s.id
                INNER JOIN users u ON u.id = c.instructor_id
                LEFT JOIN user_profile up ON up.created_by = u.id
                LEFT JOIN section_review sr ON sr.section_id = s.id
                LEFT JOIN TagAggregates ta ON ta.course_id = c.id
            WHERE
                c.id != :courseId
                AND tp.id NOT IN (
                    SELECT from_topic_id FROM user_alternate_topic
                    WHERE user_id = :userId AND course_id = :courseId
                )
            GROUP BY
                c.id, c.title, cc.name, u.id, u.full_name, up.profile_picture, s.id, s.name, tp.name, ta.tag_names
            HAVING
                SIMILARITY(CAST((c.title || ' ' || ta.tag_names) AS TEXT), :courseDetail) > 0.6
                AND SIMILARITY(CAST(tp.name AS TEXT), :topicName) > 0.6;
            """, nativeQuery = true)
    Page<Tuple> findAlternativeTopics(Long courseId, String courseDetail, String topicName, Long userId, PageRequest page);

}
