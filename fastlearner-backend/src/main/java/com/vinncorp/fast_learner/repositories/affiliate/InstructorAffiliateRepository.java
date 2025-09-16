package com.vinncorp.fast_learner.repositories.affiliate;

import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;

import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorAffiliateRepository extends JpaRepository<InstructorAffiliate, Long> {

    @Query(value = "SELECT id, created_date, default_reward, status, nickname, affiliate_user, instructor_id " +
            "FROM instructor_affiliate " +
            "WHERE instructor_id = :instructor_id " +
            "AND (affiliate_user = :affiliate_user OR nickname = :nickname)", nativeQuery = true)
    List<Tuple> findByInstructorAndAffiliateOrNickName(Long instructor_id, Long affiliate_user, String nickname);


    @Query(value = """
            SELECT id, created_date, default_reward, status, nickname,username,affiliate_user, instructor_id
            FROM instructor_affiliate
            WHERE instructor_id = :instructor_id
              AND affiliate_user = :affiliate_user
            """, nativeQuery = true)
    Tuple findByInstructorAndAffiliateId(@Param("instructor_id") Long instructorId, @Param("affiliate_user") Long affiliateUserId);

    @Query(value = """
            SELECT DISTINCT
                aff.id,
                afu.id AS affiliation_id,
                afu.created_date,
                afu.default_reward,
                afu.status,
                afu.nickname AS nick_name,
                aff.email,
                afu.username,
                afu.affiliate_uuid,
                afu.last_mod_date,
                CASE
                    WHEN aff.email = :currentUserEmail THEN TRUE
                    ELSE FALSE
                END AS is_self,
                COALESCE(afu.last_mod_date, afu.created_date) AS sort_date
            FROM
                instructor_affiliate afu
            INNER JOIN
                affiliate aff ON afu.affiliate_user = aff.id
            INNER JOIN
                users us ON us.id = afu.instructor_id
            WHERE
                afu.instructor_id = :instructorId
                AND afu.status = 'ACTIVE'
                AND (
                    :searchTerm IS NULL OR
                    afu.username ILIKE '%' || :searchTerm || '%' OR
                    afu.nickname ILIKE '%' || :searchTerm || '%' OR
                    aff.email ILIKE '%' || :searchTerm || '%'
                )
            ORDER BY sort_date DESC;
            """, nativeQuery = true
            , countQuery = """
            SELECT DISTINCT
                                            aff.id,
                                            afu.id AS affiliation_id,
                                            afu.created_date,
                                            afu.default_reward,
                                            afu.status,
                                            afu.nickname AS nick_name,
                                            aff.email,
                                            afu.username,
                                            afu.affiliate_uuid,
                                            afu.last_mod_date,
                                            CASE
                                                WHEN aff.email = :currentUserEmail THEN TRUE
                                                ELSE FALSE
                                            END AS is_self,
                                            COALESCE(afu.last_mod_date, afu.created_date) AS sort_date
                                        FROM
                                            instructor_affiliate afu
                                        INNER JOIN
                                            affiliate aff ON afu.affiliate_user = aff.id
                                        INNER JOIN
                                            users us ON us.id = afu.instructor_id
                                        WHERE
                                            afu.instructor_id = :instructorId
                                            AND afu.status = 'ACTIVE'
                                            AND (
                                                :searchTerm IS NULL OR
                                                afu.username ILIKE '%' || :searchTerm || '%' OR
                                                afu.nickname ILIKE '%' || :searchTerm || '%' OR
                                                aff.email ILIKE '%' || :searchTerm || '%'
                                            )
                                        ORDER BY sort_date DESC;
            """)
    Page<Tuple> findByInstructorAndIsActive(@Param("searchTerm") String searchTerm,
                                            @Param("instructorId") Long instructorId, @Param("currentUserEmail") String currentUserEmail,
                                            Pageable pageable);

    @Query(value = "SELECT id, affiliate_uuid, created_date, default_reward, last_mod_date, nickname, status, username, affiliate_user, instructor_id " +
            "FROM instructor_affiliate " +
            "WHERE instructor_id = :instructor_id " +
            "AND nickname = :nickName AND status='ACTIVE'", nativeQuery = true)
    InstructorAffiliate findByInstructorAndNickName(Long instructor_id, String nickName);

    @Query(value = """
            SELECT id, affiliate_uuid, created_date, default_reward, last_mod_date, nickname, status, username, affiliate_user, instructor_id             FROM instructor_affiliate\s
                        WHERE instructor_id = :instructor_id\s
                        AND nickname = :nickName AND status='ACTIVE' And id!=:affiliateUserId""", nativeQuery = true)
    InstructorAffiliate findByInstructorAndNickNameAndNotAffUserId(Long affiliateUserId, Long instructor_id, String nickName);


    @Query(value = "SELECT id, affiliate_uuid, created_date, default_reward, last_mod_date, nickname, status, username, affiliate_user, instructor_id " +
            "FROM instructor_affiliate " +
            "WHERE instructor_id = :instructor_id " +
            "AND nickname = :nickName AND status='INACTIVE'", nativeQuery = true)
    InstructorAffiliate findByInstructorAndNickNameAndInActive(Long instructor_id, String nickName);


    @Query("SELECT ia FROM InstructorAffiliate ia WHERE ia.instructor.id = :instructorId AND ia.affiliateUser.id = :affiliateId AND ia.status = :status")
    InstructorAffiliate findActiveAffiliateByInstructorAndAffiliateUser(
            @Param("instructorId") Long instructorId,
            @Param("affiliateId") Long affiliateId,
            @Param("status") GenericStatus status);


    @Query(value = """
            SELECT aff.id AS affiliate_id,
                   ia.id AS instructor_affiliate_id,
                   ia.username AS username,
                   COALESCE(SUM(ac.onboarded_students), 0) AS onboarded_students,
                   aff.onboard_status,
                   CASE
                       WHEN aff.email = :currentUserEmail THEN TRUE
                       ELSE FALSE
                   END AS is_self,
                   COALESCE(SUM(ac.revenue), 0) AS total_revenue,
                   aff.email AS email,
                   ia.default_reward,
                   ia.nickname AS nick_name
            FROM instructor_affiliate ia
            LEFT JOIN affiliated_courses ac ON ia.id = ac.instructor_affiliate
            INNER JOIN affiliate aff ON aff.id = ia.affiliate_user
            WHERE ia.id = :instructorAffiliateId
            GROUP BY ia.id,
                     ia.nickname,
                     aff.email,
                     ia.default_reward,
                     aff.onboard_status,
                     aff.id
            """, nativeQuery = true)
    Tuple findByInstructorAff(
            @Param("instructorAffiliateId") Long instructorAffiliateId,
            @Param("currentUserEmail") String currentUserEmail
    );

    @Query(value = """
            		select * from course c inner join users u on u.id=c.created_by
                    where c.id =:courseId and u.email=:email
            """, nativeQuery = true)
    Tuple findByAffiliateId(@Param("courseId") Long courseId,@Param("email")String email);

    @Query("SELECT ia FROM InstructorAffiliate ia WHERE ia.affiliateUuid = :affiliateUUID AND ia.status = :status")
    InstructorAffiliate findByAffiliateUUID(
            @Param("affiliateUUID") String affiliateUUID,
            @Param("status") GenericStatus status);

    @Query(value = """
            select * from instructor_affiliate where affiliate_user=:affiliateId
            """,nativeQuery = true)
    InstructorAffiliate findByAffiliateUser(@Param("affiliateId") Long affiliateId);
}

