package com.vinncorp.fast_learner.repositories.user;

import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:toLowerCase)")
    Optional<User> findByEmail(String toLowerCase);

    @Query(value = """
            WITH filtered_users AS (
                SELECT *
                FROM users
                WHERE DATE(login_timestamp) IN (
                    CURRENT_DATE - INTERVAL '10 days',
                    CURRENT_DATE - INTERVAL '15 days'
                )
            )
            SELECT *,
                CASE
                    WHEN DATE(login_timestamp) = CURRENT_DATE - INTERVAL '10 days' THEN '10_days_ago'
                    WHEN DATE(login_timestamp) = CURRENT_DATE - INTERVAL '15 days' THEN '15_days_ago'
                END AS login_category
            FROM filtered_users;
            """, nativeQuery = true)
    List<Tuple> findAllUsersNotLoggedInForTenDays();


    @Query(value = """
            		SELECT
                course_id,
                COUNT(DISTINCT student_id) AS total_students,u.full_name,
            	up.specialization,up.profile_url,u.id AS user_id,up.profile_picture,up.about_me
            	
            FROM
                enrollment AS e
            	inner join course as c on e.course_id=c.id inner join users u
            	on u.id=c.instructor_id
                LEFT JOIN user_profile AS up ON up.created_by = u.id
            GROUP BY
                course_id ,up.about_me,up.specialization,
            	up.profile_url,u.full_name,u.id,up.profile_picture
            	order by total_students desc;
            """,nativeQuery = true,
            countQuery = """
                            		SELECT
                                course_id,
                                COUNT(DISTINCT student_id) AS total_students,u.full_name,
                            	up.specialization,up.profile_url,u.id AS user_id,up.profile_picture,up.about_me
                            	
                            FROM
                                enrollment AS e
                            	inner join course as c on e.course_id=c.id inner join users u\s
                            	on u.id=c.instructor_id
                                LEFT JOIN user_profile AS up ON up.created_by = u.id
                            GROUP BY
                                course_id ,up.about_me,up.specialization,
                            	up.profile_url,u.full_name,u.id,up.profile_picture
                            	order by total_students desc;
                    """)
    Page<Tuple> findAllTopInstructor(Pageable pageable);
}

