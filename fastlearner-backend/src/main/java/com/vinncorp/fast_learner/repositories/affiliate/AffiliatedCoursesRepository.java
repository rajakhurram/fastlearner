package com.vinncorp.fast_learner.repositories.affiliate;
import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto;
import com.vinncorp.fast_learner.models.affiliate.AffiliatedCourses;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AffiliatedCoursesRepository extends JpaRepository<AffiliatedCourses,Long> {

    @Query("SELECT new com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto(" +
            "ac.id, ac.assignDate, ac.onboardedStudents, ac.reward, ac.url, ac.course.id, ac.instructorAffiliate.id, " +
            "ac.course.title, " +
            "ac.revenue) " +
            "FROM AffiliatedCourses ac " +
            "WHERE ac.instructorAffiliate.id = :instructorAffiliateId " +
            "AND ac.course.id = :courseId " +
            "AND ac.status = :status")
    Optional<AffiliateCourseDto> getByInstructorAffiliateIdAndCourseIdAndStatus(
            @Param("instructorAffiliateId") Long instructorAffiliateId,
            @Param("courseId") Long courseId,
            @Param("status") GenericStatus status);

    @Query("SELECT new com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto(\n" +
            "    ac.id, \n" +
            "    ac.assignDate, \n" +
            "    COALESCE(ac.onboardedStudents, 0) as onboardedStudents, \n" +
            "    ac.reward, \n" +
            "    ac.url, \n" +
            "    ac.course.id, \n" +
            "    ac.instructorAffiliate.id, \n" +
            "    ac.course.title, \n" +
            "    COALESCE(ROUND(ac.revenue, 2), 0) as revenue, \n" +
            "    ac.status\n" +
            ") \n" +
            "FROM AffiliatedCourses ac \n" +
            "INNER JOIN ac.course c \n" +
            "WHERE ac.instructorAffiliate.id = :instructorAffiliateId \n" +
            "AND ac.course.courseStatus = 'PUBLISHED'")
    Page<AffiliateCourseDto> getByInstructorAffiliateIdAndStatus(
            @Param("instructorAffiliateId") Long instructorAffiliateId,
            Pageable pageable);

    @Query(value = "SELECT new com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto(" +
            "ac.id, ac.assignDate, COALESCE(ac.onboardedStudents, 0), ac.reward, ac.url, " +
            "ac.course.id, ac.instructorAffiliate.id, ac.course.title, " +
            "COALESCE(ac.revenue, 0), a.id, " +
            "CASE " +
            "  WHEN :email = a.email THEN CONCAT(ia.username, ' (Self)') " +
            "  ELSE ia.username " +
            "END) " +
            "FROM AffiliatedCourses ac " +
            "INNER JOIN InstructorAffiliate ia ON ia.id = ac.instructorAffiliate.id " +
            "INNER JOIN Affiliate a ON a.id = ia.affiliateUser.id " +
            "WHERE ia.instructor.id = :instructorId " +
            "AND ac.course.id = :courseId " +
            "AND ia.status = :instructorAffiliateStatus " +
            "AND ac.status = :affiliateCourseStatus")
    Page<AffiliateCourseDto> getAllAffiliatesByCourseAndStatus(
            @Param("instructorId") Long instructorId,
            @Param("courseId") Long courseId,
            @Param("email") String email,
            @Param("instructorAffiliateStatus") GenericStatus instructorAffiliateStatus,
            @Param("affiliateCourseStatus") GenericStatus affiliateCourseStatus,
            Pageable pageable);

    @Query(value = """
            select aff.id,aff.email,c.price,c.title, aff.stripe_account_id,aff.onboard_status,affu.affiliate_uuid,ac.course_id,
            ac.instructor_affiliate,ac.revenue,ac.reward,c.price from\s
            instructor_affiliate AS affu inner join affiliated_courses as ac on
            affu.id= ac.instructor_affiliate inner join affiliate aff on aff.id=affu.affiliate_user\s
            inner join course c on c.id=ac.course_id
            where affu.affiliate_uuid= :uuid
            and ac.course_id= :courseId AND ac.status='ACTIVE';
            """, nativeQuery = true)
    Tuple findByUuidAndCourseId(@Param("uuid") String uuid, @Param("courseId") Long courseId);

    @Query(value = """
            SELECT COUNT(*)\s
            FROM affiliated_courses ac
            INNER JOIN instructor_affiliate ia on ia.id = ac.instructor_affiliate
            where ac.status = :affiliateCourseStatus and ia.status = :instructorAffiliateStatus
            and ia.instructor_id = :instructorId and ac.course_id = :courseId""", nativeQuery = true)
    Long getCountOfAssignedAffiliatesByInstructorAndCourse(@Param("instructorId") Long instructorId,
                                                           @Param("courseId") Long courseId,
                                                           @Param("instructorAffiliateStatus") String instructorAffiliateStatus,
                                                           @Param("affiliateCourseStatus") String affiliateCourseStatus
    );

}
