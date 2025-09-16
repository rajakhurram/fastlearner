package com.vinncorp.fast_learner.services.premium_student;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Page;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.premium_student.PremiumStudentResponse;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface IPremiumStudentService {

    Message<Page<PremiumStudentResponse>> getPremiumStudentsWithFilters(String email, String search, Date startDate, Date endDate, Pageable pageable) throws EntityNotFoundException, BadRequestException;

    //Page<PremiumStudentResponse> getPremiumStudentsByDate(String email, Date selectedDate, Pageable pageable) throws EntityNotFoundException;

//    Page<PremiumStudentResponse> getPremiumStudentsByDateRange(String email, String search, Date startDate, Date endDate, Pageable pageable) throws EntityNotFoundException;


    byte[] getPremiumStudentsToExcel(String email, String search, Date startDate, Date endDate, Pageable pageable) throws EntityNotFoundException, IOException;
}
