package com.vinncorp.fast_learner.repositories.favourite_course;

import com.vinncorp.fast_learner.models.favourite_course.FavouriteCourse;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FavouriteCourseRepository extends JpaRepository<FavouriteCourse, Long> {

    @Query(value = """
            SELECT DISTINCT c.id AS course_id, c.title AS course_title, cu.url AS course_url, c.description AS course_description, c.thumbnail AS course_thumbnail, 
            us.id as user_id, us.full_name, up.profile_picture, up.profile_url, c.course_duration_in_hours, c.content_type, COALESCE(mcr.max_rating, 0) AS max_rating, 
            COALESCE(mcr.total_review, 0) AS total_reviews, cc.name as category, 
            CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled, 
            CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
            (SELECT COUNT(t.id) FROM topic as t INNER JOIN section as s ON s.id = t.section_id WHERE s.course_id = c.id) as total_topics,
            (SELECT SUM(t1.duration_in_sec) FROM topic as t1 INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id WHERE c1.id = c.id) as duration,
            c.course_type, c.price,cl.name AS course_level,
            CASE WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE' ELSE 'FALSE' END AS already_bought, COUNT(CASE WHEN c.content_type = 'TEST' THEN qq.id END) AS test_total_question
            FROM course AS c LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating  
                FROM course_review AS cr GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id  
            INNER JOIN course_category as cc ON c.course_category_id = cc.id 
            INNER JOIN users as us ON us.id = c.created_by 
            INNER JOIN favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId 
            LEFT JOIN user_profile as up ON up.created_by = us.id 
            LEFT JOIN enrollment as e ON e.course_id = c.id AND e.student_id = :userId 
            INNER JOIN course_level AS cl ON c.course_level = cl.id
            INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
            LEFT JOIN (
            SELECT COUNT(DISTINCT tp.id) AS no_of_topics, c.id\s
            FROM course c\s
            INNER JOIN section s ON s.course_id = c.id AND s.is_active = true\s
            LEFT JOIN topic tp ON tp.section_id = s.id\s
            GROUP BY c.id
            ) AS topic ON topic.id = c.id\s
            INNER JOIN section as s ON s.course_id = c.id
            INNER JOIN topic as t ON t.section_id = s.id 
            LEFT JOIN quiz as q ON q.topic_id = t.id
            LEFT JOIN quiz_question as qq ON qq.quiz_id = q.id            
            WHERE (:title is null OR c.title ilike :title) AND c.course_status = 'PUBLISHED' 
            GROUP BY\s
                          c.id, c.title, cu.url, c.description, c.thumbnail, us.id, us.full_name,\s
                          up.profile_picture, up.profile_url, c.course_duration_in_hours,\s
                          cc.name, e.id, fc.id, c.course_type,\s
                          c.price, c.content_type, cl.name, mcr.max_rating, mcr.total_review, c.content_type, no_of_topics
            ORDER BY max_rating, course_id DESC
            """,
            countQuery = """
                            SELECT DISTINCT c.id AS course_id, c.title AS course_title, cu.url AS course_url, c.description AS course_description, c.content_type, c.thumbnail AS course_thumbnail, 
                    us.id as user_id, us.full_name, up.profile_picture, up.profile_url, c.course_duration_in_hours, COALESCE(mcr.max_rating, 0) AS max_rating, 
                    COALESCE(mcr.total_review, 0) AS total_reviews, cc.name as category, 
                    CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled, 
                    CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
                    (SELECT COUNT(t.id) FROM topic as t INNER JOIN section as s ON s.id = t.section_id WHERE s.course_id = c.id) as total_topics,
                    (SELECT SUM(t1.duration_in_sec) FROM topic as t1 INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id WHERE c1.id = c.id) as duration,
                    c.course_type, c.price,cl.name AS course_level,
                    CASE WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE' ELSE 'FALSE' END AS already_bought, COUNT(CASE WHEN c.content_type = 'TEST' THEN qq.id END) AS test_total_question
                    FROM course AS c LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating  
                        FROM course_review AS cr GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id  
                    INNER JOIN course_category as cc ON c.course_category_id = cc.id 
                    INNER JOIN users as us ON us.id = c.created_by 
                    INNER JOIN favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId 
                    LEFT JOIN user_profile as up ON up.created_by = us.id 
                    LEFT JOIN enrollment as e ON e.course_id = c.id AND e.student_id = :userId 
                    INNER JOIN course_level AS cl ON c.course_level = cl.id
                    INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                    LEFT JOIN (
                    SELECT COUNT(DISTINCT tp.id) AS no_of_topics, c.id\s
                    FROM course c\s
                    INNER JOIN section s ON s.course_id = c.id AND s.is_active = true\s
                    LEFT JOIN topic tp ON tp.section_id = s.id\s
                    GROUP BY c.id
                    ) AS topic ON topic.id = c.id\s
                    INNER JOIN section as s ON s.course_id = c.id
                    INNER JOIN topic as t ON t.section_id = s.id 
                    LEFT JOIN quiz as q ON q.topic_id = t.id
                    LEFT JOIN quiz_question as qq ON qq.quiz_id = q.id
                    WHERE (:title is null OR c.title ilike :title) AND c.course_status = 'PUBLISHED' 
                    GROUP BY\s
                          c.id, c.title, cu.url, c.description, c.thumbnail, us.id, us.full_name,\s
                          up.profile_picture, up.profile_url, c.course_duration_in_hours,\s
                          cc.name, e.id, fc.id, c.course_type,\s
                          c.price, c.content_type, cl.name, mcr.max_rating, mcr.total_review, c.content_type, no_of_topics
                    ORDER BY max_rating, course_id DESC
                            """,
            nativeQuery = true)
    Page<Tuple> findFavouriteCoursesByTitle(String title, Long userId, PageRequest pageRequest);

    Optional<FavouriteCourse> findByCourseIdAndCreatedBy(Long courseId, Long userId);
}
