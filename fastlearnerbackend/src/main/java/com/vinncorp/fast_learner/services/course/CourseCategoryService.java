package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.repositories.course.CourseCategoryRepository;
import com.vinncorp.fast_learner.models.course.CourseCategory;
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
public class CourseCategoryService implements ICourseCategoryService{

    private final CourseCategoryRepository repo;

    @Override
    public Message<List<CourseCategory>> fetchAllCourseCategory() throws EntityNotFoundException {
        log.info("Fetching the course category.");

        List<CourseCategory> courseCategories = repo.findAllByIsActive(true);
        if (CollectionUtils.isEmpty(courseCategories)) {
            log.error("Course category is not found.");
            throw new EntityNotFoundException("Course category is not found.");
        }

        log.info("Course category is fetched successfully.");

        return new Message<List<CourseCategory>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Course category is fetched successfully.")
                .setData(courseCategories);
    }

    @Override
    public CourseCategory findById(Long categoryId) throws EntityNotFoundException {
        log.info("Fetching the course category by id: "+categoryId);

        return repo.findById(categoryId).orElseThrow(() -> {
            log.error("Course category not found.");
            return new EntityNotFoundException("Course category not found.");
        });
    }
}
