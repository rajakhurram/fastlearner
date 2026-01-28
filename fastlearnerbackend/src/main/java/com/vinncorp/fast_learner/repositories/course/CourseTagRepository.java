package com.vinncorp.fast_learner.repositories.course;

import com.vinncorp.fast_learner.models.course.CourseTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface CourseTagRepository extends JpaRepository<CourseTag, Long> {
    @Modifying
    @Transactional
    void deleteAllByCourseId(Long courseId);
}