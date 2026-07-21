package com.vinncorp.fast_learner.repositories.course;

import com.vinncorp.fast_learner.models.course.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseLevelRepository extends JpaRepository<CourseLevel, Long> {
    List<CourseLevel> findByIsActive(boolean b);
}