package com.vinncorp.fast_learner.repositories.course;

import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseUrlRepository extends JpaRepository<CourseUrl, Long> {

    @Query("SELECT cu FROM CourseUrl cu " +
            "INNER JOIN Course c ON c.id = cu.course.id " +
            "WHERE LOWER(cu.url) = LOWER(:url) AND c.courseStatus IN :courseStatuses")
    Optional<CourseUrl> findByUrlAndCourseStatuses(String url, List<CourseStatus> courseStatuses);


    Optional<CourseUrl> findByCourseIdAndStatus(Long id, GenericStatus status);

    List<CourseUrl> findAllByCourseId(Long id);

    @Modifying
    @Query("DELETE FROM CourseUrl cu WHERE cu.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
