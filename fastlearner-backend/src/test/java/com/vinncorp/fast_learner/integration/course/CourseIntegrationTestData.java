package com.vinncorp.fast_learner.integration.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.models.course.CourseLevel;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CourseIntegrationTestData {
    public static Course courseData() throws IOException {
        File createCourseRequestJsonFile = new File("src/main/resources/testing/CreateCourseRequest.json");
        ObjectMapper objectMapper = new ObjectMapper();
        CreateCourseRequest createCourseRequest = objectMapper.readValue(createCourseRequestJsonFile, CreateCourseRequest.class);

        CourseCategory courseCategory = CourseCategory.builder().id(1L).name("Development").isActive(true).build();
        CourseLevel courseLevel = CourseLevel.builder().id(1L).name("Intermediate").isActive(true).build();

        StringBuilder documentVector = new StringBuilder();
        documentVector.append(createCourseRequest.getTitle()).append(' ');
        documentVector.append(createCourseRequest.getAbout()).append(' ');
        documentVector.append(courseCategory.getName()).append(' ');
        createCourseRequest.getTags().forEach(e -> documentVector.append(e.getName()).append(' '));

        Course course = Course.builder()
                .about(createCourseRequest.getAbout())
                .courseLevel(courseLevel)
                .courseCategory(courseCategory)
                .courseDurationInHours(createCourseRequest.getCourseDuration())
                .instructor(UserTestData.userData())
                .description(createCourseRequest.getDescription())
                .title(createCourseRequest.getTitle().trim())
                .prerequisite(!CollectionUtils.isEmpty(createCourseRequest.getPrerequisite()) ? String.join("~", createCourseRequest.getPrerequisite()).trim() : null)
                .courseOutcome(!CollectionUtils.isEmpty(createCourseRequest.getCourseOutcomes()) ? String.join("~", createCourseRequest.getCourseOutcomes()).trim() : null)
                .previewVideoURL(createCourseRequest.getPreviewVideoURL())
                .thumbnail(createCourseRequest.getThumbnailUrl())
                .documentVector(documentVector.toString())
                .courseStatus(CourseStatus.PUBLISHED)
                .certificateEnabled(true)
                .build();
        course.setCreationDate(new Date());
        course.setCreatedBy(UserTestData.userData().getId());

        return course;
    }
}
