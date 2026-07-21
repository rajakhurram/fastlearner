package com.vinncorp.fast_learner.controllers.premium_student;

import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Page;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.premium_student.PremiumStudentResponse;
import com.vinncorp.fast_learner.services.premium_student.IPremiumStudentService;
import com.vinncorp.fast_learner.services.premium_student.PremiumStudentService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@RequestMapping(APIUrls.PREMIUM_STUDENTS)

@RestController
public class PremiumStudentController {
    private final IPremiumStudentService service;
    @GetMapping(APIUrls.GET_PREMIUM_STUDENTS)
    public ResponseEntity<Message<Page<PremiumStudentResponse>>> getAllEnrolledCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            Principal principal) throws EntityNotFoundException, BadRequestException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date start = Objects.nonNull(startDate) ? dateFormat.parse(startDate) : null;
        Date end = Objects.nonNull(endDate) ? dateFormat.parse(endDate) : null;
        var m = service.getPremiumStudentsWithFilters(principal.getName(),search, start, end, PageRequest.of(pageNo, pageSize));
        return ResponseEntity.status(m.getStatus()).body(m);
    }


//    @GetMapping(APIUrls.GET_PREMIUM_STUDENTS_BY_DATE)
//    public ResponseEntity<Page<PremiumStudentResponse>> getPremiumStudentsByDate(
//            @RequestParam String startDate,
//            @RequestParam String endDate,
//            @RequestParam int pageNo,
//            @RequestParam int pageSize,
//            Principal principal) throws EntityNotFoundException, ParseException {
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"); // Adjust the pattern if needed
//        Date start = dateFormat.parse(startDate);
//        Date end = dateFormat.parse(endDate);
//
//        Pageable pageable = PageRequest.of(pageNo, pageSize);
//        Page<PremiumStudentResponse> response = service.getPremiumStudentsByDateRange(principal.getName(), start, end, pageable);
//        return ResponseEntity.ok(response);
//    }


    @GetMapping(APIUrls.GET_PREMIUM_STUDENTS_EXPORT)
    public ResponseEntity<byte[]> exportPremiumStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) int pageNo,
            @RequestParam(required = false) int pageSize,
            Principal principal) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date start = Objects.nonNull(startDate) ? dateFormat.parse(startDate) : null;
            Date end = Objects.nonNull(endDate) ? dateFormat.parse(endDate) : null;

            byte[] excelData = service.getPremiumStudentsToExcel(principal.getName(),search, start, end,  Pageable.unpaged());

            // Prepare the response
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=premium_students.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
