package com.vinncorp.fast_learner.repositories.user;

import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.models.user.UserProfile;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query(value = """
            SELECT u.id, u.full_name, u.email, p.profile_picture, p.specialization, 
            p.qualification, p.experience, p.headline, p.about_me, p.show_profile, 
            p.show_courses, p.website_url, p.facebook_url, p.twitter_url, 
            p.linked_in_url, p.youtube_url, p.profile_url,
            (SELECT COALESCE(COUNT(DISTINCT e.student_id), 0) FROM course as c
            LEFT JOIN enrollment as e ON c.id = e.course_id WHERE c.created_by = :userId AND e.student_id != :userId) as total_students, 
            (SELECT COALESCE(COUNT(cr.course_id), 0) FROM course as c
            LEFT JOIN course_review as cr ON cr.course_id = c.id WHERE c.created_by = :userId) as total_reviews
            FROM users u 
            INNER JOIN user_profile p ON u.id = p.created_by 
            WHERE u.id = :userId
            """, nativeQuery = true)
    Optional<Tuple> findProfileInfoByUserId(Long userId);

    Optional<UserProfile> findOneByCreatedBy(Long id);

    @Query(value = "SELECT NEW com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto(u.id, u.fullName, up.profilePicture, up.profileUrl) " +
            "FROM User u " +
            "INNER JOIN UserProfile up ON u.id = up.createdBy " +
            "INNER JOIN Course c ON c.createdBy = u.id " +
            "WHERE LOWER(u.fullName) LIKE LOWER(:searchTerm) " +
            "AND c.courseStatus IN ('PUBLISHED', 'UNPUBLISHED', 'DELETE') " +
            "GROUP BY u.id, u.fullName, up.profilePicture, up.profileUrl"
    )
    Page<InstructorProfileSearchDto> getSearchInstructorProfiles(String searchTerm, PageRequest of);

    Optional<UserProfile> findByProfileUrl(String finalUrl);
}