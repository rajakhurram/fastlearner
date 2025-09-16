package com.vinncorp.fast_learner.repositories.enrollment;

import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.enrollment.Enrollment;
import com.vinncorp.fast_learner.response.premium_student.PremiumStudentResponse;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudent_IdAndCourse_Id(Long id, Long id1);

    @Query(value = """
            SELECT  c.id AS course_id, c.title AS course_title, cu.url as course_url, c.description AS course_description, c.thumbnail AS course_thumbnail,c.certificate_enabled,
              us.id as user_id, us.full_name, up.profile_picture, up.profile_url, c.course_duration_in_hours,
              COALESCE(mcr.max_rating, 0) AS max_rating,
              COALESCE(mcr.total_review, 0) AS total_reviews,
              cc.name as category, CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
               (SELECT SUM(t1.duration_in_sec) FROM topic as t1 INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id WHERE c1.id = c.id) as duration,
              CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
            ucp.last_mod_date as last_mode_date, c.course_type, c.price,cl.name AS course_level,
            CASE WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE'
                                                                           ELSE 'FALSE'
                                                                        END AS already_bought,
            COUNT(CASE WHEN c.content_type = 'TEST' THEN qq.id END) AS test_total_question, c.content_type, no_of_topics
            FROM course AS c LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating
                  FROM course_review AS cr GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
              INNER JOIN course_category as cc ON c.course_category_id = cc.id
              INNER JOIN course_level AS cl ON c.course_level = cl.id
              INNER JOIN users as us ON us.id = c.created_by
              INNER JOIN enrollment as e ON e.course_id = c.id AND e.student_id = :userId
            INNER JOIN (
                    SELECT course_id, MAX(last_mod_date) AS last_mod_date
                    FROM user_course_progress
                    WHERE student_id = :userId
                    GROUP BY course_id
                ) AS ucp ON ucp.course_id = c.id
              LEFT JOIN user_profile as up ON up.created_by = us.id
              LEFT JOIN user_course_completion ucc ON ucc.course_id = c.id  AND ucc.user_id = :userId
              LEFT JOIN favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId
              LEFT JOIN (SELECT COUNT(DISTINCT tp.id) no_of_topics, c.id FROM course c INNER JOIN section s ON s.course_id = c.id AND s.is_active = true LEFT JOIN topic tp ON tp.section_id = s.id GROUP BY c.id ) topic ON topic.id=c.id
              INNER JOIN course_url as cu ON cu.course_id = c.id
              INNER JOIN section AS s ON s.course_id = c.id
              INNER JOIN topic as t ON t.section_id = s.id
              LEFT JOIN quiz as q ON q.topic_id = t.id
              LEFT JOIN quiz_question as qq ON qq.quiz_id = q.id
              WHERE (:title is null OR c.title ilike :title) AND c.course_status = 'PUBLISHED' AND cu.status = 'ACTIVE' AND ucc.course_id IS NULL
              GROUP BY\s
                  c.id, c.title, cu.url, c.description, c.thumbnail, us.id, us.full_name,\s
                  up.profile_picture, up.profile_url, c.course_duration_in_hours,\s
                  cc.name, e.id, fc.id, ucp.last_mod_date, c.course_type,\s
                  c.price, cl.name, mcr.max_rating, mcr.total_review, c.content_type, no_of_topics
                   ORDER BY CASE WHEN :sortDesc = true THEN ucp.last_mod_date END DESC,\s
                               CASE WHEN :sortDesc = false THEN ucp.last_mod_date END ASC
                  """,
            countQuery = """
                      SELECT  c.id AS course_id, c.title AS course_title, cu.url as course_url, c.description AS course_description, c.thumbnail AS course_thumbnail,c.certificate_enabled,
              us.id as user_id, us.full_name, up.profile_picture, up.profile_url, c.course_duration_in_hours,
              COALESCE(mcr.max_rating, 0) AS max_rating,
              COALESCE(mcr.total_review, 0) AS total_reviews,
              cc.name as category, CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END as is_enrolled,
               (SELECT SUM(t1.duration_in_sec) FROM topic as t1 INNER JOIN section as s1 ON s1.id = t1.section_id INNER JOIN course as c1 ON c1.id = s1.course_id WHERE c1.id = c.id) as duration,
              CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END as is_favourite,
            ucp.last_mod_date as last_mode_date, c.course_type, c.price,cl.name AS course_level,
            CASE WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE'
                                                                           ELSE 'FALSE'
                                                                       END AS already_bought,
            COUNT(CASE WHEN c.content_type = 'TEST' THEN qq.id END) AS test_total_question, c.content_type,  no_of_topics
            FROM course AS c LEFT JOIN (SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating
                  FROM course_review AS cr GROUP BY cr.course_id) AS mcr ON c.id = mcr.course_id
              INNER JOIN course_category as cc ON c.course_category_id = cc.id
              INNER JOIN course_level AS cl ON c.course_level = cl.id
              INNER JOIN users as us ON us.id = c.created_by
              INNER JOIN enrollment as e ON e.course_id = c.id AND e.student_id = :userId
            INNER JOIN (
                    SELECT course_id, MAX(last_mod_date) AS last_mod_date
                    FROM user_course_progress
                    WHERE student_id = :userId
                    GROUP BY course_id
                ) AS ucp ON ucp.course_id = c.id
              LEFT JOIN
                            (SELECT COUNT(DISTINCT tp.id) no_of_topics, c.id FROM course c INNER JOIN section s ON s.course_id = c.id AND s.is_active = true LEFT JOIN topic tp ON tp.section_id = s.id GROUP BY c.id ) topic ON topic.id=c.id
              LEFT JOIN user_profile as up ON up.created_by = us.id
              LEFT JOIN user_course_completion ucc ON ucc.course_id = c.id  AND ucc.user_id = :userId
              LEFT JOIN favourite_course as fc ON fc.course_id = c.id AND fc.created_by = :userId
              INNER JOIN course_url as cu ON cu.course_id = c.id
              INNER JOIN section AS s ON s.course_id = c.id
              INNER JOIN topic as t ON t.section_id = s.id
              LEFT JOIN quiz as q ON q.topic_id = t.id
              LEFT JOIN quiz_question as qq ON qq.quiz_id = q.id
              WHERE (:title is null OR c.title ilike :title) AND c.course_status = 'PUBLISHED' AND cu.status = 'ACTIVE' AND ucc.course_id IS NULL
              GROUP BY\s
                          c.id, c.title, cu.url, c.description, c.thumbnail, us.id, us.full_name,\s
                          up.profile_picture, up.profile_url, c.course_duration_in_hours,\s
                          cc.name, e.id, fc.id, ucp.last_mod_date, c.course_type,\s
                          c.price, cl.name, mcr.max_rating, mcr.total_review, c.content_type, no_of_topics
                          ORDER BY CASE WHEN :sortDesc = true THEN ucp.last_mod_date END DESC,\s
                                               CASE WHEN :sortDesc = false THEN ucp.last_mod_date END ASC
                          """,
            nativeQuery = true)
    Page<Tuple> findAllEnrolledInProgressCoursesByUserId(String title, Long userId, @Param("sortDesc") Boolean sortDesc, Pageable pageable);


    @Query(value = """
                    SELECT DISTINCT\s
                        c.id AS course_id,\s
                        c.content_type,\s
                        c.title AS course_title,\s
                        cu.url AS course_url,\s
                        c.description AS course_description,\s
                        c.thumbnail AS course_thumbnail,\s
                        c.certificate_enabled,\s
                        c.content_type,
                        us.id AS user_id,\s
                        us.full_name,\s
                        up.profile_picture,\s
                        up.profile_url,\s
                        c.course_duration_in_hours,\s
                        COALESCE(mcr.max_rating, 0) AS max_rating,\s
                        COALESCE(mcr.total_review, 0) AS total_reviews,\s
                        cc.name AS category,\s
                        CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END AS is_enrolled,\s
                        COUNT(CASE WHEN c.content_type = 'TEST' THEN qq.id END) AS test_total_question,
                        (SELECT SUM(t1.duration_in_sec)\s
                         FROM topic AS t1\s
                         INNER JOIN section AS s1 ON s1.id = t1.section_id\s
                         INNER JOIN course AS c1 ON c1.id = s1.course_id\s
                         WHERE c1.id = c.id) AS duration,
                        CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END AS is_favourite,\s
                        c.course_type,\s
                        c.price,\s
                        cl.name AS course_level,\s
                        CASE\s
                            WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId\s
                            THEN 'TRUE' ELSE 'FALSE'\s
                        END AS already_bought,\s
                        topic.no_of_topics\s
                    FROM course AS c\s
            
                    -- Course Review Aggregation
                    LEFT JOIN (
                        SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating\s
                        FROM course_review AS cr\s
                        GROUP BY cr.course_id
                    ) AS mcr ON c.id = mcr.course_id\s
            
                    -- Joins for Metadata
                    INNER JOIN course_category AS cc ON c.course_category_id = cc.id\s
                    INNER JOIN users AS us ON us.id = c.created_by\s
                    INNER JOIN course_level AS cl ON c.course_level = cl.id\s
                    INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                    LEFT JOIN user_profile AS up ON up.created_by = us.id\s
            
                    -- Enrollment & Favorite Courses
                    LEFT JOIN enrollment AS e ON e.course_id = c.id AND e.student_id = :userId\s
                    LEFT JOIN favourite_course AS fc ON fc.course_id = c.id AND fc.created_by = :userId\s
            
                    -- Course Completion
                    INNER JOIN user_course_completion ucc ON ucc.course_id = c.id AND ucc.user_id = :userId\s
            
                    -- Topic Count
                    LEFT JOIN (
                        SELECT COUNT(DISTINCT tp.id) AS no_of_topics, c.id\s
                        FROM course c\s
                        INNER JOIN section s ON s.course_id = c.id AND s.is_active = true\s
                        LEFT JOIN topic tp ON tp.section_id = s.id\s
                        GROUP BY c.id
                    ) AS topic ON topic.id = c.id\s
            
                    INNER JOIN section AS s ON s.course_id = c.id
                    INNER JOIN topic as t ON t.section_id = s.id
                    LEFT JOIN quiz as q ON q.topic_id = t.id
                    LEFT JOIN quiz_question as qq ON qq.quiz_id = q.id
            
                    -- Filtering & Grouping
                    WHERE (:title IS NULL OR c.title ILIKE :title)\s
                    AND c.course_status = 'PUBLISHED'\s
            
                    GROUP BY\s
                        c.id, c.content_type, c.title, cu.url, c.description, c.thumbnail,\s
                        us.id, us.full_name, up.profile_picture, up.profile_url,\s
                        c.course_duration_in_hours, cc.name, e.id, fc.id,\s
                        c.course_type, c.content_type, c.price, cl.name, mcr.max_rating, mcr.total_review, topic.no_of_topics\s
            
                    ORDER BY max_rating DESC, course_id DESC;
                    ;
            
            """,
            countQuery = """
                    SELECT DISTINCT\s
                        c.id AS course_id,\s
                        c.content_type,\s
                        c.title AS course_title,\s
                        cu.url AS course_url,\s
                        c.description AS course_description,\s
                        c.thumbnail AS course_thumbnail,\s
                        c.certificate_enabled,\s
                        c.content_type,
                        us.id AS user_id,\s
                        us.full_name,\s
                        up.profile_picture,\s
                        up.profile_url,\s
                        c.course_duration_in_hours,\s
                        COALESCE(mcr.max_rating, 0) AS max_rating,\s
                        COALESCE(mcr.total_review, 0) AS total_reviews,\s
                        cc.name AS category,\s
                        CASE WHEN e.id IS NOT NULL THEN 'true' ELSE 'false' END AS is_enrolled,\s
                        COUNT(CASE WHEN c.content_type = 'TEST' THEN qq.id END) AS test_total_question,
                        (SELECT SUM(t1.duration_in_sec)\s
                         FROM topic AS t1\s
                         INNER JOIN section AS s1 ON s1.id = t1.section_id\s
                         INNER JOIN course AS c1 ON c1.id = s1.course_id\s
                         WHERE c1.id = c.id) AS duration,
                        CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END AS is_favourite,\s
                        c.course_type,\s
                        c.price,\s
                        cl.name AS course_level,\s
                        CASE\s
                            WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId\s
                            THEN 'TRUE' ELSE 'FALSE'\s
                        END AS already_bought,\s
                        topic.no_of_topics\s
                    FROM course AS c\s
                    
                    -- Course Review Aggregation
                    LEFT JOIN (
                        SELECT cr.course_id, COUNT(cr.course_id) AS total_review, AVG(cr.rating) AS max_rating\s
                        FROM course_review AS cr\s
                        GROUP BY cr.course_id
                    ) AS mcr ON c.id = mcr.course_id\s
                    
                    -- Joins for Metadata
                    INNER JOIN course_category AS cc ON c.course_category_id = cc.id\s
                    INNER JOIN users AS us ON us.id = c.created_by\s
                    INNER JOIN course_level AS cl ON c.course_level = cl.id\s
                    INNER JOIN course_url AS cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
                    LEFT JOIN user_profile AS up ON up.created_by = us.id\s
                    
                    -- Enrollment & Favorite Courses
                    LEFT JOIN enrollment AS e ON e.course_id = c.id AND e.student_id = :userId\s
                    LEFT JOIN favourite_course AS fc ON fc.course_id = c.id AND fc.created_by = :userId\s
                    
                    -- Course Completion
                    INNER JOIN user_course_completion ucc ON ucc.course_id = c.id AND ucc.user_id = :userId\s
                    
                    -- Topic Count
                    LEFT JOIN (
                        SELECT COUNT(DISTINCT tp.id) AS no_of_topics, c.id\s
                        FROM course c\s
                        INNER JOIN section s ON s.course_id = c.id AND s.is_active = true\s
                        LEFT JOIN topic tp ON tp.section_id = s.id\s
                        GROUP BY c.id
                    ) AS topic ON topic.id = c.id\s
                    
                    INNER JOIN section AS s ON s.course_id = c.id
                    INNER JOIN topic as t ON t.section_id = s.id
                    LEFT JOIN quiz as q ON q.topic_id = t.id
                    LEFT JOIN quiz_question as qq ON qq.quiz_id = q.id
                    
                    -- Filtering & Grouping
                    WHERE (:title IS NULL OR c.title ILIKE :title)\s
                    AND c.course_status = 'PUBLISHED'\s
                    
                    GROUP BY\s
                        c.id, c.content_type, c.title, cu.url, c.description, c.thumbnail,\s
                        us.id, us.full_name, up.profile_picture, up.profile_url,\s
                        c.course_duration_in_hours, cc.name, e.id, fc.id,\s
                        c.course_type, c.content_type, c.price, cl.name, mcr.max_rating, mcr.total_review, topic.no_of_topics\s
                    
                    ORDER BY max_rating DESC, course_id DESC;
                    ;
                            """,
            nativeQuery = true)

    Page<Tuple> findAllEnrolledCompletedCoursesByUserId(String title, Long userId, Pageable pageable);

    @Query(value = """
        SELECT 
            c.id AS course_id,
            c.title AS course_title,
            cu.url AS course_url,
            c.description AS course_description,
            c.thumbnail AS course_thumbnail,
            c.certificate_enabled,
            us.id AS user_id,
            us.full_name,
            up.profile_picture,
            up.profile_url,
            c.course_duration_in_hours,
            COALESCE(mcr.max_rating, 0) AS max_rating,
            COALESCE(mcr.total_review, 0) AS total_reviews,
            cc.name AS category,
            'true' AS is_enrolled,
            (
                SELECT SUM(t1.duration_in_sec)
                FROM topic t1
                INNER JOIN section s1 ON s1.id = t1.section_id
                WHERE s1.course_id = c.id
            ) AS duration,
            CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END AS is_favourite,
            ucp.last_mod_date,
            c.course_type,
            c.price,
            cl.name AS course_level,
            CASE
                WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE'
                ELSE 'FALSE'
            END AS already_bought,
            COUNT(DISTINCT qq.id) AS test_total_question,
            c.content_type,
            topic.no_of_topics
        FROM course c
        -- Course Reviews
        LEFT JOIN (
            SELECT cr.course_id, COUNT(*) AS total_review, AVG(cr.rating) AS max_rating
            FROM course_review cr
            GROUP BY cr.course_id
        ) mcr ON c.id = mcr.course_id
        -- Other joins
        INNER JOIN course_category cc ON cc.id = c.course_category_id
        INNER JOIN course_level cl ON cl.id = c.course_level
        INNER JOIN users us ON us.id = c.created_by
        INNER JOIN enrollment e ON e.course_id = c.id AND e.student_id = :userId
        LEFT JOIN user_profile up ON up.created_by = us.id
        LEFT JOIN favourite_course fc ON fc.course_id = c.id AND fc.created_by = :userId
        INNER JOIN course_url cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
        -- Last modified progress
        LEFT JOIN (
            SELECT course_id, MAX(last_mod_date) AS last_mod_date
            FROM user_course_progress
            WHERE student_id = :userId
            GROUP BY course_id
        ) ucp ON ucp.course_id = c.id
        -- Topic count
        LEFT JOIN (
            SELECT c.id, COUNT(DISTINCT tp.id) AS no_of_topics
            FROM course c
            INNER JOIN section s ON s.course_id = c.id AND s.is_active = true
            LEFT JOIN topic tp ON tp.section_id = s.id
            GROUP BY c.id
        ) topic ON topic.id = c.id
        -- Quiz and question count
        LEFT JOIN section s ON s.course_id = c.id
        LEFT JOIN topic t ON t.section_id = s.id
        LEFT JOIN quiz q ON q.topic_id = t.id
        LEFT JOIN quiz_question qq ON qq.quiz_id = q.id
        WHERE (:title IS NULL OR c.title ILIKE :title) AND c.course_status = 'PUBLISHED'
        GROUP BY
            c.id, c.title, cu.url, c.description, c.thumbnail,
            us.id, us.full_name, up.profile_picture, up.profile_url,
            c.course_duration_in_hours, cc.name, fc.id, ucp.last_mod_date,
            c.course_type, c.price, cl.name, mcr.max_rating, mcr.total_review,
            c.content_type, topic.no_of_topics, e.student_id
        """,
            countQuery = """
        SELECT 
            c.id AS course_id,
            c.title AS course_title,
            cu.url AS course_url,
            c.description AS course_description,
            c.thumbnail AS course_thumbnail,
            c.certificate_enabled,
            us.id AS user_id,
            us.full_name,
            up.profile_picture,
            up.profile_url,
            c.course_duration_in_hours,
            COALESCE(mcr.max_rating, 0) AS max_rating,
            COALESCE(mcr.total_review, 0) AS total_reviews,
            cc.name AS category,
            'true' AS is_enrolled,
            (
                SELECT SUM(t1.duration_in_sec)
                FROM topic t1
                INNER JOIN section s1 ON s1.id = t1.section_id
                WHERE s1.course_id = c.id
            ) AS duration,
            CASE WHEN fc.id IS NOT NULL THEN 'true' ELSE 'false' END AS is_favourite,
            ucp.last_mod_date,
            c.course_type,
            c.price,
            cl.name AS course_level,
            CASE
                WHEN c.course_type = 'PREMIUM_COURSE' AND e.student_id = :userId THEN 'TRUE'
                ELSE 'FALSE'
            END AS already_bought,
            COUNT(DISTINCT qq.id) AS test_total_question,
            c.content_type,
            topic.no_of_topics
        FROM course c
        -- Course Reviews
        LEFT JOIN (
            SELECT cr.course_id, COUNT(*) AS total_review, AVG(cr.rating) AS max_rating
            FROM course_review cr
            GROUP BY cr.course_id
        ) mcr ON c.id = mcr.course_id
        -- Other joins
        INNER JOIN course_category cc ON cc.id = c.course_category_id
        INNER JOIN course_level cl ON cl.id = c.course_level
        INNER JOIN users us ON us.id = c.created_by
        INNER JOIN enrollment e ON e.course_id = c.id AND e.student_id = :userId
        LEFT JOIN user_profile up ON up.created_by = us.id
        LEFT JOIN favourite_course fc ON fc.course_id = c.id AND fc.created_by = :userId
        INNER JOIN course_url cu ON cu.course_id = c.id AND cu.status = 'ACTIVE'
        -- Last modified progress
        LEFT JOIN (
            SELECT course_id, MAX(last_mod_date) AS last_mod_date
            FROM user_course_progress
            WHERE student_id = :userId
            GROUP BY course_id
        ) ucp ON ucp.course_id = c.id
        -- Topic count
        LEFT JOIN (
            SELECT c.id, COUNT(DISTINCT tp.id) AS no_of_topics
            FROM course c
            INNER JOIN section s ON s.course_id = c.id AND s.is_active = true
            LEFT JOIN topic tp ON tp.section_id = s.id
            GROUP BY c.id
        ) topic ON topic.id = c.id
        -- Quiz and question count
        LEFT JOIN section s ON s.course_id = c.id
        LEFT JOIN topic t ON t.section_id = s.id
        LEFT JOIN quiz q ON q.topic_id = t.id
        LEFT JOIN quiz_question qq ON qq.quiz_id = q.id
        WHERE (:title IS NULL OR c.title ILIKE :title) AND c.course_status = 'PUBLISHED'
        GROUP BY
            c.id, c.title, cu.url, c.description, c.thumbnail,
            us.id, us.full_name, up.profile_picture, up.profile_url,
            c.course_duration_in_hours, cc.name, fc.id, ucp.last_mod_date,
            c.course_type, c.price, cl.name, mcr.max_rating, mcr.total_review,
            c.content_type, topic.no_of_topics, e.student_id
        """,
            nativeQuery = true)
    Page<Tuple> findAllEnrolledCoursesByUserId(@Param("title") String title,@Param("userId") Long userId, Pageable pageable);



    @Transactional(readOnly = true)
    boolean existsByCourseIdAndStudentEmail(Long courseId, String email);

    @Query(value = """
            SELECT COUNT( Distinct e.student_id) from enrollment as e
            Inner Join course as c ON c.id = e.course_id AND c.created_by = :instructorId
            """, nativeQuery = true)
    long findNoOfTotalStudentsEnrolledForATeacher(Long instructorId);

    @Query(value = """
            SELECT
                c.instructor_id,
                (SELECT COALESCE(COUNT(DISTINCT e2.student_id), 0)
                 FROM enrollment AS e2
                 INNER JOIN course AS c2 ON c2.id = e2.course_id
                 WHERE c2.instructor_id = :instructorId 
                 AND e2.student_id != c.instructor_id
                 AND e2.enrolled_date >=
                CASE
                    WHEN :period = 'monthly' THEN DATE_TRUNC('month', CURRENT_DATE)
                    WHEN :period = 'yearly' THEN DATE_TRUNC('year', CURRENT_DATE)
                    WHEN :period = 'previous_month' THEN DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month'
                    WHEN :period = 'previous_year' THEN DATE_TRUNC('year', CURRENT_DATE) - INTERVAL '1 year'
                END
                AND e2.enrolled_date <\s
                       CASE
                           WHEN :period = 'monthly' THEN DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
                           WHEN :period = 'yearly' THEN DATE_TRUNC('year', CURRENT_DATE) + INTERVAL '1 year'
                           WHEN :period = 'previous_month' THEN DATE_TRUNC('month', CURRENT_DATE)
                           WHEN :period = 'previous_year' THEN DATE_TRUNC('year', CURRENT_DATE)
                       END) AS total,
                CASE
                    WHEN :period = 'monthly' OR :period = 'previous_month' THEN DATE_TRUNC('month', e.enrolled_date)
                    WHEN :period = 'yearly' OR :period = 'previous_year' THEN DATE_TRUNC('year', e.enrolled_date)
                END AS period,
                COALESCE(COUNT(e.student_id), 0) AS total_students
            FROM
                enrollment AS e
            INNER JOIN course AS c ON c.id = e.course_id
            WHERE
                c.instructor_id = :instructorId
                AND e.student_id != c.instructor_id
                AND e.enrolled_date >=
                CASE
                    WHEN :period = 'monthly' THEN DATE_TRUNC('month', CURRENT_DATE)
                    WHEN :period = 'yearly' THEN DATE_TRUNC('year', CURRENT_DATE)
                    WHEN :period = 'previous_month' THEN DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month'
                    WHEN :period = 'previous_year' THEN DATE_TRUNC('year', CURRENT_DATE) - INTERVAL '1 year'
                END
                AND e.enrolled_date <\s
                       CASE
                           WHEN :period = 'monthly' THEN DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
                           WHEN :period = 'yearly' THEN DATE_TRUNC('year', CURRENT_DATE) + INTERVAL '1 year'
                           WHEN :period = 'previous_month' THEN DATE_TRUNC('month', CURRENT_DATE)
                           WHEN :period = 'previous_year' THEN DATE_TRUNC('year', CURRENT_DATE)
                       END
            GROUP BY
                c.instructor_id, period
            ORDER BY
                c.instructor_id, period;
                        
            """,
            nativeQuery = true
    )
    List<Tuple> findByInstructorId(String period, Long instructorId);

    @Query(value = """
            WITH students_with_course_1 AS (
                            SELECT DISTINCT
                                student_id
                            FROM
                                enrollment as e
            				INNER JOIN course as c ON c.id = e.course_id
                            WHERE
                                e.course_id = :courseId and c.course_status = 'PUBLISHED'
                        ),
                        course_counts AS (
                            SELECT
                                e.course_id,
                                COUNT(DISTINCT e.student_id) AS student_count
                            FROM
                                enrollment e
            				INNER JOIN course as c ON c.id = e.course_id
                            JOIN
                                students_with_course_1 s
                                ON e.student_id = s.student_id
            				WHERE c.course_status = 'PUBLISHED'
                            GROUP BY
                                e.course_id
                        )
                        SELECT
                            cc.course_id,
                            cc.student_count
                        FROM
                            course_counts cc
                        WHERE
                            cc.course_id != :courseId
                        ORDER BY
                            cc.student_count DESC;
            """, nativeQuery = true)
    List<Tuple> findRecommendedCoursesIDs(Long courseId);
    @Transactional(readOnly = true)
    Enrollment findByCourseIdAndStudentEmail(Long courseId, String email);

    long countByCourseId(Long courseId);


    @Query(value = """
            WITH RecentEnrollments AS (
                SELECT
                    e.student_id,
                    e.course_id,
                    e.enrolled_date,
                    e.is_active,
                    'Enrolled 15 Days Ago' AS enrollment_status
                FROM
                    enrollment e
                WHERE
                    DATE(e.enrolled_date) = CURRENT_DATE - INTERVAL '15 days'
            )
            SELECT
                e.student_id,
                u.full_name,
                u.email,
                e.course_id,
                c.title,
                cu.url,
                e.enrolled_date,
                e.is_active
            FROM
                enrollment e
            JOIN
                course c ON e.course_id = c.id
            JOIN
                course_url cu ON c.id = cu.course_id
            JOIN
                users u ON u.id = e.student_id
            WHERE
                e.student_id IN (SELECT student_id FROM RecentEnrollments)\s
            ORDER BY
                e.student_id, e.enrolled_date DESC;
            """, nativeQuery = true)
    List<Tuple> findStudentByEnrollmentDate();

    @Query(value = """
    SELECT new com.vinncorp.fast_learner.response.premium_student.PremiumStudentResponse(
        u.id, u.fullName, u.email, c.id, c.title, e.enrolledDate)
    FROM Enrollment e
    INNER JOIN Course c ON e.course.id = c.id
    INNER JOIN User u ON u.id = e.student.id
    WHERE c.courseType = 'PREMIUM_COURSE' AND c.createdBy = :userId AND e.student.id != :userId
    AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(:search) OR LOWER(c.title) LIKE LOWER(:search))
    """)
    Page<PremiumStudentResponse> findPremiumStudentsRaw(Long userId, String search, Pageable pageable);

    @Query(value = """
    SELECT u.id AS userId, u.full_name AS fullName, u.email AS email, 
           c.id AS courseId, c.title AS courseTitle, e.enrolled_date AS enrolledDate
    FROM enrollment e
    INNER JOIN course c ON e.course_id = c.id
    INNER JOIN users u ON u.id = e.student_id
    WHERE c.course_type = 'PREMIUM_COURSE' 
    AND c.created_by = :userId 
    AND e.student_id != :userId
    AND (:search IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :search, '%')) 
         OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')))
    AND DATE(e.enrolled_date) >= COALESCE(CAST(:startDate AS date), '1900-01-01')
    AND DATE(e.enrolled_date) <= COALESCE(CAST(:endDate AS date), '9999-12-31')
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM enrollment e
    INNER JOIN course c ON e.course_id = c.id
    INNER JOIN users u ON u.id = e.student_id
    WHERE c.course_type = 'PREMIUM_COURSE' 
    AND c.created_by = :userId 
    AND e.student_id != :userId
    AND (:search IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :search, '%')) 
         OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')))
    AND DATE(e.enrolled_date) >= COALESCE(CAST(:startDate AS date), '1900-01-01')
    AND DATE(e.enrolled_date) <= COALESCE(CAST(:endDate AS date), '9999-12-31')
    """,
            nativeQuery = true)
    Page<Tuple> findPremiumStudentsWithFilter(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable);



    @Query(value = """
        SELECT new com.vinncorp.fast_learner.response.premium_student.PremiumStudentResponse(
        u.id, u.fullName, u.email, c.id, c.title, e.enrolledDate) 
        FROM Enrollment e 
        INNER JOIN Course c ON e.course.id = c.id 
        INNER JOIN User u ON u.id = e.student.id 
        WHERE c.courseType = 'PREMIUM_COURSE' 
        AND c.createdBy = :userId 
        AND e.student.id != :userId 
        AND e.enrolledDate BETWEEN :startDate AND :endDate
        """)
    Page<PremiumStudentResponse> findPremiumStudentsByDateRangeAndUserId(Long userId, Date startDate, Date endDate, Pageable pageable);

    @Query(value = """
            SELECT\s
                                             e.course_id,\s
                                             cp.id AS coupon_id,\s
                                             c.title,\s
                                             CASE\s
                                                 WHEN cp.id IS NOT NULL THEN (c.price - (c.price * (cp.discount / 100.0)))
                                                 ELSE c.price
                                             END AS price,
                                             e.student_id,\s
                                             e.enrolled_date,\s
                                             e.is_active
                                         FROM\s
                                             enrollment AS e
                                         INNER JOIN\s
                                             course AS c ON e.course_id = c.id
                                         LEFT JOIN\s
                                             premium_course_payout_transaction_history AS th\s
                                                 ON th.course_id = c.id AND th.student_id = :studentId
                                         LEFT JOIN\s
                                             coupon AS cp ON cp.id = th.coupon_id
                                         WHERE\s
                                             e.student_id = :studentId\s
                                             AND c.course_type = 'PREMIUM_COURSE'
                                         GROUP BY\s
                                             e.course_id, e.enrolled_date, e.student_id, e.is_active,
                                             c.title, c.price, cp.id, cp.discount
                                         ORDER BY\s
                                             e.enrolled_date DESC;;
            """, nativeQuery = true)
    Page<Tuple> findAllPremiumCoursesByStudentId(Long studentId, PageRequest pageRequest);

    @Query(value = """
        SELECT 
            e.course_id, 
            cp.id AS coupon_id,
            c.price AS original_price,
            CASE
                        WHEN cp.id IS NOT NULL THEN (c.price * (cp.discount / 100.0))
                        ELSE 0.0
                    END AS discount,
            c.title, 
            CASE
                WHEN cp.id IS NOT NULL THEN (c.price - (c.price * (cp.discount / 100.0)))
                ELSE c.price
            END AS price,
            e.student_id,
            u.full_name, 
            u.email, 
            e.enrolled_date, 
            e.is_active
        FROM enrollment AS e
        INNER JOIN course AS c ON e.course_id = c.id
        INNER JOIN users AS u ON u.id = e.student_id
        LEFT JOIN premium_course_payout_transaction_history AS th 
            ON th.course_id = c.id AND th.student_id = :studentId
        LEFT JOIN coupon AS cp ON cp.id = th.coupon_id
        WHERE e.student_id = :studentId 
          AND e.course_id = :courseId 
          AND c.course_type = 'PREMIUM_COURSE'
        GROUP BY 
            e.course_id, e.enrolled_date, e.student_id, 
            u.full_name, u.email, e.is_active,
            c.title, c.price, cp.id, cp.discount
        ORDER BY e.enrolled_date DESC
        """, nativeQuery = true)
    Tuple findAllPremiumCoursesByStudentIdAndCourseId(Long courseId, Long studentId);



}