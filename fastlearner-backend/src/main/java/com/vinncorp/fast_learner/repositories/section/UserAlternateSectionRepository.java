package com.vinncorp.fast_learner.repositories.section;

import com.vinncorp.fast_learner.models.section.UserAlternateSection;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAlternateSectionRepository extends JpaRepository<UserAlternateSection, Long> {
    boolean existsByCourse_IdAndFromSection_IdAndUser_Email(long courseId, long fromCourseId, String email);

    List<UserAlternateSection> findByCourse_IdAndFromSection_IdAndUser_Email(long courseId, long sectionId, String email);

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
        c.course_type AS course_type,
        cu.url as course_url,
        cc.name AS course_category,
        u.id AS instructor_id,
        u.full_name AS instructor_name,
        up.profile_picture,
        s.id AS section_id,
        s.name AS section_name,
        s.is_free AS is_free,
        COUNT(DISTINCT sr.id) AS total_reviews,
        COALESCE(AVG(sr.rating), 0) AS avg_section_rating,
        ta.tag_names AS tag_names,
        CASE WHEN EXISTS (
             SELECT 1 FROM enrollment e WHERE e.course_id = c.id AND e.student_id = ?4
             ) THEN true ELSE false END AS is_enrolled
        FROM course c
        INNER JOIN course_category cc ON c.course_category_id = cc.id
        INNER JOIN section s ON s.course_id = c.id
        INNER JOIN users u ON u.id = c.instructor_id
        LEFT JOIN user_profile up ON up.created_by = u.id
        LEFT JOIN section_review sr ON sr.section_id = s.id
        LEFT JOIN TagAggregates ta ON ta.course_id = c.id
        INNER JOIN course_url as cu ON cu.course_id = c.id
        WHERE c.id != ?1 AND c.course_status = 'PUBLISHED' AND cu.status = 'ACTIVE'
        AND s.id NOT IN (
            SELECT from_section_id FROM user_alternate_section
            WHERE user_id = ?4 AND course_id = ?1
        )
        GROUP BY
        c.id, c.title, cu.url, cc.name, u.id, u.full_name, up.profile_picture, s.id, s.name, ta.tag_names
        HAVING SIMILARITY(CAST((c.title || ' ' || ta.tag_names || ' ' || cc.name) AS TEXT), ?2) > 0.6
        AND SIMILARITY(CAST(s.name AS TEXT), ?3) > 0.5
        """,
            countQuery = """
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
                c.course_type AS course_type,
                cu.url as course_url,
                cc.name AS course_category,
                u.id AS instructor_id,
                u.full_name AS instructor_name,
                up.profile_picture,
                s.id AS section_id,
                s.name AS section_name,
                s.is_free AS is_free,
                COUNT(DISTINCT sr.id) AS total_reviews,
                COALESCE(AVG(sr.rating), 0) AS avg_section_rating,
                ta.tag_names AS tag_names,
                CASE WHEN EXISTS (
                     SELECT 1 FROM enrollment e WHERE e.course_id = c.id AND e.student_id = ?4
                     ) THEN true ELSE false END AS is_enrolled
                FROM course c
                INNER JOIN course_category cc ON c.course_category_id = cc.id
                INNER JOIN section s ON s.course_id = c.id
                INNER JOIN users u ON u.id = c.instructor_id
                LEFT JOIN user_profile up ON up.created_by = u.id
                LEFT JOIN section_review sr ON sr.section_id = s.id
                LEFT JOIN TagAggregates ta ON ta.course_id = c.id
                INNER JOIN course_url as cu ON cu.course_id = c.id
                WHERE c.id != ?1 AND c.course_status = 'PUBLISHED' AND cu.status = 'ACTIVE'
                AND s.id NOT IN (
                    SELECT from_section_id FROM user_alternate_section
                    WHERE user_id = ?4 AND course_id = ?1
                )
                GROUP BY
                c.id, c.title, cu.url, cc.name, u.id, u.full_name, up.profile_picture, s.id, s.name, ta.tag_names
                HAVING SIMILARITY(CAST((c.title || ' ' || ta.tag_names || ' ' || cc.name) AS TEXT), ?2) > 0.6
                AND SIMILARITY(CAST(s.name AS TEXT), ?3) > 0.5
                """, nativeQuery = true)
    Page<Tuple> findAlternateSections(Long courseId, String courseDetail, String sectionName, Long userId, PageRequest of);

    @Query(value = "SELECT * FROM user_alternate_section WHERE course_id = :courseId AND user_id = :userId LIMIT 1", nativeQuery = true)
    Optional<UserAlternateSection> findByCourseId(@Param("courseId") Long courseId, @Param("userId") Long userId);

}
