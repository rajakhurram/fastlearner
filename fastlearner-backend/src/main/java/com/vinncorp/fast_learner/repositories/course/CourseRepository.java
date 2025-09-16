package com.vinncorp.fast_learner.repositories.course;

import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.course.CourseDetailByType;
import com.vinncorp.fast_learner.models.user.UserCourseCompletion;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import com.vinncorp.fast_learner.response.course.CourseDetailResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.CourseType;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByIdAndCertificateEnabled(Long id, boolean certificateEnabled);

    Optional<Course> findByIdAndCreatedBy(Long courseId, Long userId);

    @Query(value = """
            SELECT DISTINCT c.id AS course_id, c.title AS course_title, cu.url AS course_url, c.description AS course_description, c.about,
                   c.thumbnail AS course_thumbnail, c.preview_videourl as video_url, c.preview_video_vtt_content, c.prerequisite, c.course_outcome,
                   c.last_mod_date, c.content_type, cl.name as level, us.id as user_id, us.full_name, up.profile_picture, up.profile_url, up.about_me,
                   up.headline, c.course_duration_in_hours, COALESCE(mcr.max_rating, 0) AS max_rating, COALESCE(mcr.total_review, 0) AS total_reviews,
                   cc.name as category, CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
                   CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite, c.certificate_enabled as has_certificate, c.created_date,
                   COALESCE(tc.total_students, 0) AS total_students, c.meta_title ,c.meta_description, c.meta_heading, c.course_type, c.price,
                   CASE WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE' ELSE 'FALSE'
                   END AS already_bought, COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                   FROM course AS c
                   LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating
                       FROM course_review AS cr WHERE cr.comment is not null GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
                   INNER JOIN course_category as cc ON c.course_category_id = cc.id
                   INNER JOIN users as us ON us.id = c.created_by
                   INNER JOIN course_level as cl ON c.course_level = cl.id
                   LEFT JOIN user_profile as up ON up.created_by = us.id
                   LEFT JOIN enrollment as e ON e.course_id = c.id AND e.student_id = :userId
                   LEFT JOIN favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId
                   INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                   INNER JOIN section as s ON s.course_id = c.id
                   INNER JOIN topic as t ON t.section_id = s.id 
                   LEFT JOIN quiz as q ON q.topic_id = t.id
                                      
                   LEFT JOIN (
                   SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
                   ) AS q_summary ON q_summary.topic_id = t.id
                                      
                   LEFT JOIN (SELECT e.course_id, COUNT(e.student_id) AS total_students FROM enrollment AS e WHERE
                                  e.course_id IN (SELECT id FROM course WHERE course_status = 'PUBLISHED' AND (:courseId IS NULL OR id = :courseId))
                                  AND e.student_id != :userId
                               GROUP BY e.course_id) AS tc ON c.id = tc.course_id
                   WHERE
                      (:courseLevelId is null OR cl.id = :courseLevelId) AND
                       (:categoryId is null OR c.course_category_id = :categoryId) AND
                      (:courseId is null OR c.id = :courseId) AND c.course_status = 'PUBLISHED'
                   GROUP BY c.id, cu.url, c.description, c.about, c.thumbnail, c.preview_videourl, c.preview_video_vtt_content,\s
                            c.prerequisite, c.course_outcome, c.last_mod_date, c.content_type, cl.name, us.id, us.full_name, up.profile_picture,\s
                            up.profile_url, up.about_me, up.headline, c.course_duration_in_hours, mcr.max_rating, mcr.total_review, cc.name,\s
                            e.id, fc.id, c.certificate_enabled, c.created_date, tc.total_students, c.meta_title, c.meta_description, c.meta_heading,\s
                            c.course_type, c.price   
                   ORDER BY max_rating DESC    
            """, countQuery = """
                    SELECT DISTINCT c.id AS course_id, c.title AS course_title, cu.url AS course_url, c.description AS course_description, c.about,
                                      c.thumbnail AS course_thumbnail, c.preview_videourl as video_url, c.preview_video_vtt_content, c.prerequisite, c.course_outcome,
                                      c.last_mod_date, c.content_type, cl.name as level, us.id as user_id, us.full_name, up.profile_picture, up.profile_url, up.about_me,
                                      up.headline, c.course_duration_in_hours, COALESCE(mcr.max_rating, 0) AS max_rating, COALESCE(mcr.total_review, 0) AS total_reviews,
                                      cc.name as category, CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
                                      CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite, c.certificate_enabled as has_certificate, c.created_date,
                                      COALESCE(tc.total_students, 0) AS total_students, c.meta_title ,c.meta_description, c.meta_heading, c.course_type, c.price,
                                      CASE WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE' ELSE 'FALSE' END AS already_bought,
                                      COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                                      FROM course AS c
                                      LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating
                                          FROM course_review AS cr WHERE cr.comment is not null GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
                                      INNER JOIN course_category as cc ON c.course_category_id = cc.id
                                      INNER JOIN users as us ON us.id = c.created_by
                                      INNER JOIN course_level as cl ON c.course_level = cl.id
                                      LEFT JOIN user_profile as up ON up.created_by = us.id
                                      LEFT JOIN enrollment as e ON e.course_id = c.id AND e.student_id = :userId
                                      LEFT JOIN favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId
                                      INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                                      INNER JOIN section as s ON s.course_id = c.id
                                      INNER JOIN topic as t ON t.section_id = s.id 
                                      LEFT JOIN quiz as q ON q.topic_id = t.id
                                      
                                      LEFT JOIN (
                                          SELECT topic_id, SUM(random_question) AS total_random_questions
                                          FROM quiz
                                          GROUP BY topic_id
                                      ) AS q_summary ON q_summary.topic_id = t.id
                                      
                                      LEFT JOIN (SELECT e.course_id, COUNT(e.student_id) AS total_students FROM enrollment AS e WHERE
                                                     e.course_id IN (SELECT id FROM course WHERE course_status = 'PUBLISHED' AND (:courseId IS NULL OR id = :courseId))
                                                     AND e.student_id != :userId
                                                  GROUP BY e.course_id) AS tc ON c.id = tc.course_id
                                      WHERE
                                         (:courseLevelId is null OR cl.id = :courseLevelId) AND
                                          (:categoryId is null OR c.course_category_id = :categoryId) AND
                                         (:courseId is null OR c.id = :courseId) AND c.course_status = 'PUBLISHED'
                                      GROUP BY c.id, cu.url, c.description, c.about, c.thumbnail, c.preview_videourl, c.preview_video_vtt_content,\s
                                               c.prerequisite, c.course_outcome, c.last_mod_date, c.content_type, cl.name, us.id, us.full_name, up.profile_picture,\s
                                               up.profile_url, up.about_me, up.headline, c.course_duration_in_hours, mcr.max_rating, mcr.total_review, cc.name,\s
                                               e.id, fc.id, c.certificate_enabled, c.created_date, tc.total_students, c.meta_title, c.meta_description, c.meta_heading,\s
                                               c.course_type, c.price   
                                      ORDER BY max_rating DESC
            """,nativeQuery = true)
    Page<Tuple> findAllByCoursesCategoryOrCourseIdAndMostReviewed(Long categoryId, Long courseLevelId, Long courseId, Long userId, PageRequest of);


    @Query(value = """
            SELECT c.id AS course_id, c.title AS course_title, cu.url AS course_url, c.description AS course_description,
                   c.about, c.content_type, c.thumbnail AS course_thumbnail, c.preview_videourl as video_url,
                   c.preview_video_vtt_content, c.prerequisite, c.course_outcome, c.last_mod_date,
                   cl.name as level, us.id as user_id, us.full_name, up.profile_picture, up.about_me,
                   up.headline, c.course_duration_in_hours, COALESCE(mcr.max_rating, 0) AS max_rating,
                   COALESCE(mcr.total_review, 0) AS total_reviews, cc.name as category,
                   c.certificate_enabled as has_certificate, c.meta_title, c.meta_description,
                   c.meta_heading, c.course_type, c.price,
                   COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                   CASE WHEN :userId IS NULL THEN 'FALSE'
                        WHEN c.course_type = 'PREMIUM_COURSE'
                        AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                        THEN 'TRUE' ELSE 'FALSE' END AS already_bought
            FROM course AS c
            LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating
                       FROM course_review AS cr GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
            INNER JOIN course_category as cc ON c.course_category_id = cc.id
            INNER JOIN users as us ON us.id = c.created_by
            INNER JOIN course_level as cl ON c.course_level = cl.id
            LEFT JOIN user_profile as up ON up.created_by = us.id
            LEFT JOIN enrollment AS e ON e.course_id = c.id
            LEFT JOIN section AS s ON s.course_id = c.id
            INNER JOIN topic as t ON t.section_id = s.id
            LEFT JOIN quiz as q ON q.topic_id = t.id
            
            LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
            
            INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
            WHERE c.created_by = :instructorId AND c.course_status = 'PUBLISHED'
            GROUP BY c.id, c.title, cu.url, c.description, c.about, c.content_type,
                     c.thumbnail, c.preview_videourl, c.preview_video_vtt_content,
                     c.prerequisite, c.course_outcome, c.last_mod_date, cl.name, us.id,
                     us.full_name, up.profile_picture, up.about_me, up.headline,
                     c.course_duration_in_hours, cc.name, c.certificate_enabled, c.meta_title,
                     c.meta_description, c.meta_heading, c.course_type, c.price, mcr.max_rating,
                     mcr.total_review
            ORDER BY max_rating DESC, course_id DESC;
                        
            """, countQuery = """
            SELECT c.id AS course_id, c.title AS course_title, cu.url AS course_url, c.description AS course_description,
                   c.about, c.content_type, c.thumbnail AS course_thumbnail, c.preview_videourl as video_url,
                   c.preview_video_vtt_content, c.prerequisite, c.course_outcome, c.last_mod_date,
                   cl.name as level, us.id as user_id, us.full_name, up.profile_picture, up.about_me,
                   up.headline, c.course_duration_in_hours, COALESCE(mcr.max_rating, 0) AS max_rating,
                   COALESCE(mcr.total_review, 0) AS total_reviews, cc.name as category,
                   c.certificate_enabled as has_certificate, c.meta_title, c.meta_description,
                   c.meta_heading, c.course_type, c.price,
                   COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                   CASE WHEN :userId IS NULL THEN 'FALSE'
                        WHEN c.course_type = 'PREMIUM_COURSE'
                        AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                        THEN 'TRUE' ELSE 'FALSE' END AS already_bought
            FROM course AS c
            LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating
                       FROM course_review AS cr GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
            INNER JOIN course_category as cc ON c.course_category_id = cc.id
            INNER JOIN users as us ON us.id = c.created_by
            INNER JOIN course_level as cl ON c.course_level = cl.id
            LEFT JOIN user_profile as up ON up.created_by = us.id
            LEFT JOIN enrollment AS e ON e.course_id = c.id
            LEFT JOIN section AS s ON s.course_id = c.id
            INNER JOIN topic as t ON t.section_id = s.id
            LEFT JOIN quiz as q ON q.topic_id = t.id
            
            LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
            
            INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
            WHERE c.created_by = :instructorId AND c.course_status = 'PUBLISHED'
            GROUP BY c.id, c.title, cu.url, c.description, c.about, c.content_type,
                     c.thumbnail, c.preview_videourl, c.preview_video_vtt_content,
                     c.prerequisite, c.course_outcome, c.last_mod_date, cl.name, us.id,
                     us.full_name, up.profile_picture, up.about_me, up.headline,
                     c.course_duration_in_hours, cc.name, c.certificate_enabled, c.meta_title,
                     c.meta_description, c.meta_heading, c.course_type, c.price, mcr.max_rating,
                     mcr.total_review
            ORDER BY max_rating DESC, course_id DESC;
                        
            """, nativeQuery = true)
    Page<Tuple> findAllByInstructorAndLoggedInUser(Long instructorId,Long userId, PageRequest of);

    @Query(value = "select count(DISTINCT c.id) as courses, COALESCE(COUNT( DISTINCT e.student_id),0) as enrolled_students from course as c " + "LEFT JOIN enrollment as e ON e.course_id = c.id where c.created_by = :userId AND c.course_status = 'PUBLISHED' AND e.student_id != :userId", nativeQuery = true)
    Tuple findCoursesAndStudentEnrolledByUserId(Long userId);


    @Query(value = """
            SELECT DISTINCT
                rc.id,
                rc.instructor_id,
                rc.title,
                rc.course_url,
                rc.description,
                rc.thumbnail,
                rc.name,
                rc.full_name,
                rc.max_rating,
                rc.total_reviews,
                up.profile_picture,
                CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
                CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
                total_topics.total_topics
            FROM
                (
                    SELECT
                        c.id,
                        c.title,
                        c.course_url,
                        c.description,
                        c.thumbnail,
                        c.instructor_id,
                        t.name AS tag_name,
                        cc.name,
                        u.full_name,
                        COALESCE(mcr.max_rating, 0) AS max_rating,
                        COALESCE(mcr.total_review, 0) AS total_reviews,
                        ROW_NUMBER() OVER (PARTITION BY c.id ORDER BY c.id) AS rn
                    FROM
                        course as c
                    INNER JOIN
                        course_category as cc ON cc.id = c.course_category_id
                    INNER JOIN
                        users as u ON u.id = c.instructor_id
                    LEFT JOIN
                        course_tag as ct ON ct.course_id = c.id
                    LEFT JOIN
                        tag as t ON t.id = ct.tag_id
                    LEFT JOIN
                        (
                            SELECT
                                cr.course_id,
                                COUNT(cr.course_id) AS total_review,
                                AVG(cr.rating) AS max_rating
                            FROM
                                course_review AS cr
                            GROUP BY
                                cr.course_id
                        ) AS mcr ON c.id = mcr.course_id
                    WHERE
                        c.id in (:courseIds)
                ) as rc
            LEFT JOIN
                enrollment as e ON e.course_id = rc.id AND e.student_id = :userId
            LEFT JOIN
                favourite_course as fc ON fc.course_id = rc.id AND fc.created_by = :userId
            LEFT JOIN
                user_profile as up ON up.created_by = rc.instructor_id
            LEFT JOIN
                (
                    SELECT
                        s.course_id,
                        count(t.id) as total_topics
                    FROM
                        topic as t
                    INNER JOIN
                        section as s ON s.id = t.section_id AND s.course_id IN (:courseIds)
                    GROUP BY
                        s.course_id
                ) as total_topics ON total_topics.course_id = rc.id;
            """, countQuery = """
            SELECT DISTINCT
                rc.id,
                rc.instructor_id,
                rc.title,
                rc.course_url,
                rc.description,
                rc.thumbnail,
                rc.name,
                rc.full_name,
                rc.max_rating,
                rc.total_reviews,
                up.profile_picture,
                CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
                CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
                total_topics.total_topics
            FROM
                (
                    SELECT
                        c.id,
                        c.title,
                        c.course_url,
                        c.description,
                        c.thumbnail,
                        c.instructor_id,
                        t.name AS tag_name,
                        cc.name,
                        u.full_name,
                        COALESCE(mcr.max_rating, 0) AS max_rating,
                        COALESCE(mcr.total_review, 0) AS total_reviews,
                        ROW_NUMBER() OVER (PARTITION BY c.id ORDER BY c.id) AS rn
                    FROM
                        course as c
                    INNER JOIN
                        course_category as cc ON cc.id = c.course_category_id
                    INNER JOIN
                        users as u ON u.id = c.instructor_id
                    LEFT JOIN
                        course_tag as ct ON ct.course_id = c.id
                    LEFT JOIN
                        tag as t ON t.id = ct.tag_id
                    LEFT JOIN
                        (
                            SELECT
                                cr.course_id,
                                COUNT(cr.course_id) AS total_review,
                                AVG(cr.rating) AS max_rating
                            FROM
                                course_review AS cr
                            GROUP BY
                                cr.course_id
                        ) AS mcr ON c.id = mcr.course_id
                    WHERE
                        c.id in (:courseIds)
                ) as rc
            LEFT JOIN
                enrollment as e ON e.course_id = rc.id AND e.student_id = :userId
            LEFT JOIN
                favourite_course as fc ON fc.course_id = rc.id AND fc.created_by = :userId
            LEFT JOIN
                user_profile as up ON up.created_by = rc.instructor_id
            LEFT JOIN
                (
                    SELECT
                        s.course_id,
                        count(t.id) as total_topics
                    FROM
                        topic as t
                    INNER JOIN
                        section as s ON s.id = t.section_id AND s.course_id IN (:courseIds)
                    GROUP BY
                        s.course_id
                ) as total_topics ON total_topics.course_id = rc.id;
            """, nativeQuery = true)
    Page<Tuple> findAllRelatedCoursesByIdsAndUser(List<Long> courseIds, Long userId, Pageable pageSize);

    // New changed for course status
    @Query(value = """
            SELECT DISTINCT
                c.id AS course_id,
                c.title AS course_title,
                cu.url AS course_url,
                c.description AS course_description,
                c.about,
                c.thumbnail AS course_thumbnail,
                c.preview_videourl as video_url,
                c.preview_video_vtt_content,
                c.prerequisite,
                c.course_outcome,
                c.last_mod_date,
                cl.name as level,
                us.id as user_id,
                us.full_name,
                up.profile_picture,
                up.about_me,
                up.headline,
                c.course_duration_in_hours,
                COALESCE(mcr.max_rating, 0) AS max_rating,
                COALESCE(mcr.total_review, 0) AS total_reviews,
                cc.name as category,
                CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
                CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
                c.certificate_enabled as has_certificate, c.meta_title ,c.meta_description, c.meta_heading 
            FROM
                course AS c
            LEFT JOIN
                (SELECT
                    cr.course_id,
                    COUNT(cr.course_id) AS total_review,
                    AVG(cr.rating) AS max_rating
                FROM
                    course_review AS cr
                GROUP BY
                    cr.course_id) AS mcr ON c.id = mcr.course_id
            INNER JOIN 
                course_url as cu ON cu.course_id = c.id        
            INNER JOIN
                course_category as cc ON c.course_category_id = cc.id
            INNER JOIN
                users as us ON us.id = c.created_by
            INNER JOIN
                course_level as cl ON c.course_level = cl.id
            LEFT JOIN
                user_profile as up ON up.created_by = us.id
            LEFT JOIN
                enrollment as e ON e.course_id = c.id AND e.student_id = :userId
            LEFT JOIN
                favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId
            WHERE
                ((:reviewTo is null) OR (mcr.max_rating is null OR mcr.max_rating BETWEEN :reviewFrom AND :reviewTo))
                AND ((:searchValue is null AND c.title is not null) OR c.title ilike :searchValue)
                AND c.course_status = 'PUBLISHED'
                AND cu.status = 'ACTIVE'
                AND (:courseIdsCheck = TRUE OR c.id NOT IN (:courseIds))
            ORDER BY
                max_rating,
                course_id DESC;
            """, countQuery = """
            SELECT DISTINCT
                c.id AS course_id,
                c.title AS course_title,
                cu.url AS course_url,
                c.description AS course_description,
                c.about,
                c.thumbnail AS course_thumbnail,
                c.preview_videourl as video_url,
                c.preview_video_vtt_content,
                c.prerequisite,
                c.course_outcome,
                c.last_mod_date,
                cl.name as level,
                us.id as user_id,
                us.full_name,
                up.profile_picture,
                up.about_me,
                up.headline,
                c.course_duration_in_hours,
                COALESCE(mcr.max_rating, 0) AS max_rating,
                COALESCE(mcr.total_review, 0) AS total_reviews,
                cc.name as category,
                CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
                CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
                c.certificate_enabled as has_certificate, c.meta_title ,c.meta_description, c.meta_heading 
            FROM
                course AS c
            LEFT JOIN
                (SELECT
                    cr.course_id,
                    COUNT(cr.course_id) AS total_review,
                    AVG(cr.rating) AS max_rating
                FROM
                    course_review AS cr
                GROUP BY
                    cr.course_id) AS mcr ON c.id = mcr.course_id
            INNER JOIN 
                course_url as cu ON cu.course_id = c.id    
            INNER JOIN
                course_category as cc ON c.course_category_id = cc.id
            INNER JOIN
                users as us ON us.id = c.created_by
            INNER JOIN
                course_level as cl ON c.course_level = cl.id
            LEFT JOIN
                user_profile as up ON up.created_by = us.id
            LEFT JOIN
                enrollment as e ON e.course_id = c.id AND e.student_id = :userId
            LEFT JOIN
                favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId
            WHERE
                ((:reviewTo is null) OR (mcr.max_rating is null OR mcr.max_rating BETWEEN :reviewFrom AND :reviewTo))
                AND ((:searchValue is null AND c.title is not null) OR c.title ilike :searchValue)
                AND c.course_status = 'PUBLISHED'
                AND cu.status = 'ACTIVE'
                AND (:courseIdsCheck = TRUE OR c.id NOT IN (:courseIds))
            ORDER BY
                max_rating,
                course_id DESC;
            """, nativeQuery = true)
    Page<Tuple> findAllBySearchFilter(String searchValue, Double reviewFrom, Double reviewTo, Long userId, Boolean courseIdsCheck, List<Long> courseIds, PageRequest of);

    @Query(value = """
                WITH course_durations AS (
                    SELECT
                        s.course_id,
                        SUM(t.duration_in_sec) AS total_duration_in_sec
                    FROM
                        section AS s
                    INNER JOIN
                        topic AS t ON t.section_id = s.id
                    GROUP BY
                        s.course_id
                )
                SELECT
                    c.id,
                    c.title,
                    c.created_date,
                    COALESCE(cd.total_duration_in_sec, 0) AS total_duration_in_sec,
                    c.course_duration_in_hours,
                    c.last_mod_date,
                    c.thumbnail,
                    c.course_status,
                    c.course_progress,
                    COUNT(DISTINCT CASE WHEN e.student_id <> c.created_by THEN e.student_id END) AS no_of_students,
                    c.course_type,
                    c.content_type,
                    c.price,
                    COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                FROM
                    course AS c
                LEFT JOIN
                    enrollment AS e ON e.course_id = c.id
                LEFT JOIN
                    course_durations AS cd ON cd.course_id = c.id
                LEFT JOIN
                    section AS s ON s.course_id = c.id
                LEFT JOIN
                    topic AS t ON t.section_id = s.id
                LEFT JOIN
                    quiz AS q ON q.topic_id = t.id
                    
                LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
                 ) AS q_summary ON q_summary.topic_id = t.id    
                    
                    
                WHERE
                    (:searchInput IS NULL OR c.title ILIKE '%' || :searchInput || '%')
                AND
                    c.created_by = :userId
                AND
                     (c.course_status = 'PUBLISHED' OR c.course_status = 'UNPUBLISHED' OR (c.course_status = 'DRAFT' AND c.course_progress <> '100'))
                GROUP BY
                    c.id, cd.total_duration_in_sec
                ORDER BY
                    CASE WHEN :sortedBy = 0 THEN c.created_date END ASC,
                    CASE WHEN :sortedBy = 1 THEN c.created_date END DESC;
            """, countQuery = """
                        WITH course_durations AS (
                            SELECT
                                s.course_id,
                                SUM(t.duration_in_sec) AS total_duration_in_sec
                            FROM
                                section AS s
                            INNER JOIN
                                topic AS t ON t.section_id = s.id
                            GROUP BY
                                s.course_id
                        )
                        SELECT
                            c.id,
                            c.title,
                            c.created_date,
                            COALESCE(cd.total_duration_in_sec, 0) AS total_duration_in_sec,
                            c.course_duration_in_hours,
                            c.last_mod_date,
                            c.thumbnail,
                            c.course_status,
                            c.course_progress,
                            COUNT(DISTINCT CASE WHEN e.student_id <> c.created_by THEN e.student_id END) AS no_of_students,
                            c.course_type,
                            c.content_type,
                            c.price,
                            COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                        FROM
                            course AS c
                        LEFT JOIN
                            enrollment AS e ON e.course_id = c.id
                        LEFT JOIN
                            course_durations AS cd ON cd.course_id = c.id
                        LEFT JOIN
                            section AS s ON s.course_id = c.id
                        LEFT JOIN
                            topic AS t ON t.section_id = s.id
                        LEFT JOIN
                            quiz AS q ON q.topic_id = t.id
                        
                        LEFT JOIN (
                            SELECT topic_id, SUM(random_question) AS total_random_questions
                            FROM quiz
                            GROUP BY topic_id
                        ) AS q_summary ON q_summary.topic_id = t.id    
                            
                        WHERE
                            c.created_by = :userId
                        AND
                            (c.course_status = 'PUBLISHED' OR c.course_status = 'UNPUBLISHED' OR (c.course_status = 'DRAFT' AND c.course_progress <> '100'))
                        AND
                            (:searchInput IS NULL OR c.title ILIKE '%' || :searchInput || '%')
                        GROUP BY
                            c.id, cd.total_duration_in_sec
                        ORDER BY
                            CASE WHEN :sortedBy = 0 THEN c.created_date END ASC,
                            CASE WHEN :sortedBy = 1 THEN c.created_date END DESC;
            """, nativeQuery = true)
    Page<Tuple> findAllCoursesWithFilter(Long userId, Integer sortedBy, String searchInput, Pageable of);

    boolean existsByThumbnailOrPreviewVideoURL(String thumbnail, String previewVideoURL);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM course as c 
                INNER JOIN users as u ON u.id = c.created_by 
                WHERE u.email = :email) THEN true ELSE false END
            """, nativeQuery = true)
    boolean existsByUserEmail(String email);

    @Query(value = """
            SELECT
                c.id,
                c.title,
                c.thumbnail,
                COALESCE(COUNT(DISTINCT s.id), 0) as sections,
                COALESCE(COUNT(DISTINCT t.id), 0) as topics,
                COALESCE(SUM(t.duration_in_sec), 0.0) as course_duration,
                c.created_by as instructor_id, u.full_name as instructor_name,
            	up.profile_picture as instructor_image
            FROM
                course as c
            LEFT JOIN
                users as u ON u.id = c.created_by
            LEFT JOIN
            	user_profile as up ON up.created_by = u.id
            LEFT JOIN
                section as s ON s.course_id = c.id
            LEFT JOIN
                topic as t ON t.section_id = s.id
            WHERE
                c.id = :courseId
            GROUP BY
                c.id,
                c.title,
                c.course_duration_in_hours,
                c.created_by,
            	u.full_name,
            	up.profile_picture
            """, nativeQuery = true)
    Tuple fetchCourseDetailForCertificate(Long courseId);

    Course findByDocumentVector(String docVector);

    @Query("SELECT c FROM Course c WHERE LOWER(c.title) = LOWER(:courseTitle) AND c.courseStatus = :courseStatus")
    Course findByTitleAndPublished(@Param("courseTitle") String courseTitle, @Param("courseStatus") String courseStatus);

    @Query("SELECT c FROM Course c WHERE LOWER(c.title) = LOWER(:courseTitle)")
    List<Course> findByTitle(@Param("courseTitle") String courseTitle);

    @Query(value = """
            SELECT id, title FROM course WHERE instructor_id = :instructorId and course_status = 'PUBLISHED'
            """, nativeQuery = true)
    List<Tuple> findAllByInstructorId(Long instructorId);

//    Boolean existsByCourseUrl(String courseUrl);
//
//    @Query("SELECT c FROM Course c WHERE LOWER(c.courseUrl) = LOWER(:courseUrl) AND c.courseStatus = :courseStatus")
//    Course findByCourseUrlAndPublished(@Param("courseUrl") String courseUrl, @Param("courseStatus") CourseStatus courseStatus);

    @Query("SELECT c FROM Course c WHERE c.id = :courseId AND c.courseStatus = :courseStatus")
    Course findByCourseIdAndPublished(@Param("courseId") Long courseId, @Param("courseStatus") CourseStatus courseStatus);

    @Query(value = "SELECT c.id, c.title, t.name AS tag_name" + "FROM course c" + "JOIN Tag t ON ct.tag.id = t.id " + "JOIN course_tag ct ON c.id = ct.course_id" + "JOIN tag t ON ct.tag_id = t.id;" + "WHERE c.course_status = 'PUBLISHED'", nativeQuery = true)
    List<Course> findRelatedCourses(@Param("title") String title, @Param("tags") List<Long> tags, @Param("courseStatus") CourseStatus courseStatus);

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
            SELECT DISTINCT
                c.id AS course_id,
                c.title AS course_name,
                cu.url AS course_url,
                c.description,
                c.thumbnail,
                 CASE
                                    WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id =:userId THEN 'TRUE'
                                    ELSE 'FALSE'
                                END AS already_bought,
                cc.name AS course_category,
                u.id AS instructor_id,
                u.full_name AS instructor_name,
                up.profile_picture,
                COUNT(DISTINCT sr.id) AS total_reviews,
                COALESCE(AVG(sr.rating), 0) AS avg_course_rating,
                COUNT(DISTINCT t.id) AS total_topics,
                ta.tag_names AS tag_names,
                up.profile_url AS profile_url,
                c.course_type,
                c.price,cl.name AS course_level,
                c.content_type,
                COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                 (SELECT SUM(t1.duration_in_sec) FROM topic as t1 INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id WHERE c1.id = c.id) as duration
            FROM course c
            INNER JOIN course_category cc ON c.course_category_id = cc.id
            INNER JOIN users u ON u.id = c.instructor_id
            INNER JOIN section s ON s.course_id = c.id
            INNER JOIN topic t ON t.section_id = s.id
            INNER JOIN course_url cu ON cu.course_id = c.id
            LEFT JOIN user_profile up ON up.created_by = u.id
            LEFT JOIN enrollment AS e ON e.course_id = c.id
            INNER JOIN course_level AS cl ON c.course_level = cl.id
            LEFT JOIN course_review sr ON sr.course_id = c.id
            LEFT JOIN TagAggregates ta ON ta.course_id = c.id
            INNER JOIN quiz as q ON q.topic_id = t.id
            
            LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
            
            WHERE c.id != :courseId
            AND c.course_status = 'PUBLISHED'
            AND cu.status = 'ACTIVE'

            GROUP BY
                c.id, c.title, cu.url, up.profile_url, cc.name, u.id, u.full_name, up.profile_picture, ta.tag_names,cc.description,e.student_id,cl.name,c.content_type
            HAVING
                SIMILARITY(CAST((c.title || ' ' || COALESCE(ta.tag_names, '') || ' ' || cc.description) AS TEXT), :courseDetail) > 0.5
            """, nativeQuery = true, countQuery = """
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
            SELECT DISTINCT
                c.id AS course_id,
                c.title AS course_name,
                cu.url AS course_url,
                c.description,
                c.thumbnail,
                 CASE
                                    WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id =:userId THEN 'TRUE'
                                    ELSE 'FALSE'
                                END AS already_bought,
                cc.name AS course_category,
                u.id AS instructor_id,
                u.full_name AS instructor_name,
                up.profile_picture,
                COUNT(DISTINCT sr.id) AS total_reviews,
                COALESCE(AVG(sr.rating), 0) AS avg_course_rating,
                COUNT(DISTINCT t.id) AS total_topics,
                ta.tag_names AS tag_names,
                up.profile_url AS profile_url,
                c.course_type,
                c.price,cl.name AS course_level,
                c.content_type,
                COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                 (SELECT SUM(t1.duration_in_sec) FROM topic as t1 INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id WHERE c1.id = c.id) as duration
            FROM course c
            INNER JOIN course_category cc ON c.course_category_id = cc.id
            INNER JOIN users u ON u.id = c.instructor_id
            INNER JOIN section s ON s.course_id = c.id
            INNER JOIN topic t ON t.section_id = s.id
            INNER JOIN course_url cu ON cu.course_id = c.id
            LEFT JOIN user_profile up ON up.created_by = u.id
            LEFT JOIN enrollment AS e ON e.course_id = c.id
            INNER JOIN course_level AS cl ON c.course_level = cl.id
            LEFT JOIN course_review sr ON sr.course_id = c.id
            LEFT JOIN TagAggregates ta ON ta.course_id = c.id
            INNER JOIN quiz as q ON q.topic_id = t.id
            
            LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id            
            
            WHERE c.id != :courseId
            AND c.course_status = 'PUBLISHED'
            AND cu.status = 'ACTIVE'

            GROUP BY
                c.id, c.title, cu.url, up.profile_url, cc.name, u.id, u.full_name, up.profile_picture, ta.tag_names,cc.description,e.student_id,cl.name,c.content_type
            HAVING
                SIMILARITY(CAST((c.title || ' ' || COALESCE(ta.tag_names, '') || ' ' || cc.description) AS TEXT), :courseDetail) > 0.5
                    """)
    Page<Tuple> findRelatedCourses(@Param("courseId") Long courseId,
                                   @Param("courseDetail") String courseDetail,
                                   @Param("userId") Long userId,
                                   Pageable pageable);

    @Query(value = """
            SELECT DISTINCT
                                    c.id AS course_id,
                                    c.title,
                                    c.created_date,
                                    c.description,
            						c.price,
                                    c.content_type,
            						CASE WHEN :userId IS NULL THEN 'FALSE' WHEN c.course_type = 'PREMIUM_COURSE'
                                    AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                                    THEN 'TRUE' ELSE 'FALSE'
                                    END AS already_bought,
                                    c.course_type,
                                    c.thumbnail AS course_thumbnail,
                                    (SELECT SUM(t1.duration_in_sec) FROM topic as t1
                                    INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id
                                    WHERE c1.id = c.id) as duration,
                                    cu.url AS course_url,
                                    cl.name AS course_level,
                                    cc.name AS category_name,
                                    COALESCE(mcr.max_rating, 0) AS max_rating,
                                    COALESCE(mcr.total_review, 0) AS total_reviews,
                                    us.id AS user_id,
                                    us.full_name,
                                    up.profile_picture,
                                    up.about_me,
                                    up.headline,
                                    up.profile_url AS profile_url,
                                    COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                                FROM course c
                                LEFT JOIN (
                                    SELECT
                                        cr.course_id,
                                        COUNT(cr.course_id) AS total_review,
                                        AVG(cr.rating) AS max_rating
                                    FROM course_review AS cr
                                    GROUP BY cr.course_id
                                ) AS mcr ON c.id = mcr.course_id
                                INNER JOIN course_level cl ON c.course_level = cl.id
                                INNER JOIN course_category cc ON c.course_category_id = cc.id
                                INNER JOIN users as us ON us.id = c.created_by
                                LEFT JOIN user_profile as up ON up.created_by = us.id
            					LEFT JOIN enrollment AS e ON e.course_id = c.id
                                LEFT JOIN section s ON s.course_id = c.id
                                 INNER JOIN topic as t ON t.section_id = s.id
                                INNER JOIN quiz as q ON q.topic_id = t.id
                                
                                LEFT JOIN (
                                    SELECT topic_id, SUM(random_question) AS total_random_questions
                                    FROM quiz
                                    GROUP BY topic_id
                                ) AS q_summary ON q_summary.topic_id = t.id
                                
                                INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                                WHERE c.course_status = 'PUBLISHED'
                                GROUP BY c.id, c.title, c.created_date, c.description, c.thumbnail, duration,
                                         cl.name, cc.name, mcr.max_rating, mcr.total_review, us.id, us.full_name, up.profile_picture,
                                         up.about_me, up.headline,up.profile_url,cu.url,e.student_id,c.course_type
                                ORDER BY c.created_date DESC;
                """, nativeQuery = true, countQuery = """
            SELECT DISTINCT
                                    c.id AS course_id,
                                    c.title,
                                    c.created_date,
                                    c.description,
            						c.price,
                                    c.content_type,
            						CASE WHEN :userId IS NULL THEN 'FALSE' 
                                    WHEN c.course_type = 'PREMIUM_COURSE'
                                    AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                                    THEN 'TRUE' ELSE 'FALSE' END AS already_bought,
                                    c.course_type,
                                    c.thumbnail AS course_thumbnail,
                                    (SELECT SUM(t1.duration_in_sec) FROM topic as t1
                                    INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id
                                    WHERE c1.id = c.id) as duration,
                                    cu.url AS course_url,
                                    cl.name AS course_level,
                                    cc.name AS category_name,
                                    COALESCE(mcr.max_rating, 0) AS max_rating,
                                    COALESCE(mcr.total_review, 0) AS total_reviews,
                                    us.id AS user_id,
                                    us.full_name,
                                    up.profile_picture,
                                    up.about_me,
                                    up.headline,
                                    up.profile_url AS profile_url,
                                    COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                                FROM course c
                                LEFT JOIN (
                                    SELECT
                                        cr.course_id,
                                        COUNT(cr.course_id) AS total_review,
                                        AVG(cr.rating) AS max_rating
                                    FROM course_review AS cr
                                    GROUP BY cr.course_id
                                ) AS mcr ON c.id = mcr.course_id
                                INNER JOIN course_level cl ON c.course_level = cl.id
                                INNER JOIN course_category cc ON c.course_category_id = cc.id
                                INNER JOIN users as us ON us.id = c.created_by
                                LEFT JOIN user_profile as up ON up.created_by = us.id
            					LEFT JOIN enrollment AS e ON e.course_id = c.id
                                LEFT JOIN section s ON s.course_id = c.id
                                INNER JOIN topic as t ON t.section_id = s.id 
                                INNER JOIN quiz as q ON q.topic_id = t.id
                                
            LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id                                
                                
                                INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                                WHERE c.course_status = 'PUBLISHED'
                                GROUP BY c.id, c.title, c.created_date, c.description, c.thumbnail, duration,
                                         cl.name, cc.name, mcr.max_rating, mcr.total_review, us.id, us.full_name, up.profile_picture,
                                         up.about_me, up.headline,up.profile_url,cu.url,e.student_id,c.course_type
                                ORDER BY c.created_date DESC;
            """)
    Page<Tuple> findByCreationDateDesc(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
SELECT DISTINCT
                        c.id AS course_id,
                        c.title,
                        c.created_date,
                        c.description,
                        c.thumbnail AS course_thumbnail,
                        c.content_type,
                        (SELECT SUM(t1.duration_in_sec) FROM topic as t1
                        INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id
                        WHERE c1.id = c.id) as duration,
                        cu.url AS course_url,
                        cl.name AS course_level,
                        cc.name AS category_name,
                        COALESCE(mcr.max_rating, 0) AS max_rating,
                        COALESCE(mcr.total_review, 0) AS total_reviews,
                        us.id AS user_id,
                        us.full_name,
                        up.profile_picture,
                        up.about_me,
                        up.headline,
                        up.profile_url AS profile_url,
                        c.course_type,
                        c.price,
                        COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                    FROM course AS c
                    LEFT JOIN (
                        SELECT
                            cr.course_id,
                            COUNT(cr.course_id) AS total_review,
                            AVG(cr.rating) AS max_rating
                        FROM course_review AS cr
                        GROUP BY cr.course_id
                    ) AS mcr ON c.id = mcr.course_id
                    INNER JOIN course_level AS cl ON c.course_level = cl.id
                    INNER JOIN course_category AS cc ON c.course_category_id = cc.id
                    INNER JOIN users AS us ON us.id = c.created_by
                    LEFT JOIN user_profile AS up ON up.created_by = us.id
                    LEFT JOIN section AS s ON s.course_id = c.id
                    INNER JOIN topic as t ON t.section_id = s.id
                    INNER JOIN quiz as q ON q.topic_id = t.id
                    
                                LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
                    
                    INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                    WHERE c.course_status = 'PUBLISHED' AND c.course_type = 'FREE_COURSE'
                    GROUP BY c.id, c.title, c.created_date, c.description, c.thumbnail, duration,
                             cl.name, cc.name, mcr.max_rating, mcr.total_review, us.id, us.full_name, up.profile_picture,
                             up.about_me, up.headline, up.profile_url,cu.url
                    ORDER BY max_rating DESC
                """, nativeQuery = true,
            countQuery = """
SELECT DISTINCT
                        c.id AS course_id,
                        c.title,
                        c.created_date,
                        c.description,
                        c.thumbnail AS course_thumbnail,
                        c.content_type,
                        (SELECT SUM(t1.duration_in_sec) FROM topic as t1
                        INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id
                        WHERE c1.id = c.id) as duration,
                        cu.url AS course_url,
                        cl.name AS course_level,
                        cc.name AS category_name,
                        COALESCE(mcr.max_rating, 0) AS max_rating,
                        COALESCE(mcr.total_review, 0) AS total_reviews,
                        us.id AS user_id,
                        us.full_name,
                        up.profile_picture,
                        up.about_me,
                        up.headline,
                        up.profile_url AS profile_url,
                        c.course_type,
                        c.price,
                        COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
                    FROM course AS c
                    LEFT JOIN (
                        SELECT
                            cr.course_id,
                            COUNT(cr.course_id) AS total_review,
                            AVG(cr.rating) AS max_rating
                        FROM course_review AS cr
                        GROUP BY cr.course_id
                    ) AS mcr ON c.id = mcr.course_id
                    INNER JOIN course_level AS cl ON c.course_level = cl.id
                    INNER JOIN course_category AS cc ON c.course_category_id = cc.id
                    INNER JOIN users AS us ON us.id = c.created_by
                    LEFT JOIN user_profile AS up ON up.created_by = us.id
                    LEFT JOIN section AS s ON s.course_id = c.id
                    INNER JOIN topic as t ON t.section_id = s.id
                    INNER JOIN quiz as q ON q.topic_id = t.id
                    
                                LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
                    
                    INNER JOIN course_url as cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                    WHERE c.course_status = 'PUBLISHED' AND c.course_type = 'FREE_COURSE'
                    GROUP BY c.id, c.title, c.created_date, c.description, c.thumbnail, duration,
                             cl.name, cc.name, mcr.max_rating, mcr.total_review, us.id, us.full_name, up.profile_picture,
                             up.about_me, up.headline, up.profile_url,cu.url
                    ORDER BY max_rating DESC    
                            """)
    Page<Tuple> findAllFreeCourses(Pageable pageable);

    @Query(value = """
            SELECT
                        COUNT(DISTINCT e.student_id) AS total_students,
                        us.full_name,
                        c.id AS course_id,
                        c.title,
                        c.created_date,
                        c.description,
                        c.thumbnail AS course_thumbnail,
                        c.content_type,                        
                        (SELECT SUM(t1.duration_in_sec)
                         FROM topic AS t1
                                  INNER JOIN section AS s1 ON s1.id = t1.section_id
                                  INNER JOIN course AS c1 ON c1.id = s1.course_id
                         WHERE c1.id = c.id) AS duration,
                        cu.url AS course_url,
                        cl.name AS course_level,
                        cc.name AS category_name,
                        COALESCE(mcr.max_rating, 0) AS max_rating,
                        COALESCE(mcr.total_review, 0) AS total_reviews,
                        us.id AS user_id,
                        up.profile_picture,
                        up.about_me,
                        up.headline,
                        up.profile_url,
                        c.course_type,
                        c.price,
                        COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                        CASE
                            WHEN :studentId IS NULL THEN 'FALSE' 
                            WHEN c.course_type = 'PREMIUM_COURSE'
                                AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :studentId)
                                THEN 'TRUE'
                            ELSE 'FALSE'
                            END AS already_bought
                    FROM course AS c
                             LEFT JOIN enrollment AS e ON e.course_id = c.id
                             LEFT JOIN (SELECT cr.course_id,
                                               COUNT(cr.course_id) AS total_review,
                                               AVG(cr.rating) AS max_rating
                                        FROM course_review AS cr
                                        GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
                             INNER JOIN course_level AS cl ON c.course_level = cl.id
                             INNER JOIN course_category AS cc ON c.course_category_id = cc.id
                             INNER JOIN users AS us ON us.id = c.created_by
                             LEFT JOIN user_profile AS up ON up.created_by = us.id
                             LEFT JOIN section AS s ON s.course_id = c.id
                             INNER JOIN topic as t ON t.section_id = s.id
                             INNER JOIN quiz as q ON q.topic_id = t.id
                             
                                         LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
                             
                             INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                    WHERE c.course_status = 'PUBLISHED'
                    GROUP BY c.id,
                             c.title,
                             c.created_date,
                             c.description,
                             c.thumbnail,
                             cl.name,
                             cc.name,
                             us.full_name,
                             us.id,
                             up.profile_picture,
                             up.about_me,
                             up.headline,
                             up.profile_url,
                             mcr.max_rating,
                             mcr.total_review,
                             cu.url
                    ORDER BY total_students DESC; 
                """, nativeQuery = true,
            countQuery = """
                    SELECT
                        COUNT(DISTINCT e.student_id) AS total_students,
                        us.full_name,
                        c.id AS course_id,
                        c.title,
                        c.created_date,
                        c.description,
                        c.thumbnail AS course_thumbnail,
                        c.content_type,
                        (SELECT SUM(t1.duration_in_sec)
                         FROM topic AS t1
                                  INNER JOIN section AS s1 ON s1.id = t1.section_id
                                  INNER JOIN course AS c1 ON c1.id = s1.course_id
                         WHERE c1.id = c.id) AS duration,
                        cu.url AS course_url,
                        cl.name AS course_level,
                        cc.name AS category_name,
                        COALESCE(mcr.max_rating, 0) AS max_rating,
                        COALESCE(mcr.total_review, 0) AS total_reviews,
                        us.id AS user_id,
                        up.profile_picture,
                        up.about_me,
                        up.headline,
                        up.profile_url,
                        c.course_type,
                        c.price,
                        COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                        CASE
                            WHEN :studentId IS NULL THEN 'FALSE' 
                            WHEN c.course_type = 'PREMIUM_COURSE'
                                AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :studentId)
                                THEN 'TRUE'
                            ELSE 'FALSE'
                            END AS already_bought
                    FROM course AS c
                             LEFT JOIN enrollment AS e ON e.course_id = c.id
                             LEFT JOIN (SELECT cr.course_id,
                                               COUNT(cr.course_id) AS total_review,
                                               AVG(cr.rating) AS max_rating
                                        FROM course_review AS cr
                                        GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
                             INNER JOIN course_level AS cl ON c.course_level = cl.id
                             INNER JOIN course_category AS cc ON c.course_category_id = cc.id
                             INNER JOIN users AS us ON us.id = c.created_by
                             LEFT JOIN user_profile AS up ON up.created_by = us.id
                             LEFT JOIN section AS s ON s.course_id = c.id
                             INNER JOIN topic as t ON t.section_id = s.id
                             INNER JOIN quiz as q ON q.topic_id = t.id
                             
                                         LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
                             
                             INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                    WHERE c.course_status = 'PUBLISHED'
                    GROUP BY c.id,
                             c.title,
                             c.created_date,
                             c.description,
                             c.thumbnail,
                             cl.name,
                             cc.name,
                             us.full_name,
                             us.id,
                             up.profile_picture,
                             up.about_me,
                             up.headline,
                             up.profile_url,
                             mcr.max_rating,
                             mcr.total_review,
                             cu.url
                    ORDER BY total_students DESC; 
                            """)
    Page<Tuple> findAllTrendingCourses(@Param("studentId") Long studentId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT
                 c.id AS course_id,
                 c.title,
                 c.created_date,
                 c.description,
                 c.price,
                 c.thumbnail AS course_thumbnail,
                  CASE WHEN :userId IS NULL THEN 'FALSE'
                         WHEN c.course_type = 'PREMIUM_COURSE'
                         AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                         THEN 'TRUE' ELSE 'FALSE' END AS already_bought,
                 c.course_type,
                 c.content_type,
                 (SELECT SUM(t1.duration_in_sec)
                  FROM topic AS t1
                  INNER JOIN section AS s1 ON s1.id = t1.section_id
                  INNER JOIN course AS c1 ON c1.id = s1.course_id
                  WHERE c1.id = c.id) AS duration,
                 cu.url AS course_url,
                 cl.name AS course_level,
                 cc.name AS category_name,
                 COALESCE(mcr.max_rating, 0) AS max_rating,
                 COALESCE(mcr.total_review, 0) AS total_reviews,
                 us.id AS user_id,
                 us.full_name,
                 up.profile_picture,
                 up.about_me,
                 up.headline,
                 up.profile_url AS profile_url,
                 COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
             FROM course AS c
             LEFT JOIN (
                 SELECT
                     cr.course_id,
                     COUNT(cr.course_id) AS total_review,
                     AVG(cr.rating) AS max_rating
                 FROM course_review AS cr
                 GROUP BY cr.course_id
             ) AS mcr ON c.id = mcr.course_id
             INNER JOIN course_level AS cl ON c.course_level = cl.id
             INNER JOIN course_category AS cc ON c.course_category_id = cc.id
             INNER JOIN users AS us ON us.id = c.created_by
             LEFT JOIN user_profile AS up ON up.created_by = us.id
             LEFT JOIN enrollment AS e ON e.course_id = c.id
             LEFT JOIN section AS s ON s.course_id = c.id
             INNER JOIN topic as t ON t.section_id = s.id
             LEFT JOIN quiz as q ON q.topic_id = t.id
             
                         LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
             
             INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
             WHERE c.course_status = 'PUBLISHED' AND c.course_type = 'PREMIUM_COURSE'
             GROUP BY c.id, c.title, c.created_date, c.description, c.thumbnail, cl.name, cc.name,
                      mcr.max_rating, mcr.total_review, us.id, us.full_name, up.profile_picture,
                      up.about_me, up.headline, up.profile_url, cu.url, e.course_id, e.student_id
             ORDER BY max_rating DESC
                """, nativeQuery = true, countQuery = """
            SELECT DISTINCT
                  c.id AS course_id,
                  c.title,
                  c.created_date,
                  c.description,
                  c.price,
                  c.thumbnail AS course_thumbnail,
                   CASE WHEN :userId IS NULL THEN 'FALSE'
                          WHEN c.course_type = 'PREMIUM_COURSE'
                          AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                          THEN 'TRUE' ELSE 'FALSE' END AS already_bought,
                  c.course_type,
                  c.content_type,
                  (SELECT SUM(t1.duration_in_sec)
                   FROM topic AS t1
                   INNER JOIN section AS s1 ON s1.id = t1.section_id
                   INNER JOIN course AS c1 ON c1.id = s1.course_id
                   WHERE c1.id = c.id) AS duration,
                  cu.url AS course_url,
                  cl.name AS course_level,
                  cc.name AS category_name,
                  COALESCE(mcr.max_rating, 0) AS max_rating,
                  COALESCE(mcr.total_review, 0) AS total_reviews,
                  us.id AS user_id,
                  us.full_name,
                  up.profile_picture,
                  up.about_me,
                  up.headline,
                  up.profile_url AS profile_url,
                  COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question
              FROM course AS c
              LEFT JOIN (
                  SELECT
                      cr.course_id,
                      COUNT(cr.course_id) AS total_review,
                      AVG(cr.rating) AS max_rating
                  FROM course_review AS cr
                  GROUP BY cr.course_id
              ) AS mcr ON c.id = mcr.course_id
              INNER JOIN course_level AS cl ON c.course_level = cl.id
              INNER JOIN course_category AS cc ON c.course_category_id = cc.id
              INNER JOIN users AS us ON us.id = c.created_by
              LEFT JOIN user_profile AS up ON up.created_by = us.id
              LEFT JOIN enrollment AS e ON e.course_id = c.id
              LEFT JOIN section AS s ON s.course_id = c.id
              INNER JOIN topic as t ON t.section_id = s.id
              LEFT JOIN quiz as q ON q.topic_id = t.id
              
                          LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id       
             
              INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
              WHERE c.course_status = 'PUBLISHED' AND c.course_type = 'PREMIUM_COURSE'
              GROUP BY c.id, c.title, c.created_date, c.description, c.thumbnail, cl.name, cc.name,
                       mcr.max_rating, mcr.total_review, us.id, us.full_name, up.profile_picture,
                       up.about_me, up.headline, up.profile_url, cu.url, e.course_id, e.student_id
              ORDER BY max_rating DESC""")
    Page<Tuple> findPremiumCourses(@Param("userId") Long userId, Pageable pageable);

    List<Course> findAllByCourseStatusIn(List<CourseStatus> courseStatuses);

    @Query(value = """
            SELECT
                COUNT(DISTINCT e.student_id) AS total_students,
                us.full_name,
                c.id AS course_id,
                c.title,
                c.created_date,
                c.description,
                c.thumbnail AS course_thumbnail,
                c.content_type,
                COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                (SELECT SUM(t1.duration_in_sec)
                 FROM topic AS t1
                      INNER JOIN section AS s1 ON s1.id = t1.section_id
                      INNER JOIN course AS c1 ON c1.id = s1.course_id
                 WHERE c1.id = c.id) AS duration,
                cu.url AS course_url,
                cl.name AS course_level,
                cc.name AS category_name,
                COALESCE(mcr.max_rating, 0) AS max_rating,
                COALESCE(mcr.total_review, 0) AS total_reviews,
                us.id AS user_id,
                up.profile_picture,
                up.about_me,
                up.headline,
                up.profile_url,
                c.course_type,
                c.price,
                CASE
                    WHEN :userId IS NULL THEN 'FALSE'
                    WHEN c.course_type = 'PREMIUM_COURSE'
                         AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                    THEN 'TRUE'
                    ELSE 'FALSE'
                END AS already_bought
            FROM course AS c
                 LEFT JOIN enrollment AS e ON e.course_id = c.id
                 LEFT JOIN (
                    SELECT
                        cr.course_id,
                        COUNT(cr.course_id) AS total_review,
                        COALESCE(AVG(cr.rating), 0) AS max_rating
                    FROM course_review AS cr
                    GROUP BY cr.course_id
                 ) AS mcr ON c.id = mcr.course_id
                 INNER JOIN course_level AS cl ON c.course_level = cl.id
                 INNER JOIN course_category AS cc ON c.course_category_id = cc.id
                 INNER JOIN users AS us ON us.id = c.created_by
                 LEFT JOIN user_profile AS up ON up.created_by = us.id
                 LEFT JOIN section AS s ON s.course_id = c.id
                 LEFT JOIN topic as t ON t.section_id = s.id
                 LEFT JOIN quiz as q ON q.topic_id = t.id
                 
                             LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
                 
                 INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
            WHERE c.course_status = 'PUBLISHED'
                  AND (c.course_type = :courseType OR :courseType IS NULL)
                  AND (:singleCat IS NULL OR cc.name IN (:multipleCategories))
                  AND (
                      (:minRating IS NULL AND :maxRating IS NULL) OR
                      (COALESCE(mcr.max_rating, 0) BETWEEN :minRating AND :maxRating)
                  )
                  AND (:contentType IS NULL OR c.content_type = :contentType)
                  AND (
                      :search IS NULL OR
                      LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                      LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            GROUP BY c.id,
                     c.title,
                     c.created_date,
                     c.description,
                     c.thumbnail,
                     cl.name,
                     cc.name,
                     us.full_name,
                     us.id,
                     up.profile_picture,
                     up.about_me,
                     up.headline,
                     up.profile_url,
                     mcr.max_rating,
                     mcr.total_review,
                     cu.url,
                     c.content_type
            ORDER BY
                CASE WHEN :feature = 'TRENDING_COURSE' THEN COUNT(DISTINCT e.student_id) END DESC,
                CASE WHEN :feature = 'NEW_COURSE' THEN c.created_date END DESC;
            """,
    countQuery = """
            SELECT
                COUNT(DISTINCT e.student_id) AS total_students,
                us.full_name,
                c.id AS course_id,
                c.title,
                c.created_date,
                c.description,
                c.thumbnail AS course_thumbnail,
                c.content_type,
                COALESCE(SUM(CASE WHEN c.content_type = 'TEST' THEN q_summary.total_random_questions ELSE 0 END), 0) AS test_total_question,
                (SELECT SUM(t1.duration_in_sec)
                 FROM topic AS t1
                      INNER JOIN section AS s1 ON s1.id = t1.section_id
                      INNER JOIN course AS c1 ON c1.id = s1.course_id
                 WHERE c1.id = c.id) AS duration,
                cu.url AS course_url,
                cl.name AS course_level,
                cc.name AS category_name,
                COALESCE(mcr.max_rating, 0) AS max_rating,
                COALESCE(mcr.total_review, 0) AS total_reviews,
                us.id AS user_id,
                up.profile_picture,
                up.about_me,
                up.headline,
                up.profile_url,
                c.course_type,
                c.price,
                CASE
                    WHEN :userId IS NULL THEN 'FALSE'
                    WHEN c.course_type = 'PREMIUM_COURSE'
                         AND EXISTS (SELECT 1 FROM enrollment e2 WHERE e2.course_id = c.id AND e2.student_id = :userId)
                    THEN 'TRUE'
                    ELSE 'FALSE'
                END AS already_bought
            FROM course AS c
                 LEFT JOIN enrollment AS e ON e.course_id = c.id
                 LEFT JOIN (
                    SELECT
                        cr.course_id,
                        COUNT(cr.course_id) AS total_review,
                        COALESCE(AVG(cr.rating), 0) AS max_rating
                    FROM course_review AS cr
                    GROUP BY cr.course_id
                 ) AS mcr ON c.id = mcr.course_id
                 INNER JOIN course_level AS cl ON c.course_level = cl.id
                 INNER JOIN course_category AS cc ON c.course_category_id = cc.id
                 INNER JOIN users AS us ON us.id = c.created_by
                 LEFT JOIN user_profile AS up ON up.created_by = us.id
                 LEFT JOIN section AS s ON s.course_id = c.id
                 LEFT JOIN topic as t ON t.section_id = s.id
                 LEFT JOIN quiz as q ON q.topic_id = t.id
                 
                 
                             LEFT JOIN (
                 SELECT topic_id, SUM(random_question) AS total_random_questions
                   FROM quiz
                   GROUP BY topic_id
            ) AS q_summary ON q_summary.topic_id = t.id
                 
                 INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
            WHERE c.course_status = 'PUBLISHED'
                  AND (c.course_type = :courseType OR :courseType IS NULL)
                  AND (:singleCat IS NULL OR cc.name IN (:multipleCategories))
                  AND (
                      (:minRating IS NULL AND :maxRating IS NULL) OR
                      (COALESCE(mcr.max_rating, 0) BETWEEN :minRating AND :maxRating)
                  )
                  AND (:contentType IS NULL OR c.content_type = :contentType)
                  AND (
                      :search IS NULL OR
                      LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                      LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            GROUP BY c.id,
                     c.title,
                     c.created_date,
                     c.description,
                     c.thumbnail,
                     cl.name,
                     cc.name,
                     us.full_name,
                     us.id,
                     up.profile_picture,
                     up.about_me,
                     up.headline,
                     up.profile_url,
                     mcr.max_rating,
                     mcr.total_review,
                     cu.url,
                     c.content_type
            ORDER BY
                CASE WHEN :feature = 'TRENDING_COURSE' THEN COUNT(DISTINCT e.student_id) END DESC,
                CASE WHEN :feature = 'NEW_COURSE' THEN c.created_date END DESC;
            """,nativeQuery = true)
    Page<Tuple> findViewAllBySearchFilter(@Param("multipleCategories") List<String> multipleCategories,
                                      @Param("singleCat") String singleCat,@Param("courseType") String courseType,
                                      @Param("search") String search,
                                      @Param("feature") String feature,@Param("minRating") double minRating,
                                      @Param("maxRating") double maxRating,@Param("userId")Long userId,
                                      @Param("contentType") String contentType,
                                      Pageable pageable);

    @Query(value = """
            SELECT
                c.id AS course_id,
                CASE
                    WHEN COUNT(t.id) = 0 THEN 0
                    ELSE
                        CASE
                            WHEN COUNT(ucp.topic_id) = 0 THEN 0
                            ELSE COUNT(ucp.topic_id) * 100.0 / COUNT(t.id)
                        END
                END AS completion_percentage,
                MAX(ucp.last_mod_date) AS last_mod_date
            FROM
                course c
            INNER JOIN
                section s ON s.course_id = c.id
            INNER JOIN
                topic t ON t.section_id = s.id
            LEFT JOIN
                user_course_progress ucp ON ucp.topic_id = t.id
                                          AND ucp.course_id = c.id
                                          AND ucp.student_id = :userId
                                          AND ucp.is_completed = true
            GROUP BY
                c.id
            HAVING
                CASE
                    WHEN COUNT(t.id) = 0 THEN 0
                    ELSE
                        CASE
                            WHEN COUNT(ucp.topic_id) = 0 THEN 0
                            ELSE COUNT(ucp.topic_id) * 100.0 / COUNT(t.id)
                        END
                END >= 99
            ORDER BY
                last_mod_date DESC;
            """,countQuery = """
              SELECT
                  c.id AS course_id,
                  CASE
                      WHEN COUNT(t.id) = 0 THEN 0
                      ELSE
                          CASE
                              WHEN COUNT(ucp.topic_id) = 0 THEN 0
                              ELSE COUNT(ucp.topic_id) * 100.0 / COUNT(t.id)
                          END
                  END AS completion_percentage,
                  MAX(ucp.last_mod_date) AS last_mod_date
              FROM
                  course c
              INNER JOIN
                  section s ON s.course_id = c.id
              INNER JOIN
                  topic t ON t.section_id = s.id
              LEFT JOIN
                  user_course_progress ucp ON ucp.topic_id = t.id
                                            AND ucp.course_id = c.id
                                            AND ucp.student_id = :userId
                                            AND ucp.is_completed = true
              GROUP BY
                  c.id
              HAVING
                  CASE
                      WHEN COUNT(t.id) = 0 THEN 0
                      ELSE
                          CASE
                              WHEN COUNT(ucp.topic_id) = 0 THEN 0
                              ELSE COUNT(ucp.topic_id) * 100.0 / COUNT(t.id)
                          END
                  END >= 99
              ORDER BY
                  last_mod_date DESC;
            """,nativeQuery = true)
    Page<Tuple> getAllCompletedCourseByUser(Long userId,Pageable pageable);


    @Query(value = "SELECT new com.vinncorp.fast_learner.response.course.CourseDetailByType(c.id, c.title," +
            "c.description, c.thumbnail, cu.url, COUNT(DISTINCT ac.id)) " +
            "FROM Course c " +
            "INNER JOIN CourseUrl cu ON cu.course.id = c.id " +
            "LEFT JOIN AffiliatedCourses ac ON ac.course.id = c.id " +
            "AND ac.status = 'ACTIVE' " +
            "AND ac.instructorAffiliate.id IN ( " +
            "   SELECT ia.id " +
            "   FROM InstructorAffiliate ia " +
            "   WHERE ia.instructor.id = :instructorId " +
            "   AND ia.status = 'ACTIVE' " +
            ") " +
            "WHERE c.createdBy = :instructorId " +
            "AND cu.status = 'ACTIVE' " +
            "AND c.courseType = :courseType AND c.courseStatus='PUBLISHED' " +
            "GROUP BY c.id, c.title, c.description, c.thumbnail, cu.url " +
            "ORDER BY c.creationDate DESC")
    List<CourseDetailByType> findByCreatedByAndCourseType(@Param("instructorId") Long instructorId,
                                                          @Param("courseType") CourseType courseType);

    @Query(value = "SELECT new com.vinncorp.fast_learner.response.course.CourseDetailByType(" +
            "c.id, c.title, c.description, c.thumbnail, cu.url, COUNT(DISTINCT ac.id)) " +
            "FROM Course c " +
            "INNER JOIN CourseUrl cu ON cu.course.id = c.id " +
            "LEFT JOIN AffiliatedCourses ac ON ac.course.id = c.id " +
            "AND ac.status = 'ACTIVE' " +
            "AND ac.instructorAffiliate.id IN ( " +
            "   SELECT ia.id " +
            "   FROM InstructorAffiliate ia " +
            "   WHERE ia.instructor.id = :instructorId " +
            "   AND ia.status = 'ACTIVE' " +
            ") " +
            "WHERE c.createdBy = :instructorId " +
            "AND cu.status = 'ACTIVE' " +
            "AND c.courseType = :courseType " +
            "AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND c.courseStatus = 'PUBLISHED' " +
            "GROUP BY c.id, c.title, c.description, c.thumbnail, cu.url " +
            "ORDER BY c.creationDate DESC")
    Page<CourseDetailByType> findByCreatedByAndCourseTypeAndSearch(
            @Param("search") String search,
            @Param("instructorId") Long instructorId,
            @Param("courseType") CourseType courseType,
            Pageable pageable);


    List<Course> findByInstructorAndCourseStatus(User instructor, CourseStatus courseStatus);

    @Query(value = """
            select count(*) from course where instructor_id=:instructorId and course_status=:courseStatus
            """,nativeQuery = true)
    Long findByInstructorAndPublished(@Param("instructorId") Long instructorId, @Param("courseStatus") String courseStatus);
}

