package com.vinncorp.fast_learner.repositories.chat;

import com.vinncorp.fast_learner.models.chat.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    @Query(value = """
            select ch.* from chat_history ch
            INNER JOIN chat c ON c.id = ch.chat_id
            where c.id = :chatId AND c.created_by = :userId
            ORDER BY ch.created_date ASC
            """, nativeQuery = true)
    List<ChatHistory> findByChat_Id(Long chatId, Long userId);

    @Query(value = """
            SELECT ch.* FROM chat_history ch INNER JOIN
                chat c ON c.id=ch.chat_id
                where c.video_id= :videoId AND created_by= :userId 
                ORDER BY ch.created_date
            """, nativeQuery = true)
    List<ChatHistory> findByVideo_Id(Long videoId, Long userId);
}
