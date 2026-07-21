package com.vinncorp.fast_learner.services.super_admin;

import com.vinncorp.fast_learner.dtos.super_admin.*;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.ForbiddenException;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Date;
import java.util.List;

public interface ISuperAdminService {

    Message<SuperAdminUserStatsDto> getUserStats(String callerEmail)
            throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminUserListDto>> getUsers(String callerEmail,
                                                   String search,
                                                   String planType,
                                                   String subscriptionStatus,
                                                   Date dateFrom,
                                                   Date dateTo,
                                                   String accountType,
                                                   int page,
                                                   int size)
            throws EntityNotFoundException, ForbiddenException;

    Message<SuperAdminUserOverviewDto> getUserOverview(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException;

    Message<List<SuperAdminUserSubscriptionDto>> getUserSubscriptions(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException;

    Message<List<SuperAdminUserCourseDto>> getUserCourses(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException;

    Message<List<SuperAdminUserTransactionDto>> getUserTransactions(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException;

    Message<SuperAdminCourseDetailDto> getCourseDetail(String callerEmail, Long courseId)
            throws EntityNotFoundException, ForbiddenException;

    Message<String> publishCourse(String callerEmail, Long courseId)
            throws EntityNotFoundException, ForbiddenException;

    Message<String> unpublishCourse(String callerEmail, Long courseId)
            throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminPaymentListDto>> getPayments(String callerEmail,
                                                         String search,
                                                         String planType,
                                                         String status,
                                                         String type,
                                                         Date dateFrom,
                                                         Date dateTo,
                                                         int page,
                                                         int size)
            throws EntityNotFoundException, ForbiddenException;

    ResponseEntity<byte[]> exportPaymentsCsv(String callerEmail,
                                              String search,
                                              String planType,
                                              String status,
                                              String type,
                                              Date dateFrom,
                                              Date dateTo)
            throws EntityNotFoundException, ForbiddenException;

    ResponseEntity<byte[]> exportUserCsv(
            String callerEmail,
            String search,
            String planType,
            String subscriptionStatus,
            Date dateFrom,
            Date dateTo,
            String accountType
    ) throws EntityNotFoundException, ForbiddenException; ;

    Message<String> inviteUser(String email, String link) throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminEnrollmentListDto>> getEnrollments(String callerEmail,
                                                               String search,
                                                               Long courseId,
                                                               String status,
                                                               String progress,
                                                               int page,
                                                               int size)
            throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminCourseListDto>> getCourses(String callerEmail,
                                                       String search,
                                                       String status,
                                                       String category,
                                                       String instructor,
                                                       int page,
                                                       int size)
            throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminInvoiceDto>> getInvoices(String callerEmail,
                                                     String search,
                                                     String type,
                                                     String planType,
                                                     String status,
                                                     Date dateFrom,
                                                     Date dateTo,
                                                     int page,
                                                     int size)
            throws EntityNotFoundException, ForbiddenException;

    Message<SuperAdminSubscriptionStatsDto> getSubscriptionStats(String callerEmail)
            throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminSubscriptionListDto>> getSubscriptions(String callerEmail,
                                                                   String search,
                                                                   String planType,
                                                                   String status,
                                                                   String billingCycle,
                                                                   Date dateFrom,
                                                                   Date dateTo,
                                                                   int page,
                                                                   int size)
            throws EntityNotFoundException, ForbiddenException;

    Message<SuperAdminInvoiceDetailDto> getInvoiceDetail(String callerEmail, Long invoiceId)
            throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminInvoiceDto>> getSubscriptionInvoices(String callerEmail, Long subscriptionId,
                                                                  String search, String type,
                                                                  String planType, String status,
                                                                  String dateFrom, String dateTo,
                                                                  int page, int size)
            throws EntityNotFoundException, ForbiddenException;

    ResponseEntity<byte[]> downloadInvoicePdf(String callerEmail, Long invoiceId)
            throws EntityNotFoundException, ForbiddenException;

    Message<String> sendInvoiceToUser(String callerEmail, Long invoiceId)
            throws EntityNotFoundException, ForbiddenException;

    Message<SuperAdminPayoutStatsDto> getPayoutStats(String callerEmail)
            throws EntityNotFoundException, ForbiddenException;

    Message<Page<SuperAdminPayoutListDto>> getPayouts(String callerEmail,
                                                       String search,
                                                       String status,
                                                       Date dateFrom,
                                                       Date dateTo,
                                                       int page,
                                                       int size)
            throws EntityNotFoundException, ForbiddenException;

    Message<SuperAdminPayoutDetailDto> getPayoutDetail(String callerEmail, Long instructorId)
            throws EntityNotFoundException, ForbiddenException;

    Message<List<SuperAdminSettingsAdminDto>> getAdminUsers(String callerEmail)
            throws EntityNotFoundException, ForbiddenException;

    Message<SuperAdminSettingsAdminDto> addAdmin(String callerEmail, String email)
            throws EntityNotFoundException, ForbiddenException;

    Message<String> deactivateAdmin(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException;

    Message<List<SuperAdminInstructorDto>> getInstructors(String callerEmail)
            throws EntityNotFoundException, ForbiddenException;
}
