package com.vinncorp.fast_learner.repositories.quiz;

import com.vinncorp.fast_learner.dtos.quiz.QuizAnswer;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestion;
import com.vinncorp.fast_learner.dtos.quiz.RandomQuizProjection;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizQuestionAnwserRepository extends JpaRepository<QuizQuestionAnwser, Long> {
    List<QuizQuestionAnwser> findByQuizQuestionId(Long quizQuestionId);

    @Query(value = """
                select COALESCE(q.passing_criteria, 0) AS passing_criteria
                from quiz_question as qq
                inner join quiz q on q.id = qq.quiz_id 
                where qq.id = :questionId
            """, nativeQuery = true)
    Long findPassingCriteriaAndQuizIdByQuestionId(@Param("questionId") Long questionId);

    @Query(
            value = """
                    WITH RandomizedQuestions AS (
                        SELECT qq_sub.id AS question_id, q_sub.id AS quiz_id,
                               ROW_NUMBER() OVER (PARTITION BY q_sub.id ORDER BY RANDOM()) AS rn
                        FROM quiz_question qq_sub
                        INNER JOIN quiz q_sub ON qq_sub.quiz_id = q_sub.id
                    )
                    SELECT 
                       t.id AS topic_id,
                       q.id AS quiz_id,
                       qq.id AS question_id,
                       qq.question_text,
                       qq.question_type,
                       qq.explanation
                    FROM quiz_question qq
                    INNER JOIN quiz q ON qq.quiz_id = q.id
                    INNER JOIN topic t ON t.id = q.topic_id
                    INNER JOIN quiz_question_anwser qqa ON qqa.quiz_question_id = qq.id
                    INNER JOIN RandomizedQuestions rq ON qq.id = rq.question_id AND q.id = rq.quiz_id
                    WHERE t.id = :topicId
                    AND rq.rn <= q.random_question
                    ORDER BY q.id, rq.rn, qqa.id
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM quiz_question qq
                    INNER JOIN quiz q ON qq.quiz_id = q.id
                    INNER JOIN topic t ON t.id = q.topic_id
                    WHERE t.id = :topicId
                    """
            , nativeQuery = true)
    Page<RandomQuizProjection> findAllQuizQuestionAndAnswersByTopicRandom(@Param("topicId") Long topicId, Pageable pageable);


    @Query(value = """
                SELECT new com.vinncorp.fast_learner.dtos.quiz.QuizQuestion(
                    t.id, q.id, qq.id, qq.questionText, qq.questionType, qq.explanation
                )
                FROM com.vinncorp.fast_learner.models.quiz.QuizQuestion qq
                INNER JOIN qq.quiz q
                INNER JOIN q.topic t
                WHERE t.id = :topicId
                ORDER BY t.id, q.id, qq.id
            """,
            countQuery = """
                    SELECT COUNT(qq)
                    FROM QuizQuestion qq
                    INNER JOIN qq.quiz q
                    INNER JOIN q.topic t
                    WHERE t.id = :topicId
                    """)
    Page<QuizQuestion> findAllQuizQuestionAndAnswersByTopic(Long topicId, Pageable pageable);


    @Query(value = """
            select count(*) from quiz_question where quiz_id=:id
            """, nativeQuery = true)
    Long findTotalQuestionByQuizId(@Param("id") Long id);


    @Query("""
                SELECT new com.vinncorp.fast_learner.dtos.quiz.QuizAnswer(
                    qqa.id, qqa.answer, qqa.isCorrectAnswer
                )
                FROM QuizQuestionAnwser qqa
                WHERE qqa.quizQuestion.id = :quizQuestionId
                ORDER BY qqa.id
            """)
    List<QuizAnswer> findByQuizQuestionIdCustom(Long quizQuestionId);
}