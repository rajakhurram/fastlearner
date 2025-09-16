package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.course.CourseTagRepository;
import com.vinncorp.fast_learner.models.course.CourseTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTagService implements ICourseTagService{

    private final CourseTagRepository repo;

    @Override
    public void createAllCourseTags(List<CourseTag> courseTags) throws InternalServerException {
        log.info("Creating all course tags.");
        try {
            repo.saveAll(courseTags);
        } catch (Exception e) {
            log.error("Course tag"+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
            throw new InternalServerException("Course tag" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteAllCourseTagByTagIds(Long courseId) {
        log.info("Deleting all course tag using tag provided.");
        repo.deleteAllByCourseId(courseId);
    }
}
