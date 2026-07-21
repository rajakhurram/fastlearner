package com.vinncorp.fast_learner.controllers.purchase_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.purchase_course.StudentCoursePurchasedResponse;
import com.vinncorp.fast_learner.services.purchase_course.IPurchasedCourseService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.PURCHASED_COURSE)
@RequiredArgsConstructor
public class PurchasedCourseController {

    private final IPurchasedCourseService service;

    @GetMapping(APIUrls.PURCHASED_COURSE_BY_STUDENT)
    public ResponseEntity<Message<StudentCoursePurchasedResponse>> fetchAllPurchasedCoursesByStudents(
            @RequestParam("pageSize") int pageSize, @RequestParam("pageNo") int pageNo, Principal principal) throws EntityNotFoundException {
        var m = service.fetchPurchaseHistoryForStudent(pageNo, pageSize, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.DOWNLOAD_INVOICE_BY_STUDENT_AND_COURSE)
    public ResponseEntity<byte[]> downloadInvoiceByTransactionId(
            @Param("courseId") Long courseId,
            Principal principal) throws EntityNotFoundException {

        return service.downloadInvoiceByTransactionId(courseId, principal.getName());
    }
}
