package com.vinncorp.fast_learner.repositories.course;

import com.vinncorp.fast_learner.models.course.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Long> {
    List<CourseCategory> findAllByIsActive(boolean b);
}