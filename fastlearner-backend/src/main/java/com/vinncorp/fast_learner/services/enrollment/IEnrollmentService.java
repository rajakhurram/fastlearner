package com.vinncorp.fast_learner.services.enrollment;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.enrollment.EnrolledStudentDto;
import com.vinncorp.fast_learner.models.enrollment.Enrollment;
import com.vinncorp.fast_learner.response.enrollment.EnrolledCourseResponse;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IEnrollmentService {
    Message<String> enrolled(Long courseId, String email, boolean requestFromAPI) throws EntityNotFoundException, InternalServerException, BadRequestException;

    boolean isEnrolled(Long courseId, String email);

    Message<EnrolledCourseResponse> getEnrolledCourseByUserId(Integer sortBy, String courseTitle, int pageNo, int pageSize, String email)
            throws EntityNotFoundException, BadRequestException;

    EnrolledStudentDto totalNoOfEnrolledStudent(String period, Long instructorId);

    long totalNoOfEnrolledStudent(Long courseId);

    List<Long> findRecommendedCoursesIDs(Long courseId) throws EntityNotFoundException;
    void deleteEnrollmentByCourseIdAndStudentId(Long courseId, String email);

    Page<Tuple> findAllEnrolledPremiumCoursesOfStudents(int pageNo, int pageSize, Long studentId) throws EntityNotFoundException;

    Tuple findAllEnrolledPremiumCoursesOfStudentsByCourseId(Long courseId, Long studentId) throws EntityNotFoundException;
}
