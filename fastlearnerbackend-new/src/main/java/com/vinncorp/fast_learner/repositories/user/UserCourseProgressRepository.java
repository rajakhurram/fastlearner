package com.vinncorp.fast_learner.repositories.user;

import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserCourseProgressRepository extends JpaRepository<UserCourseProgress, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserCourseProgress> findByTopic_IdAndStudent_Email(Long topicId, String email);
    Optional<UserCourseProgress> findFirstByCourse_IdAndStudent_EmailOrderByLastModifiedDateDesc(Long id, String email);

    @Query(value = """
            SELECT
                CASE
                    WHEN COUNT(t.id) = 0 THEN 0
                    ELSE
                        CASE
                            WHEN COUNT(ucp.topic_id) = 0 THEN 0
                            ELSE COUNT(ucp.topic_id) * 100.0 / COUNT(t.id)
                        END
                END AS completion_percentage
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
            WHERE
                c.id = :courseId
            """, nativeQuery = true)
    Double fetchCourseProgress(Long courseId, Long userId);

    @Query(value = """
            SELECT
                c.id,
                CASE
                    WHEN COUNT(t.id) = 0 THEN 0
                    ELSE
                        CASE
                            WHEN COUNT(ucp.topic_id) = 0 THEN 0
                            ELSE COUNT(ucp.topic_id) * 100.0 / COUNT(t.id)
                        END
                END AS completion_percentage
            FROM
                course c
            INNER JOIN
                section s ON s.course_id = c.id
            INNER JOIN
                topic t ON t.section_id = s.id
            LEFT JOIN
                user_course_progress ucp ON ucp.topic_id = t.id
                                          AND ucp.student_id = :userId
                                          AND ucp.is_completed = true
            WHERE
                c.id IN (:ids)
            GROUP BY
                c.id
            """, nativeQuery = true)
    List<Tuple> findCourseProgressByListOfCoursesAndUser(List<Long> ids, Long userId);


    UserCourseProgress findOneByStudentId(Long id);

    @Query(value = """
            SELECT
               	u.id as instructor_id,
                u.full_name,
                u.stripe_account_id,
                CASE
                	WHEN total.total_seek_time = 0 THEN 0
                    ELSE (SUM(ucp.seek_time) / total.total_seek_time) * (:subscriptionPercentage  * u.sales_raise)
                END AS relative_seek_time
            FROM
                user_course_progress AS ucp
            INNER JOIN
                course AS c ON ucp.course_id = c.id
            INNER JOIN
                users AS u ON u.id = c.created_by
            INNER JOIN
                (SELECT
                     SUM(ucp.seek_time) AS total_seek_time
                 FROM
                     user_course_progress AS ucp
                 WHERE
                     student_id = :id
            		 AND ucp.last_mod_date >= date_trunc('month', current_date) - interval '1 month' + interval '25 days' 
              		 AND ucp.last_mod_date < date_trunc('month', current_date) + interval '24 days'
            	) AS total
            ON
                true
            WHERE
                c.course_type != 'PREMIUM_COURSE' 
                AND student_id = :id
            	AND ucp.last_mod_date >= date_trunc('month', current_date) - interval '1 month' + interval '25 days'
              	AND ucp.last_mod_date < date_trunc('month', current_date) + interval '24 days'
            GROUP BY
                u.id, u.full_name, total.total_seek_time;
            """,
            nativeQuery = true
    )
    List<Tuple> fetchAllInstructorSalesByStudentId(Long id, Double subscriptionPercentage);

    @Query(value = """
            WITH all_months AS (
                SELECT generate_series(1, 12) AS month
            ),
            active_users AS (
                SELECT
                    EXTRACT(MONTH FROM ucp.created_date) AS month,
                    TO_CHAR(ucp.created_date, 'Month') AS month_name,
                    COUNT(DISTINCT ucp.student_id) AS active_students
                FROM
                    user_course_progress as ucp
                    INNER JOIN course as c ON c.id = ucp.course_id
                WHERE
                    EXTRACT(YEAR FROM ucp.created_date) = EXTRACT(YEAR FROM CURRENT_DATE)
                    AND c.instructor_id = :id and c.course_status = 'PUBLISHED'
                    AND (:courseId is null OR ucp.course_id = :courseId)
                GROUP BY
                    EXTRACT(MONTH FROM ucp.created_date),
                    TO_CHAR(ucp.created_date, 'Month')
            )
            SELECT
                m.month,
                TO_CHAR(TO_DATE(CAST(m.month AS VARCHAR), 'MM'), 'Month') AS month_name,
                COALESCE(a.active_students, 0) AS active_students
            FROM
                all_months m
            LEFT JOIN
                active_users a
            ON
                m.month = a.month
            ORDER BY
                m.month;
            """, nativeQuery = true)
    List<Tuple> findAllActiveStudents(Long courseId, Long id);

    void deleteAllByTopicId(Long topicId);
}
