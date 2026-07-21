package com.vinncorp.fast_learner.repositories.tag;

import com.vinncorp.fast_learner.models.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByIsActiveAndNameLike(boolean b, String name);

    @Query(value = "SELECT t.* FROM tag as t " +
            "INNER JOIN course_tag as ct ON t.id = ct.tag_id " +
            "WHERE ct.course_id = :courseId AND t.is_active = true", nativeQuery = true)
    List<Tag> findByCourseId(Long courseId);

    @Query(
            value = """
                   SELECT t.name FROM tag AS t INNER JOIN section_tag as st
                   ON t.id = st.tag_id
                   WHERE id = :sectionId AND t.is_active = true
                    """,nativeQuery = true
    )
    List<String> findBySectionId(Long sectionId);
}