package com.vinncorp.fast_learner.es_repository;

import com.vinncorp.fast_learner.es_models.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface ESCourseRepository extends ElasticsearchRepository<Course, String> {
    Optional<Course> findByDbId(Long dbId);

    @Query("{\"match\": {\"title\": {\"query\": \"?0\"}}}")
    Page<Course> findByTitleWithCustomAnalyzer(String title, Pageable pageable);

}
