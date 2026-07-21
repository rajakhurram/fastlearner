package com.vinncorp.fast_learner.controllers.super_admin;

import com.vinncorp.fast_learner.dtos.super_admin.*;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.ForbiddenException;
import com.vinncorp.fast_learner.services.super_admin.ISuperAdminService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(APIUrls.SUPER_ADMIN_API)
@RequiredArgsConstructor
@Validated
public class SuperAdminController {

    private final ISuperAdminService superAdminService;

    @GetMapping(APIUrls.SUPER_ADMIN_GET_USER_STATS)
    public ResponseEntity<Message<SuperAdminUserStatsDto>> getUserStats(Principal principal)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getUserStats(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_USERS)
    public ResponseEntity<Message<Page<SuperAdminUserListDto>>> getUsers(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String subscriptionStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getUsers(
                principal.getName(), search, planType, subscriptionStatus,
                parseDate(dateFrom), parseDate(dateTo), accountStatus,page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_USER_OVERVIEW)
    public ResponseEntity<Message<SuperAdminUserOverviewDto>> getUserOverview(
            Principal principal,
            @PathVariable Long userId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getUserOverview(principal.getName(), userId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_USER_SUBSCRIPTIONS)
    public ResponseEntity<Message<List<SuperAdminUserSubscriptionDto>>> getUserSubscriptions(
            Principal principal,
            @PathVariable Long userId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getUserSubscriptions(principal.getName(), userId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_USER_COURSES)
    public ResponseEntity<Message<List<SuperAdminUserCourseDto>>> getUserCourses(
            Principal principal,
            @PathVariable Long userId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getUserCourses(principal.getName(), userId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_USERS_CSV )
    public ResponseEntity<byte[]> exportUsersCsv(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String subscriptionStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String accountStatus
    ) throws EntityNotFoundException, ForbiddenException {
        return superAdminService.exportUserCsv(
                principal.getName(), search, planType, subscriptionStatus,
                parseDate(dateFrom), parseDate(dateTo), accountStatus
        );
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_USER_TRANSACTIONS)
    public ResponseEntity<Message<List<SuperAdminUserTransactionDto>>> getUserTransactions(
            Principal principal,
            @PathVariable Long userId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getUserTransactions(principal.getName(), userId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_SUBSCRIPTION_STATS)
    public ResponseEntity<Message<SuperAdminSubscriptionStatsDto>> getSubscriptionStats(Principal principal)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getSubscriptionStats(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_SUBSCRIPTIONS)
    public ResponseEntity<Message<Page<SuperAdminSubscriptionListDto>>> getSubscriptions(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String billingCycle,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getSubscriptions(
                principal.getName(), search, planType, status, billingCycle,
                parseDate(dateFrom), parseDate(dateTo), page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_INVOICE_DETAIL)
    public ResponseEntity<Message<SuperAdminInvoiceDetailDto>> getInvoiceDetail(
            Principal principal,
            @PathVariable Long invoiceId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getInvoiceDetail(principal.getName(), invoiceId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_SUBSCRIPTION_INVOICES)
    public ResponseEntity<Message<Page<SuperAdminInvoiceDto>>> getSubscriptionInvoices(
            Principal principal,
            @PathVariable Long subscriptionId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getSubscriptionInvoices(
                principal.getName(), subscriptionId,
                search, type, planType, status,
                dateFrom, dateTo, page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_DOWNLOAD_INVOICE_PDF)
    public ResponseEntity<byte[]> downloadInvoicePdf(
            Principal principal,
            @PathVariable Long invoiceId)
            throws EntityNotFoundException, ForbiddenException {
        return superAdminService.downloadInvoicePdf(principal.getName(), invoiceId);
    }

    @PostMapping(APIUrls.SUPER_ADMIN_SEND_INVOICE_EMAIL)
    public ResponseEntity<Message<String>> sendInvoiceToUser(
            Principal principal,
            @PathVariable Long invoiceId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.sendInvoiceToUser(principal.getName(), invoiceId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_PAYMENTS)
    public ResponseEntity<Message<Page<SuperAdminPaymentListDto>>> getPayments(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getPayments(
                principal.getName(), search, planType, status, type,
                parseDate(dateFrom), parseDate(dateTo), page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.SUPER_ADMIN_USER_INVITE)
    public ResponseEntity<Message<String>> userInvite(
            @Valid @NotBlank(message = "Email is required.") @Email(message = "Please provide valid email.") @RequestParam String email,
            @RequestParam String link) throws EntityNotFoundException {
        var m = superAdminService.inviteUser(email, link);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_EXPORT_PAYMENTS_CSV)
    public ResponseEntity<byte[]> exportPaymentsCsv(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo)
            throws EntityNotFoundException, ForbiddenException {
        return superAdminService.exportPaymentsCsv(
                principal.getName(), search, planType, status, type,
                parseDate(dateFrom), parseDate(dateTo));
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_ENROLLMENTS)
    public ResponseEntity<Message<Page<SuperAdminEnrollmentListDto>>> getEnrollments(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String progress,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getEnrollments(
                principal.getName(), search, courseId, status, progress, page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_COURSE_DETAIL)
    public ResponseEntity<Message<SuperAdminCourseDetailDto>> getCourseDetail(
            Principal principal,
            @PathVariable Long courseId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getCourseDetail(principal.getName(), courseId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PatchMapping(APIUrls.SUPER_ADMIN_PUBLISH_COURSE)
    public ResponseEntity<Message<String>> publishCourse(
            Principal principal,
            @PathVariable Long courseId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.publishCourse(principal.getName(), courseId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PatchMapping(APIUrls.SUPER_ADMIN_UNPUBLISH_COURSE)
    public ResponseEntity<Message<String>> unpublishCourse(
            Principal principal,
            @PathVariable Long courseId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.unpublishCourse(principal.getName(), courseId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_COURSES)
    public ResponseEntity<Message<Page<SuperAdminCourseListDto>>> getCourses(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String instructor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getCourses(
                principal.getName(), search, status, category, instructor, page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_INVOICES)
    public ResponseEntity<Message<Page<SuperAdminInvoiceDto>>> getInvoices(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getInvoices(
                principal.getName(), search, type, planType, status,
                parseDate(dateFrom), parseDate(dateTo), page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    // ── Instructors ───────────────────────────────────────────────

    @GetMapping(APIUrls.SUPER_ADMIN_GET_INSTRUCTORS)
    public ResponseEntity<Message<List<SuperAdminInstructorDto>>> getInstructors(Principal principal)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getInstructors(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    // ── Settings ──────────────────────────────────────────────────

    @GetMapping(APIUrls.SUPER_ADMIN_GET_ADMIN_USERS)
    public ResponseEntity<Message<List<SuperAdminSettingsAdminDto>>> getAdminUsers(Principal principal)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getAdminUsers(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.SUPER_ADMIN_ADD_ADMIN)
    public ResponseEntity<Message<SuperAdminSettingsAdminDto>> addAdmin(
            Principal principal,
            @RequestBody AddAdminRequest request)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.addAdmin(principal.getName(), request.getEmail());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PatchMapping(APIUrls.SUPER_ADMIN_DEACTIVATE_ADMIN)
    public ResponseEntity<Message<String>> deactivateAdmin(
            Principal principal,
            @PathVariable Long userId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.deactivateAdmin(principal.getName(), userId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    // ── Payouts ───────────────────────────────────────────────────

    @GetMapping(APIUrls.SUPER_ADMIN_GET_PAYOUT_STATS)
    public ResponseEntity<Message<SuperAdminPayoutStatsDto>> getPayoutStats(Principal principal)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getPayoutStats(principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_PAYOUTS)
    public ResponseEntity<Message<Page<SuperAdminPayoutListDto>>> getPayouts(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getPayouts(
                principal.getName(), search, status,
                parseDate(dateFrom), parseDate(dateTo), page, size);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.SUPER_ADMIN_GET_PAYOUT_DETAIL)
    public ResponseEntity<Message<SuperAdminPayoutDetailDto>> getPayoutDetail(
            Principal principal,
            @PathVariable Long instructorId)
            throws EntityNotFoundException, ForbiddenException {
        var m = superAdminService.getPayoutDetail(principal.getName(), instructorId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
