package com.vinncorp.fast_learner.services.purchase_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.purchase_course.StudentCoursePurchasedResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.http.ResponseEntity;

public interface IPurchasedCourseService {
    Message<StudentCoursePurchasedResponse> fetchPurchaseHistoryForStudent(int pageNo, int pageSize, String email) throws EntityNotFoundException;

    ResponseEntity<byte[]> downloadInvoiceByTransactionId(Long courseId, String email) throws EntityNotFoundException;
}
