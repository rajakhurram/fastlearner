package com.vinncorp.fast_learner.es_repository;


import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.vinncorp.fast_learner.es_models.CourseContent;

import java.io.IOException;

public interface CustomESCourseContentRepository {
    SearchResponse searchCoursesWithHighlights(String searchQuery);
    CourseContent saveCourseContent(CourseContent courseContent) throws IOException;
}
