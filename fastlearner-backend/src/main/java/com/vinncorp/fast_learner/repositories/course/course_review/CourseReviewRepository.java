package com.vinncorp.fast_learner.repositories.course.course_review;

import com.vinncorp.fast_learner.models.course.course_review.CourseReview;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    @Transactional(readOnly = true)
    long countByCourse_Id(Long id);

    CourseReview findByCreatedByAndCourseId(Long userId, Long courseId);

    @Query(value = """
            SELECT cr.id as review_id, cr.comment, cr.rating, 
            COALESCE(cr.last_mod_date, cr.created_date) as created_date, 
            u.full_name, cr.likes, cr.dislikes, up.profile_picture, up.profile_url 
            FROM course_review as cr 
            INNER JOIN course as c ON c.id = cr.course_id 
            INNER JOIN users as u ON u.id = cr.created_by 
            INNER JOIN user_profile AS up ON up.created_by = u.id
            WHERE c.id = :courseId and c.course_status = 'PUBLISHED' AND cr.rating <> 0
            Order by rating desc, cr.created_date desc
            """,
            countQuery = """
                    SELECT cr.id as review_id, cr.comment, cr.rating, 
                    COALESCE(cr.last_mod_date, cr.created_date) as created_date, 
                    u.full_name, cr.likes, cr.dislikes, up.profile_picture, up.profile_url 
                    FROM course_review as cr 
                    INNER JOIN course as c ON c.id = cr.course_id 
                    INNER JOIN users as u ON u.id = cr.created_by 
                    INNER JOIN user_profile AS up ON up.created_by = u.id
                    WHERE c.id = :courseId and c.course_status = 'PUBLISHED' AND cr.rating <> 0
                    Order by rating desc, cr.created_date desc
                    """, nativeQuery = true)
    Page<Tuple> findByCourseId(Long courseId, Pageable pageable);

    @Query(value = """
        SELECT cr.rating, COUNT(cr.id) AS users
        FROM course_review AS cr 
        INNER JOIN course AS c ON c.id = cr.course_id 
        WHERE c.id = :courseId 
          AND c.course_status = 'PUBLISHED' 
          AND cr.rating <> 0
        GROUP BY cr.rating
        ORDER BY MIN(cr.created_date) ASC;
        """, nativeQuery = true)
    List<Tuple> findStudentFeedbackByCourseId(Long courseId);

    @Query(value = """
            SELECT COALESCE(COUNT(cr.rating), 0) AS total_reviewers,
            COALESCE(ROUND(CAST(SUM(cr.rating) AS numeric) / NULLIF(COUNT(cr.rating), 0), 2), 0.0) AS avg_reviews
            FROM public.course_review AS cr
            INNER JOIN course AS c ON c.id = cr.course_id
            WHERE c.id = :courseId
            """, nativeQuery = true)
    Tuple findStudentReviewsByCourseId(Long courseId);

    @Query(value = """
            SELECT cr.rating, COUNT(cr.id) as users
            FROM public.course_review as cr
            INNER JOIN course as c ON c.id = cr.course_id
            WHERE c.instructor_id = :instructorId and c.course_status = 'PUBLISHED'
            GROUP BY cr.rating;
            """,
            nativeQuery = true
    )
    List<Tuple> findStudentFeedbackForCoursesOfInstructor(Long instructorId);

    @Query(value = """
            SELECT cr.id AS review_id, cr.comment, cr.rating, cr.created_date, 
                   u.full_name, cr.likes, cr.dislikes, up.profile_picture
            FROM public.course_review AS cr
            INNER JOIN course AS c ON c.id = cr.course_id
            INNER JOIN users AS u ON u.id = cr.created_by
            INNER JOIN user_profile AS up ON up.created_by = u.id
            WHERE c.instructor_id = :instructorId and c.course_status = 'PUBLISHED'
              AND (:courseId IS NULL OR c.id = :courseId)
            ORDER BY cr.created_date DESC;
            """, nativeQuery = true)
    Page<Tuple> findAllCoursesReviewsOfAnInstructor(Long instructorId, Long courseId, Pageable pageable);

    @Query(value = """
            SELECT COALESCE(COUNT(cr.rating), 0) AS total_reviewers,
            COALESCE(ROUND(CAST(SUM(cr.rating) AS numeric) / NULLIF(COUNT(cr.rating), 0), 2), 0.0) AS avg_reviews
            FROM public.course_review AS cr
            INNER JOIN course AS c ON c.id = cr.course_id
            INNER JOIN users AS u ON u.id = cr.created_by
            WHERE c.instructor_id = ?1
            """, nativeQuery = true)
    Tuple findAllCoursesReviewsOfAnInstructor(Long instructorId);

    @Query(value = """
            SELECT cr.id as review_id, cr.comment, cr.rating, 
            COALESCE(cr.last_mod_date, cr.created_date) as created_date, 
            u.full_name, cr.likes, cr.dislikes, up.profile_picture 
            FROM course_review as cr 
            INNER JOIN users as u ON u.id = cr.created_by 
            INNER JOIN user_profile AS up ON up.created_by = u.id
             WHERE cr.id = :reviewId
            """,
            countQuery = """
                    SELECT cr.id as review_id, cr.comment, cr.rating, 
                    COALESCE(cr.last_mod_date, cr.created_date) as created_date, 
                    u.full_name, cr.likes, cr.dislikes, up.profile_picture 
                    FROM course_review as cr 
                    INNER JOIN users as u ON u.id = cr.created_by 
                    INNER JOIN user_profile AS up ON up.created_by = u.id
                    WHERE cr.id = :reviewId
                    """, nativeQuery = true)
    Tuple findByReviewId(Long reviewId);

}