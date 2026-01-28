package com.vinncorp.fast_learner.repositories.question_answer;

import com.vinncorp.fast_learner.models.question_answer.Answer;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Query(value = """
            SELECT a.id as answer_id, a.answer_text, u.full_name, up.profile_picture, up.profile_url FROM answer a
            INNER JOIN question q ON a.question_id = q.id
            INNER JOIN users as u on u.id = a.created_by
            INNER JOIN user_profile as up on u.id = up.created_by
            WHERE q.id = :questionId and q.course_id = :courseId
            """, nativeQuery = true)
    Page<Tuple> findAllAnswerByCourseIdAndQuestionId(Long courseId, Long questionId, Pageable pageable);
}
