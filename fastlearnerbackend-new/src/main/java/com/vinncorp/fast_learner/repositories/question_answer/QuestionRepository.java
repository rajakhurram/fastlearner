package com.vinncorp.fast_learner.repositories.question_answer;

import com.vinncorp.fast_learner.models.question_answer.Question;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
            WITH RecursiveCTE AS (
                SELECT
                    q.id AS question_id,
                    q.question_text AS question,
                    a.id AS answer_id,
                    a.answer_text AS answer,
                    a.answer_id AS parent_answer_id,
                    ROW_NUMBER() OVER (ORDER BY q.id) AS RowNum 
                FROM question q
                LEFT JOIN answer a ON q.id = a.question_id AND a.answer_id IS NULL
            	where q.course_id = :courseId
                        
                UNION ALL
                        
                SELECT
                    q.id AS question_id,
                    q.question_text AS question,
                    a.id AS answer_id,
                    a.answer_text AS answer,
                    a.answer_id AS parent_answer_id,
                    ROW_NUMBER() OVER (ORDER BY q.id) AS RowNum
                FROM question q
                INNER JOIN answer a ON q.id = a.question_id
                INNER JOIN RecursiveCTE r ON r.answer_id = a.answer_id
            	where q.course_id = :courseId
            )
                        
            SELECT question_id, question, answer_id, answer, parent_answer_id
            FROM RecursiveCTE
            WHERE RowNum BETWEEN :pageNo AND :pageSize 
            ORDER BY question_id, answer_id
            """, nativeQuery = true)
    List<Tuple> findAllQuestionAnswerForCourse(Long courseId, int pageNo, int pageSize);


    @Query(value = """
            Select distinct q.id as question_id, a.id as answer_id, a.answer_text, q.question_text,t.ChildCount, a.answer_id from answer as a
            inner join question q on a.question_id=q.id
            left JOIN\s
            (Select id,0 as ChildCount from answer where answer_id is null and
            id not in (Select distinct  answer_id from answer where-- question_id=2 and\s
            answer_id is not null)\s
            --and question_id=2
            union all
            Select answer_id,count(1) as ChildCount from
            (
            select answer_id, (Select count(id) from answer where answer_id=M.Id) as ChildCount from answer M
            where answer_id is not null --and question_id=2
            )
            as T group by answer_id ) as t ON t.id = a.id
            WHERE a.answer_id is null
            """, nativeQuery = true)
    List<Tuple> findAllQAndAForCourse(Long courseId, int pageNo, int pageSize);

    @Query(value = "SELECT answered.question_id, qO.created_by, qO.question_text, answered.no_of_answers, t.id as topic_id, t.name as topic_name, qO.course_id, u.full_name, up.profile_picture, up.profile_url \n" +
            "FROM (SELECT q.id as question_id, COUNT(a.question_id) as no_of_answers \n" +
            "FROM question as q \n" +
            "LEFT JOIN answer as a ON a.question_id = q.id \n" +
            "GROUP BY q.id ) as answered \n" +
            "INNER JOIN question as qO ON qO.id = answered.question_id \n" +
            "INNER JOIN topic as t ON t.id = qO.topic_id \n" +
            "INNER JOIN users as u on u.id = qO.created_by \n"+
            "INNER JOIN user_profile as up on u.id = up.created_by \n"+
            "WHERE qO.course_id = :courseId \n" +
            "ORDER BY qO.created_date DESC\n ",
            countQuery = "SELECT answered.question_id, qO.created_by, qO.question_text, answered.no_of_answers, t.id as topic_id, t.name as topic_name, qO.course_id, u.full_name, up.profile_picture, up.profile_url  \n" +
            "FROM (SELECT q.id as question_id, COUNT(a.question_id) as no_of_answers \n" +
            "FROM question as q \n" +
            "LEFT JOIN answer as a ON a.question_id = q.id \n" +
            "GROUP BY q.id ) as answered \n" +
            "INNER JOIN question as qO ON qO.id = answered.question_id \n" +
            "INNER JOIN topic as t ON t.id = qO.topic_id \n"+
            "INNER JOIN users as u on u.id = qO.created_by \n"+
            "INNER JOIN user_profile as up on u.id = up.created_by \n"+
            "WHERE qO.course_id = :courseId \n" +
            "ORDER BY qO.created_date DESC\n ",  nativeQuery = true)
    Page<Tuple> findAllQuestionsByCourse(Long courseId, Pageable pageable);

    @Query(value = "SELECT answered.question_id, qO.created_by, qO.question_text, answered.no_of_answers, t.id as topic_id, t.name as topic_name, qO.course_id, u.full_name, up.profile_picture \n" +
            "FROM (SELECT q.id as question_id, COUNT(a.question_id) as no_of_answers \n" +
            "FROM question as q \n" +
            "LEFT JOIN answer as a ON a.question_id = q.id \n" +
            "GROUP BY q.id ) as answered \n" +
            "INNER JOIN question as qO ON qO.id = answered.question_id \n" +
            "INNER JOIN topic as t ON t.id = qO.topic_id \n" +
            "INNER JOIN users as u on u.id = qO.created_by \n"+
            "INNER JOIN user_profile as up on u.id = up.created_by \n"+
            "WHERE qO.id = :questionId \n",
            countQuery = "SELECT answered.question_id, qO.created_by, qO.question_text, answered.no_of_answers, t.id as topic_id, t.name as topic_name, qO.course_id, u.full_name, up.profile_picture  \n" +
                    "FROM (SELECT q.id as question_id, COUNT(a.question_id) as no_of_answers \n" +
                    "FROM question as q \n" +
                    "LEFT JOIN answer as a ON a.question_id = q.id \n" +
                    "GROUP BY q.id ) as answered \n" +
                    "INNER JOIN question as qO ON qO.id = answered.question_id \n" +
                    "INNER JOIN topic as t ON t.id = qO.topic_id \n"+
                    "INNER JOIN users as u on u.id = qO.created_by \n"+
                    "INNER JOIN user_profile as up on u.id = up.created_by \n"+
                    "WHERE qO.id = :questionId \n",  nativeQuery = true)
    Tuple findQuestionById(Long questionId);

}
