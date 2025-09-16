package com.vinncorp.fast_learner.es_services.course_content;

import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.es_dto.SearchCourses;
import com.vinncorp.fast_learner.es_models.CourseContent;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;

import java.io.IOException;
import java.util.List;

public interface IESCourseContentService {
//    CourseContent save(CourseContent courseContent) throws IOException;
    List<CourseContent> save(List<CourseContent> courseContents) throws IOException;
    SearchPage<CourseContent> searchCourses(String query, int pageNo, int pageSize);
    CourseContent getCourseContentById(String id);
    List<CourseContent> getCoursesByCreatedBy(Long createdById);
    List<SearchCourses> getCourseTitles(String query, int pageNo, int pageSize);
    List<InstructorProfileSearchDto> getInstructorProfiles(String query, int pageNo, int pageSize);
}
