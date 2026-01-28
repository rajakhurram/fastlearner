package com.vinncorp.fast_learner.repositories.course;

import com.vinncorp.fast_learner.models.course.CourseVisitor;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseVisitorRepository extends JpaRepository<CourseVisitor, Long> {

    @Query(value = """
            SELECT
                TO_CHAR(ms.month_series, 'MM') AS month,
                TO_CHAR(ms.month_series, 'Month') AS month_name,
                COALESCE(COUNT(cv.id), 0) AS visit_count
            FROM (
                SELECT generate_series(
                    DATE_TRUNC('year', CURRENT_DATE),
                    DATE_TRUNC('year', CURRENT_DATE) + INTERVAL '1 year' - INTERVAL '1 day',
                    INTERVAL '1 month'
                ) AS month_series
            ) AS ms
            LEFT JOIN
                course_visitor AS cv ON 
                    TO_CHAR(cv.visited_at, 'YYYY-MM') = TO_CHAR(ms.month_series, 'YYYY-MM')
                    AND EXTRACT(YEAR FROM cv.visited_at) = EXTRACT(YEAR FROM CURRENT_DATE)
            WHERE
                instructor_id = :id  AND (:courseId is null OR course_id = :courseId)
            OR cv.id IS NULL
            GROUP BY
                ms.month_series
            ORDER BY
                ms.month_series;
            """, nativeQuery = true)
    List<Tuple> findAllVisitorsByInstructorId(Long id, Long courseId);

    long countByCourseId(long courseId);
}
