package com.vinncorp.fast_learner.services.purchase_course;

import com.itextpdf.text.DocumentException;
import com.vinncorp.fast_learner.dtos.purchase_course.StudentCoursePurchased;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.purchase_course.StudentCoursePurchasedResponse;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.template.InvoiceTemplate;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.TimeUtil;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchasedCourseService implements IPurchasedCourseService {

    private final IEnrollmentService enrollmentService;
    private final IUserService userService;

    @Override
    public Message<StudentCoursePurchasedResponse> fetchPurchaseHistoryForStudent(int pageNo, int pageSize, String email) throws EntityNotFoundException {
        log.info("Fetching all purchase history for student.");

        User user = userService.findByEmail(email);

        Page<Tuple> data = enrollmentService.findAllEnrolledPremiumCoursesOfStudents(pageNo, pageSize, user.getId());

        if(data.isEmpty()) throw new EntityNotFoundException("No data found.");

        return new Message<StudentCoursePurchasedResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully fetched all purchased courses of a student.")
                .setData(StudentCoursePurchasedResponse.mapTo(data));
    }

    public StudentCoursePurchased fetchPurchasedHistoryForStudentByCourseId(Long courseId, String email) throws EntityNotFoundException {
        log.info("Fetch purchase history for course id: " + courseId);
        User user = userService.findByEmail(email);

        Tuple data = enrollmentService.findAllEnrolledPremiumCoursesOfStudentsByCourseId(courseId, user.getId());
        if(data == null) throw new EntityNotFoundException("No data found.");
        return StudentCoursePurchased.builder()
                .courseId(courseId)
                .courseTitle((String) data.get("title"))
                .studentEmail((String) data.get("email"))
                .studentName((String) data.get("full_name"))
                .enrolledAt(data.get("enrolled_date", Date.class))
                .originalPrice(data.get("original_price", Double.class))
                .discount(data.get("discount", Double.class))
                .price(data.get("price", Double.class))
                .build();
    }

    @Override
    public ResponseEntity<byte[]> downloadInvoiceByTransactionId(Long courseId, String email) throws EntityNotFoundException {
        StudentCoursePurchased studentCoursePurchased = fetchPurchasedHistoryForStudentByCourseId(courseId, email);
        boolean plan = false;
        try {
            String htmlContent = InvoiceTemplate.generateInvoiceTemplate(
                    studentCoursePurchased.getCourseTitle(),
                    studentCoursePurchased.getStudentEmail(),
                    studentCoursePurchased.getStudentName(),
                    studentCoursePurchased.getOriginalPrice(),
                    studentCoursePurchased.getDiscount(),
                    studentCoursePurchased.getPrice(),
                    studentCoursePurchased.getEnrolledAt(),
                    studentCoursePurchased.getStudentName(),
                    plan
            );

            // Generate PDF from HTML
            ByteArrayOutputStream pdfStream = generatePdfFromHtml(htmlContent);

            // Create the final ResponseEntity
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice_" + courseId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfStream.toByteArray()); // Ensure .body() is used here
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }

    private ByteArrayOutputStream generatePdfFromHtml(String htmlContent) throws DocumentException, com.lowagie.text.DocumentException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(pdfStream);
        return pdfStream;
    }

}
