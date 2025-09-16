package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.repositories.course.CourseLevelRepository;
import com.vinncorp.fast_learner.models.course.CourseLevel;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseLevelService implements ICourseLevelService{

    private final CourseLevelRepository repo;

    @Override
    public Message<List<CourseLevel>> fetchAllCourseLevel(String email) throws EntityNotFoundException {
        log.info("Fetching all course level.");
        List<CourseLevel> courseLevels = repo.findByIsActive(true);
        if (CollectionUtils.isEmpty(courseLevels)) {
            log.error("No course level is found.");
            throw new EntityNotFoundException("No course level is found.");
        }

        log.info("Course level fetched successfully.");
        return new Message<List<CourseLevel>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Course level fetched successfully.")
                .setData(courseLevels);
    }

    @Override
    public CourseLevel findById(Long courseLevelId) throws EntityNotFoundException {
        log.info("Fetching all course level by id: "+courseLevelId);
        return repo.findById(courseLevelId).orElseThrow( () -> {
            log.error("Course level is not found.");
            return new EntityNotFoundException("Course level is not found.");
        });
    }
}
