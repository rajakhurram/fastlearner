package com.vinncorp.fast_learner.repositories.user;

import com.vinncorp.fast_learner.models.user.UserCourseCompletion;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserCourseCompletionRepository extends JpaRepository<UserCourseCompletion, Long> {

    @Query(value = """
            SELECT
                COUNT(DISTINCT e.course_id) AS enrolled,
                COUNT(ucc.id) AS completed
            FROM
                enrollment AS e
            INNER JOIN
                course AS c ON c.id = e.course_id
            LEFT JOIN
                user_course_completion AS ucc ON e.course_id = ucc.course_id AND e.student_id = ucc.user_id
            WHERE
                c.created_by = :instructorId
                AND
                CASE
                    WHEN :period = 'monthly' THEN DATE_TRUNC('month', e.enrolled_date) = DATE_TRUNC('month', CURRENT_DATE)
                    WHEN :period = 'yearly' THEN DATE_TRUNC('year', e.enrolled_date) = DATE_TRUNC('year', CURRENT_DATE)
                    WHEN :period = 'previous_month' THEN DATE_TRUNC('year', e.enrolled_date) = DATE_TRUNC('year', CURRENT_DATE) - INTERVAL '1 month'
                    WHEN :period = 'previous_year' THEN DATE_TRUNC('year', e.enrolled_date) = DATE_TRUNC('year', CURRENT_DATE) - INTERVAL '1 year'
                END; 
            """, nativeQuery = true)
    Tuple fetchCourseCompletion(String period, Long instructorId);


    UserCourseCompletion findByCourseIdAndUserId(Long courseId, Long userId);
}
