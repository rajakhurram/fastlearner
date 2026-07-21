package com.vinncorp.fast_learner.services.favourite_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.favourite_course.FavouriteCourse;
import com.vinncorp.fast_learner.repositories.favourite_course.FavouriteCourseRepository;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.dtos.favourite_course.FavouriteCourseDetail;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.response.favourite_course.FavouriteCourseResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavouriteCourseService implements IFavouriteCourseService{

    private final FavouriteCourseRepository repo;
    private final IUserService userService;
    private final ICourseService courseService;
    private final RabbitMQProducer rabbitMQProducer;
    private final ICourseUrlService courseUrlService;

    @Override
    public Message<String> create(Long courseId, String email) throws EntityNotFoundException, InternalServerException {
        log.info("Creating favourite course for user: " + email);

        User user = userService.findByEmail(email);
        Course course = courseService.findById(courseId);
        Optional<FavouriteCourse> savedFavouriteCourse = repo.findByCourseIdAndCreatedBy(courseId, user.getId());
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);

        if (savedFavouriteCourse.isPresent()) {
            repo.delete(savedFavouriteCourse.get());
            log.info("Deleted favourite course.");

            if (!user.getId().equals(course.getCreatedBy())) {
                rabbitMQProducer.sendMessage(
                        course.getTitle(),
                        "student/course-details/" + courseUrl.getUrl(),
                        user.getEmail(),
                        course.getCreatedBy(),
                        course.getContentType(),
                        NotificationContentType.TEXT,
                        NotificationType.COURSE_NOT_FAVOURITE,
                        course.getId()
                );
            }

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Course is marked as not favourite.")
                    .setData("Course is marked as not favourite.");
        }

        try {
            FavouriteCourse favouriteCourse = FavouriteCourse.builder()
                    .course(course)
                    .isActive(true)
                    .build();
            favouriteCourse.setCreationDate(new Date());
            favouriteCourse.setCreatedBy(user.getId());
            repo.save(favouriteCourse);
            log.info("Course is marked as favourite.");

            if (!user.getId().equals(course.getCreatedBy())) {
                rabbitMQProducer.sendMessage(
                        course.getTitle(),
                        "student/course-details/" + courseUrl.getUrl(),
                        user.getEmail(),
                        course.getCreatedBy(),
                        course.getContentType(),
                        NotificationContentType.TEXT,
                        NotificationType.COURSE_FAVOURITE,
                        course.getId()
                );
            }

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Course is marked as favourite.")
                    .setData("Course is marked as favourite.");
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("Favourite course " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public Message<FavouriteCourseResponse> getAllFavouriteCourses(String courseTitle, int pageSize, int pageNo, String email) throws EntityNotFoundException {
        log.info("Fetching favourite courses.");
        User user = userService.findByEmail(email);
        Page<Tuple> pagedData = repo.findFavouriteCoursesByTitle(
                courseTitle != null ? "%" + courseTitle + "%" : null,
                user.getId(), PageRequest.of(pageNo, pageSize));
        if (pagedData.isEmpty()) {
            throw new EntityNotFoundException("No favourite courses present for the user: "+ email);
        }
        List<FavouriteCourseDetail> favouriteCourseDetail = FavouriteCourseDetail.from(pagedData);
        FavouriteCourseResponse favouriteCourseResponse = FavouriteCourseResponse.builder()
                .favouriteCourses(favouriteCourseDetail)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(pagedData.getTotalPages())
                .totalElements(pagedData.getTotalElements())
                .build();

        return new Message<FavouriteCourseResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Favourite courses fetched successfully.")
                .setData(favouriteCourseResponse);
    }
}
