package com.vinncorp.fast_learner.repositories.notification;

import com.vinncorp.fast_learner.models.notification.Notification;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByContentContainsAndType(String content, NotificationType type);

    @Query(value = """
            SELECT u.id, u.full_name, up.profile_picture
            FROM user_profile as up
            INNER JOIN users u ON up.created_by = u.id
            WHERE u.id = :id
            """, nativeQuery = true)
    Tuple findByUserId(Long id);

    @Query(value = """
            SELECT u.id, u.full_name, up.profile_picture
            FROM user_profile as up
            INNER JOIN users u ON up.created_by = u.id
            WHERE u.email = :email
            """, nativeQuery = true)
    Tuple findByEmail(String email);

    @Query(value = """
            SELECT (SELECT ARRAY_AGG(DISTINCT student_id) AS student_ids
            FROM public.enrollment
            WHERE course_id IN (
                SELECT DISTINCT id
                FROM course
                WHERE created_by = :instructorId
            )) as user_ids, usr.full_name as name, up.profile_picture as image_url FROM users as usr
            INNER JOIN user_profile as up ON up.created_by = usr.id
            where usr.id = :instructorId
            """, nativeQuery = true)
    Tuple findByInstructorId(Long instructorId);

    @Query(value = """
            SELECT
                n.*                
            FROM
                Notification n
            WHERE
                ?1 = ANY(n.receiver_ids)
            ORDER BY
                n.creation_date DESC
            LIMIT
                5;
            """, nativeQuery = true)
    List<Notification> findByReceiverId(Long receiverId);

    @Query(value = """
            SELECT COALESCE(COUNT(*), 0) as total_unread
                 FROM Notification
                 WHERE ?1 = ANY(receiver_ids) AND is_read = false
            """, nativeQuery = true)
    Long totalUnreadNotifications(Long receiverId);

    @Query(value = """
            SELECT n.* FROM Notification n WHERE ?1 = ANY(receiver_ids) ORDER BY creation_date DESC;;
            """,
            countQuery = """
            SELECT n.* FROM Notification n WHERE ?1 = ANY(receiver_ids) ORDER BY creation_date DESC;;
            """, nativeQuery = true)
    Page<Notification> findByReceiverIdWithPagination(Long receiverId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update Notification n  SET n.isRead = true where n.id IN :notificationId")
    void readANotification(@Param("notificationId") List<Long> notificationId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE notification SET is_read = true WHERE ?1 = ANY(receiver_ids)", nativeQuery = true)
    void readAllNotificationByReceiverId(Long receiverId);
}
