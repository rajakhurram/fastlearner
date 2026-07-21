package com.vinncorp.fast_learner.services.super_admin;

import com.itextpdf.text.DocumentException;
import com.vinncorp.fast_learner.dtos.super_admin.*;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.ForbiddenException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.repositories.role.IRoleRepository;
import com.vinncorp.fast_learner.repositories.super_admin.ISuperAdminRepository;
import com.vinncorp.fast_learner.repositories.tansaction_history.TransactionHistoryRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.es_models.CourseContent;
import com.vinncorp.fast_learner.es_services.course_content.IESCourseContentService;
import com.vinncorp.fast_learner.services.email_template.IEmailService;
import com.vinncorp.fast_learner.util.coupon.CouponAmountUtils;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.template.InvoiceTemplate;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.UserRole;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminService implements ISuperAdminService {

    private final ISuperAdminRepository superAdminRepository;
    private final UserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final CourseRepository courseRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final JavaMailSender mailSender;
    private final IEmailService emailService;
    private final IESCourseContentService esCourseContentService;

    // ─────────────────────────────────────────────────────────────
    // Authorization guard — call at the top of every method
    // ─────────────────────────────────────────────────────────────

    private void validateSuperAdmin(String email) throws EntityNotFoundException, ForbiddenException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
        if (user.getRole() == null ||
                !user.getRole().getType().equalsIgnoreCase(UserRole.SUPER_ADMIN.name())) {
            throw new ForbiddenException("Access denied. Super admin role required.", "FORBIDDEN");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // User Stats
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<SuperAdminUserStatsDto> getUserStats(String callerEmail)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching super admin user stats");
        Tuple row = superAdminRepository.getUserStats();
        SuperAdminUserStatsDto stats = SuperAdminUserStatsDto.builder()
                .totalUsers(toLong(row.get("total_users")))
                .freeUsers(toLong(row.get("free_users")))
                .standardUsers(toLong(row.get("standard_users")))
                .premiumUsers(toLong(row.get("premium_users")))
                .totalPremiumUsers(toLong(row.get("total_premium_users")))
                .enterpriseUsers(toLong(row.get("enterprise_users")))
                .build();
        return new Message<SuperAdminUserStatsDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("User stats fetched successfully.")
                .setData(stats);
    }

    // ─────────────────────────────────────────────────────────────
    // User List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminUserListDto>> getUsers(String callerEmail, String search,
                                                          String planType, String subscriptionStatus,
                                                          Date dateFrom, Date dateTo,String accountType,
                                                          int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching users list for super admin - page: " + page + ", size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findAllUsersForAdmin(
                blankToNull(search), blankToNull(planType), blankToNull(subscriptionStatus), dateFrom, dateTo, blankToNull(accountType),  pageable);

        List<SuperAdminUserListDto> dtos = rawPage.getContent().stream()
                .map(this::mapToUserListDto)
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminUserListDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Users fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    private SuperAdminUserListDto mapToUserListDto(Tuple row) {
        Long id = toLong(row.get("id"));
        return SuperAdminUserListDto.builder()
                .rawId(id)
                .userId(formatUserId(id))
                .name(toString(row.get("full_name")))
                .email(toString(row.get("email")))
                .registeredDate((Date) row.get("created_date"))
                .planType(toString(row.get("plan_type")))
                .subscriptionStatus(resolveSubscriptionStatus(
                        toNullableBoolean(row.get("subscription_active")),
                        (Date) row.get("subscription_end_date")))
                .accountStatus(toBoolean(row.get("is_active")) ? "Active" : "Suspended")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // User Overview
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<SuperAdminUserOverviewDto> getUserOverview(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching overview for userId: " + userId);
        Tuple row = superAdminRepository.findUserOverview(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Date endDate = (Date) row.get("subscription_end_date");
        Integer daysUntilRenewal = null;
        if (endDate != null && endDate.after(new Date())) {
            LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            daysUntilRenewal = (int) ChronoUnit.DAYS.between(LocalDate.now(), end);
        }

        SuperAdminUserOverviewDto dto = SuperAdminUserOverviewDto.builder()
                .userId(formatUserId(toLong(row.get("id"))))
                .name(toString(row.get("full_name")))
                .email(toString(row.get("email")))
                .accountStatus(toBoolean(row.get("is_active")) ? "Active" : "Suspended")
                .subscriptionName(toString(row.get("subscription_name")))
                .planType(toString(row.get("plan_type")))
                .subscriptionStartDate((Date) row.get("subscription_start_date"))
                .subscriptionEndDate(endDate)
                .coursesEnrolled(toInt(row.get("courses_enrolled")))
                .totalSpent(toDouble(row.get("total_spent")))
                .hasRiskSignals(false)
                .daysUntilRenewal(daysUntilRenewal)
                .build();

        return new Message<SuperAdminUserOverviewDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("User overview fetched successfully.")
                .setData(dto);
    }

    // ─────────────────────────────────────────────────────────────
    // User Subscriptions
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<List<SuperAdminUserSubscriptionDto>> getUserSubscriptions(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching subscriptions for userId: " + userId);
        List<Tuple> rows = superAdminRepository.findUserSubscriptions(userId);
        List<SuperAdminUserSubscriptionDto> dtos = rows.stream()
                .map(row -> SuperAdminUserSubscriptionDto.builder()
                        .subscriptionId(toLong(row.get("subscription_id")))
                        .planName(toString(row.get("plan_name")))
                        .planType(toString(row.get("plan_type")))
                        .price(toDouble(row.get("price")))
                        .startDate((Date) row.get("start_date"))
                        .endDate((Date) row.get("end_date"))
                        .status(resolveSubscriptionStatus(
                                toNullableBoolean(row.get("subscription_active")),
                                (Date) row.get("end_date")))
                        .build())
                .collect(Collectors.toList());

        return new Message<List<SuperAdminUserSubscriptionDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("User subscriptions fetched successfully.")
                .setData(dtos);
    }

    // ─────────────────────────────────────────────────────────────
    // User Courses
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<List<SuperAdminUserCourseDto>> getUserCourses(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching courses for userId: " + userId);
        List<Tuple> rows = superAdminRepository.findUserCourses(userId);
        List<SuperAdminUserCourseDto> dtos = rows.stream()
                .map(row -> SuperAdminUserCourseDto.builder()
                        .courseId(toLong(row.get("course_id")))
                        .courseTitle(toString(row.get("course_title")))
                        .instructorName(toString(row.get("instructor_name")))
                        .progressPercent(toInt(row.get("progress_percent")))
                        .build())
                .collect(Collectors.toList());

        return new Message<List<SuperAdminUserCourseDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("User courses fetched successfully.")
                .setData(dtos);
    }

    // ─────────────────────────────────────────────────────────────
    // User Transactions
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<List<SuperAdminUserTransactionDto>> getUserTransactions(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching transactions for userId: " + userId);
        List<Tuple> rows = superAdminRepository.findUserTransactions(userId);
        List<SuperAdminUserTransactionDto> dtos = rows.stream()
                .map(row -> SuperAdminUserTransactionDto.builder()
                        .transactionId(toString(row.get("transaction_id")))
                        .amount(toDouble(row.get("amount")))
                        .transactionDate((Date) row.get("transaction_date"))
                        .status(resolvePaymentStatus(toString(row.get("subscription_status"))))
                        .build())
                .collect(Collectors.toList());

        return new Message<List<SuperAdminUserTransactionDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("User transactions fetched successfully.")
                .setData(dtos);
    }


    @Override
    public ResponseEntity<byte[]> exportUserCsv(
            String callerEmail, String search,
            String planType, String subscriptionStatus,
            Date dateFrom, Date dateTo, String accountType) {

        log.info("Exporting users CSV for super admin");
        List<Tuple> rows = superAdminRepository.findAllUsersForExport(
                blankToNull(search), blankToNull(planType),
                blankToNull(subscriptionStatus), dateFrom, dateTo, blankToNull(accountType));

        StringBuilder csv = new StringBuilder();

        csv.append("ID,Full Name,Email,Created Date,Active,Plan Type,Subscription Name,Subscription Active,Subscription End Date\n");

        for (Tuple row : rows) {
            csv.append(escapeCsv(toString(row.get("id")))).append(",");
            csv.append(escapeCsv(toString(row.get("full_name")))).append(",");
            csv.append(escapeCsv(toString(row.get("email")))).append(",");
            csv.append(row.get("created_date") != null ? row.get("created_date").toString() : "").append(",");
            csv.append(escapeCsv(toString(row.get("is_active")))).append(",");
            csv.append(escapeCsv(toString(row.get("plan_type")))).append(",");
            csv.append(escapeCsv(toString(row.get("subscription_name")))).append(",");
            csv.append(escapeCsv(toString(row.get("subscription_active")))).append(",");
            csv.append(row.get("subscription_end_date") != null ? row.get("subscription_end_date").toString() : "").append("\n");
        }

        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = "admin_users_export_" + date + "_" + java.util.UUID.randomUUID() + ".csv";
        byte[] csvBytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    // ─────────────────────────────────────────────────────────────
    // Payments List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminPaymentListDto>> getPayments(String callerEmail,
                                                                String search, String planType,
                                                                String status, String type,
                                                                Date dateFrom, Date dateTo,
                                                                int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching payments for super admin - page: " + page + ", size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findAllPaymentsForAdmin(
                blankToNull(search), blankToNull(planType),
                reversePaymentStatus(blankToNull(status)),
                blankToNull(type), dateFrom, dateTo, pageable);

        List<SuperAdminPaymentListDto> dtos = rawPage.getContent().stream()
                .map(row -> SuperAdminPaymentListDto.builder()
                        .rawId(toLong(row.get("id")))
                        .transactionId(toString(row.get("transaction_id")))
                        .userName(toString(row.get("user_name")))
                        .userEmail(toString(row.get("user_email")))
                        .planType(toString(row.get("plan_type")))
                        .amount(toDouble(row.get("amount")))
                        .method(null)
                        .date((Date) row.get("payment_date"))
                        .type(toString(row.get("type")))
                        .status(resolvePaymentStatus(toString(row.get("subscription_status"))))
                        .build())
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminPaymentListDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Payments fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    // ─────────────────────────────────────────────────────────────
    // Export Payments CSV
    // ─────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<byte[]> exportPaymentsCsv(String callerEmail,
                                                     String search, String planType,
                                                     String status, String type,
                                                     Date dateFrom, Date dateTo)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Exporting payments CSV for super admin");
        List<Tuple> rows = superAdminRepository.findAllPaymentsForExport(
                blankToNull(search), blankToNull(planType),
                reversePaymentStatus(blankToNull(status)),
                blankToNull(type), dateFrom, dateTo);

        StringBuilder csv = new StringBuilder();
        csv.append("Transaction ID,User Name,User Email,Plan Type,Amount,Type,Status,Date\n");
        for (Tuple row : rows) {
            csv.append(escapeCsv(toString(row.get("transaction_id")))).append(",");
            csv.append(escapeCsv(toString(row.get("user_name")))).append(",");
            csv.append(escapeCsv(toString(row.get("user_email")))).append(",");
            csv.append(escapeCsv(toString(row.get("plan_type")))).append(",");
            csv.append(toDouble(row.get("amount"))).append(",");
            csv.append(escapeCsv(toString(row.get("type")))).append(",");
            csv.append(escapeCsv(resolvePaymentStatus(toString(row.get("subscription_status"))))).append(",");
            csv.append(row.get("payment_date") != null ? row.get("payment_date").toString() : "").append("\n");
        }

        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = "admin_payments_export_" + date + "_" + java.util.UUID.randomUUID() + ".csv";
        byte[] csvBytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ─────────────────────────────────────────────────────────────
    // Enrollments List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminEnrollmentListDto>> getEnrollments(String callerEmail,
                                                                      String search, Long courseId,
                                                                      String status, String progress,
                                                                      int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching enrollments for super admin - page: " + page + ", size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findAllEnrollmentsForAdmin(
                blankToNull(search), courseId, blankToNull(status), blankToNull(progress), pageable);

        List<SuperAdminEnrollmentListDto> dtos = rawPage.getContent().stream()
                .map(row -> {
                    Long id = toLong(row.get("id"));
                    int progressPct = toInt(row.get("progress_percent"));
                    boolean isActive = toBoolean(row.get("is_active"));
                    String resolvedStatus = !isActive ? "Inactive"
                            : progressPct >= 100 ? "Completed"
                            : progressPct == 0   ? "Enrolled"
                            :                     "In Progress";
                    return SuperAdminEnrollmentListDto.builder()
                            .rawId(id)
                            .enrollmentId(formatEnrollmentId(id))
                            .studentName(toString(row.get("student_name")))
                            .studentEmail(toString(row.get("student_email")))
                            .courseId(toLong(row.get("course_id")))
                            .courseTitle(toString(row.get("course_title")))
                            .enrolledDate((Date) row.get("enrolled_date"))
                            .progressPercent(progressPct)
                            .status(resolvedStatus)
                            .build();
                })
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminEnrollmentListDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Enrollments fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    // ─────────────────────────────────────────────────────────────
    // Course Detail
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<SuperAdminCourseDetailDto> getCourseDetail(String callerEmail, Long courseId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching course detail for courseId: " + courseId);
        Tuple row = superAdminRepository.findCourseDetail(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        Long id = toLong(row.get("id"));
        Object avgRatingObj = row.get("avg_rating");
        Double avgRating = avgRatingObj != null ? ((Number) avgRatingObj).doubleValue() : null;

        SuperAdminCourseDetailDto dto = SuperAdminCourseDetailDto.builder()
                .rawId(id)
                .courseId(formatCourseId(id))
                .title(toString(row.get("title")))
                .description(toString(row.get("description")))
                .categoryName(toString(row.get("category_name")))
                .status(toString(row.get("status")))
                .price(row.get("price") != null ? toDouble(row.get("price")) : null)
                .courseType(toString(row.get("course_type")))
                .instructorId(toLong(row.get("instructor_id")))
                .instructorName(toString(row.get("instructor_name")))
                .instructorEmail(toString(row.get("instructor_email")))
                .studentCount(toLong(row.get("student_count")))
                .avgRating(avgRating)
                .createdDate((Date) row.get("created_date"))
                .build();

        return new Message<SuperAdminCourseDetailDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Course detail fetched successfully.")
                .setData(dto);
    }

    // ─────────────────────────────────────────────────────────────
    // Publish / Unpublish Course
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<String> publishCourse(String callerEmail, Long courseId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Publishing courseId: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        if (CourseStatus.PUBLISHED.equals(course.getCourseStatus())) {
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Course is already published.")
                    .setData(null);
        }

        course.setCourseStatus(CourseStatus.PUBLISHED);
        courseRepository.save(course);
        syncCourseStatusToES(courseId, CourseStatus.PUBLISHED.name());
        log.info("Course {} published successfully.", courseId);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Course published successfully.")
                .setData(null);
    }

    @Override
    public Message<String> unpublishCourse(String callerEmail, Long courseId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Unpublishing courseId: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        if (CourseStatus.UNPUBLISHED.equals(course.getCourseStatus())) {
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Course is already unpublished.")
                    .setData(null);
        }

        course.setCourseStatus(CourseStatus.UNPUBLISHED);
        courseRepository.save(course);
        syncCourseStatusToES(courseId, CourseStatus.UNPUBLISHED.name());
        log.info("Course {} unpublished successfully.", courseId);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Course unpublished successfully.")
                .setData(null);
    }

    // ─────────────────────────────────────────────────────────────
    // Courses List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminCourseListDto>> getCourses(String callerEmail, String search,
                                                              String status, String category,
                                                              String instructor, int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching courses list for super admin - page: " + page + ", size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findAllCoursesForAdmin(
                blankToNull(normalizeFormattedIdSearch(search, "CRS-")), blankToNull(status),
                blankToNull(category), blankToNull(instructor), pageable);

        List<SuperAdminCourseListDto> dtos = rawPage.getContent().stream()
                .map(row -> {
                    Long id = toLong(row.get("id"));
                    Object avgRatingObj = row.get("avg_rating");
                    Double avgRating = avgRatingObj != null ? ((Number) avgRatingObj).doubleValue() : null;
                    return SuperAdminCourseListDto.builder()
                            .rawId(id)
                            .courseId(formatCourseId(id))
                            .title(toString(row.get("title")))
                            .instructorId(toLong(row.get("instructor_id")))
                            .instructorName(toString(row.get("instructor_name")))
                            .categoryName(toString(row.get("category_name")))
                            .studentCount(toLong(row.get("student_count")))
                            .avgRating(avgRating)
                            .status(toString(row.get("status")))
                            .courseType(toString(row.get("course_type")))
                            .build();
                })
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminCourseListDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Courses fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    // ─────────────────────────────────────────────────────────────
    // Invoices List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminInvoiceDto>> getInvoices(String callerEmail, String search,
                                                            String type, String planType, String status,
                                                            Date dateFrom, Date dateTo,
                                                            int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching invoices list for super admin - page: " + page + ", size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findAllInvoicesForAdmin(
                blankToNull(search), blankToNull(type), blankToNull(planType),
                reversePaymentStatus(blankToNull(status)), dateFrom, dateTo, pageable);

        List<SuperAdminInvoiceDto> dtos = rawPage.getContent().stream()
                .map(row -> SuperAdminInvoiceDto.builder()
                        .rawId(toLong(row.get("id")))
                        .invoiceId(toString(row.get("invoice_id")))
                        .userName(toString(row.get("user_name")))
                        .userEmail(toString(row.get("user_email")))
                        .amount(toDouble(row.get("amount")))
                        .type(toString(row.get("type")))
                        .planName(toString(row.get("plan_name")))
                        .planType(toString(row.get("plan_type")))
                        .status(resolvePaymentStatus(toString(row.get("subscription_status"))))
                        .invoiceDate((Date) row.get("invoice_date"))
                        .build())
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminInvoiceDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Invoices fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    // ─────────────────────────────────────────────────────────────
    // Subscription Stats
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<SuperAdminSubscriptionStatsDto> getSubscriptionStats(String callerEmail)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching super admin subscription stats");
        Tuple row = superAdminRepository.getSubscriptionStats();
        SuperAdminSubscriptionStatsDto stats = SuperAdminSubscriptionStatsDto.builder()
                .activeSubscriptions(toLong(row.get("active_subscriptions")))
                .standardPlanCount(toLong(row.get("standard_plan_count")))
                .premiumPlanCount(toLong(row.get("premium_plan_count")))
                .enterprisePlanCount(toLong(row.get("enterprise_plan_count")))
                .build();
        return new Message<SuperAdminSubscriptionStatsDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Subscription stats fetched successfully.")
                .setData(stats);
    }

    // ─────────────────────────────────────────────────────────────
    // Subscriptions List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminSubscriptionListDto>> getSubscriptions(String callerEmail,
                                                                          String search, String planType,
                                                                          String status, String billingCycle,
                                                                          Date dateFrom, Date dateTo,
                                                                          int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching subscriptions list for super admin - page: " + page + ", size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findAllSubscriptionsForAdmin(
                blankToNull(search), blankToNull(planType), blankToNull(status),
                blankToNull(billingCycle), dateFrom, dateTo, pageable);

        List<SuperAdminSubscriptionListDto> dtos = rawPage.getContent().stream()
                .map(row -> {
                    Long id = toLong(row.get("id"));
                    String rowStatus = toString(row.get("status"));
                    Date renewalDate = "Active".equals(rowStatus) ? (Date) row.get("renewal_date") : null;
                    return SuperAdminSubscriptionListDto.builder()
                            .rawId(id)
                            .subscriptionId(formatSubscriptionId(id))
                            .userName(toString(row.get("user_name")))
                            .userEmail(toString(row.get("user_email")))
                            .planName(toString(row.get("plan_name")))
                            .billingCycle(toString(row.get("billing_cycle")))
                            .amount(toDouble(row.get("amount")))
                            .status(rowStatus)
                            .startDate((Date) row.get("start_date"))
                            .renewalDate(renewalDate)
                            .build();
                })
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminSubscriptionListDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Subscriptions fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    // ─────────────────────────────────────────────────────────────
    // Invoice Detail
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<SuperAdminInvoiceDetailDto> getInvoiceDetail(String callerEmail, Long invoiceId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching invoice detail for invoiceId: " + invoiceId);
        Tuple row = superAdminRepository.findInvoiceDetail(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        Long subscribedUserId = toLong(row.get("subscribed_user_id"));
        double subtotal = toDouble(row.get("subtotal"));
        double total = toDouble(row.get("total"));
        SuperAdminInvoiceDetailDto dto = SuperAdminInvoiceDetailDto.builder()
                .invoiceId(toString(row.get("invoice_id")))
                .userName(toString(row.get("user_name")))
                .userEmail(toString(row.get("user_email")))
                .plan(toString(row.get("plan_name")))
                .subtotal(subtotal)
                .total(total)
                .transactionId(toString(row.get("transaction_id")))
                .paymentStatus(resolvePaymentStatus(toString(row.get("subscription_status"))))
                .subscriptionDisplayId(subscribedUserId > 0 ? formatSubscriptionId(subscribedUserId) : null)
                .invoiceDate((Date) row.get("invoice_date"))
                .updatedDate((Date) row.get("updated_date"))
                .build();

        return new Message<SuperAdminInvoiceDetailDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Invoice detail fetched successfully.")
                .setData(dto);
    }

    // ─────────────────────────────────────────────────────────────
    // Subscription Invoices
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminInvoiceDto>> getSubscriptionInvoices(String callerEmail, Long subscriptionId,
                                                                         String search, String type,
                                                                         String planType, String status,
                                                                         String dateFrom, String dateTo,
                                                                         int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching invoices for subscriptionId: " + subscriptionId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findInvoicesBySubscriptionId(
                subscriptionId, blankToNull(search), blankToNull(type),
                blankToNull(planType), reversePaymentStatus(blankToNull(status)),
                blankToNull(dateFrom), blankToNull(dateTo), pageable);

        List<SuperAdminInvoiceDto> dtos = rawPage.getContent().stream()
                .map(row -> SuperAdminInvoiceDto.builder()
                        .rawId(toLong(row.get("id")))
                        .invoiceId(toString(row.get("invoice_id")))
                        .userName(toString(row.get("user_name")))
                        .userEmail(toString(row.get("user_email")))
                        .amount(toDouble(row.get("amount")))
                        .type(toString(row.get("type")))
                        .planName(toString(row.get("plan_name")))
                        .planType(toString(row.get("plan_type")))
                        .status(resolvePaymentStatus(toString(row.get("subscription_status"))))
                        .invoiceDate((Date) row.get("invoice_date"))
                        .build())
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminInvoiceDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Subscription invoices fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    // ─────────────────────────────────────────────────────────────
    // Download Invoice PDF
    // ─────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<byte[]> downloadInvoicePdf(String callerEmail, Long invoiceId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Generating invoice PDF for invoiceId: " + invoiceId);
        TransactionHistory th = transactionHistoryRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        try {
            String htmlContent = buildInvoiceHtml(th);
            ByteArrayOutputStream pdfStream = generatePdfFromHtml(htmlContent);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + invoiceId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Send Invoice Email
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<String> sendInvoiceToUser(String callerEmail, Long invoiceId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Sending invoice email for invoiceId: " + invoiceId);
        TransactionHistory th = transactionHistoryRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        try {
            String htmlContent = buildInvoiceHtml(th);
            ByteArrayOutputStream pdfStream = generatePdfFromHtml(htmlContent);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(th.getUser().getEmail());
            helper.setSubject("Your Invoice - INV-" + invoiceId);
            helper.setText("<p>Dear " + th.getUser().getFullName() + ",</p>" +
                    "<p>Please find your invoice attached.</p>" +
                    "<p>Thank you for choosing FastLearner.</p>", true);
            helper.addAttachment("invoice_" + invoiceId + ".pdf",
                    new ByteArrayResource(pdfStream.toByteArray()),
                    MediaType.APPLICATION_PDF_VALUE);
            mailSender.send(message);

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Invoice sent successfully to " + th.getUser().getEmail())
                    .setData(null);
        } catch (MessagingException | DocumentException | com.lowagie.text.DocumentException e) {
            throw new RuntimeException("Error sending invoice email", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Payout Stats
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<SuperAdminPayoutStatsDto> getPayoutStats(String callerEmail)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching super admin payout stats");
        Tuple row = superAdminRepository.getPayoutStats();
        SuperAdminPayoutStatsDto stats = SuperAdminPayoutStatsDto.builder()
                .totalEarnings(toDouble(row.get("total_earnings")))
                .pendingPayouts(toDouble(row.get("pending_payouts")))
                .totalPaidOut(toDouble(row.get("total_paid_out")))
                .thisMonthPayouts(toDouble(row.get("this_month_payouts")))
                .build();
        return new Message<SuperAdminPayoutStatsDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Payout stats fetched successfully.")
                .setData(stats);
    }

    // ─────────────────────────────────────────────────────────────
    // Payouts List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<Page<SuperAdminPayoutListDto>> getPayouts(String callerEmail,
                                                              String search, String status,
                                                              Date dateFrom, Date dateTo,
                                                              int page, int size)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching payouts list for super admin - page: " + page + ", size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> rawPage = superAdminRepository.findAllPayoutsForAdmin(
                blankToNull(search), blankToNull(status), dateFrom, dateTo, pageable);

        List<SuperAdminPayoutListDto> dtos = rawPage.getContent().stream()
                .map(row -> SuperAdminPayoutListDto.builder()
                        .instructorId(toLong(row.get("id")))
                        .instructorName(toString(row.get("full_name")))
                        .email(toString(row.get("email")))
                        .totalEarnings(toDouble(row.get("total_earnings")))
                        .paid(toDouble(row.get("paid")))
                        .pending(toDouble(row.get("pending")))
                        .lastPayoutDate((Date) row.get("last_payout_date"))
                        .status(toString(row.get("status")))
                        .build())
                .collect(Collectors.toList());

        return new Message<Page<SuperAdminPayoutListDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Payouts fetched successfully.")
                .setData(new PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
    }

    // ─────────────────────────────────────────────────────────────
    // Payout Detail
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<SuperAdminPayoutDetailDto> getPayoutDetail(String callerEmail, Long instructorId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching payout detail for instructorId: " + instructorId);

        Tuple summary = superAdminRepository.findPayoutDetailByInstructor(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("Instructor not found with id: " + instructorId));

        List<Tuple> courseRows = superAdminRepository.findPayoutCourseBreakdown(instructorId);
        List<Tuple> historyRows = superAdminRepository.findPayoutHistory(instructorId);

        double totalEarnings = toDouble(summary.get("total_earnings"));
        double paid = toDouble(summary.get("paid"));
        double remaining = totalEarnings - paid;

        List<SuperAdminPayoutCourseBreakdownDto> courseBreakdown = courseRows.stream()
                .map(row -> SuperAdminPayoutCourseBreakdownDto.builder()
                        .courseId(toLong(row.get("course_id")))
                        .courseTitle(toString(row.get("course_title")))
                        .totalEarned(toDouble(row.get("total_earned")))
                        .paid(toDouble(row.get("paid")))
                        .pending(toDouble(row.get("pending")))
                        .build())
                .collect(Collectors.toList());

        List<SuperAdminPayoutHistoryDto> payoutHistory = historyRows.stream()
                .map(row -> SuperAdminPayoutHistoryDto.builder()
                        .amount(toDouble(row.get("amount")))
                        .type(toString(row.get("type")))
                        .payoutDate((Date) row.get("payout_date"))
                        .status(toString(row.get("status")))
                        .build())
                .collect(Collectors.toList());

        SuperAdminPayoutDetailDto dto = SuperAdminPayoutDetailDto.builder()
                .instructorId(toLong(summary.get("id")))
                .instructorName(toString(summary.get("full_name")))
                .email(toString(summary.get("email")))
                .totalEarnings(totalEarnings)
                .paid(paid)
                .remaining(remaining)
                .courseBreakdown(courseBreakdown)
                .payoutHistory(payoutHistory)
                .build();

        return new Message<SuperAdminPayoutDetailDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Payout detail fetched successfully.")
                .setData(dto);
    }

    private String buildInvoiceHtml(TransactionHistory th) {
        double amount = th.getSubscriptionAmount() != null ? th.getSubscriptionAmount() : 0.0;
        double finalAmount = CouponAmountUtils.calculateNetPaid(amount, th.getCoupon());
        double discount = amount - finalAmount;
        boolean isPlan = th.getSubscription() != null;
        String planName = isPlan ? th.getSubscription().getName() : "Course Purchase";
        return InvoiceTemplate.generateInvoiceTemplate(
                planName,
                th.getUser().getEmail(),
                th.getUser().getFullName(),
                amount,
                discount,
                finalAmount,
                th.getCreationAt(),
                th.getAuthSubscriptionId(),
                isPlan
        );
    }

    private ByteArrayOutputStream generatePdfFromHtml(String htmlContent)
            throws DocumentException, com.lowagie.text.DocumentException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(pdfStream);
        return pdfStream;
    }

    // ─────────────────────────────────────────────────────────────
    // Settings — Admin Users
    // ─────────────────────────────────────────────────────────────

    @Override
    public Message<List<SuperAdminSettingsAdminDto>> getAdminUsers(String callerEmail)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching all admin users");
        List<Tuple> rows = superAdminRepository.findAllAdminUsers();
        List<SuperAdminSettingsAdminDto> dtos = rows.stream()
                .map(row -> SuperAdminSettingsAdminDto.builder()
                        .id(toLong(row.get("id")))
                        .name(toString(row.get("full_name")))
                        .email(toString(row.get("email")))
                        .role(toString(row.get("role")))
                        .status(toBoolean(row.get("is_active")) ? "Active" : "Inactive")
                        .lastLogin((Date) row.get("login_timestamp"))
                        .build())
                .collect(Collectors.toList());
        return new Message<List<SuperAdminSettingsAdminDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Admin users fetched successfully.")
                .setData(dtos);
    }

    @Override
    public Message<SuperAdminSettingsAdminDto> addAdmin(String callerEmail, String email)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Adding admin for email: " + email);

        if (email == null || email.isBlank()) {
            throw new EntityNotFoundException("Email must not be empty.");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No user found with email: " + email + ". Ask the user to register first."));

        com.vinncorp.fast_learner.models.role.Role superAdminRole = roleRepository.findById(3L)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin role not found in system."));

        user.setRole(superAdminRole);
        user.setActive(true);
        userRepository.save(user);

        emailService.sendEmail(email, "Fastlearner super admin invite","You have been added as Super Admin, login here : " + "https://fastlearner.ai/auth/sign-in",true);

        SuperAdminSettingsAdminDto dto = SuperAdminSettingsAdminDto.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .role(superAdminRole.getType())
                .status("Active")
                .lastLogin(user.getLoginTimestamp())
                .build();

        return new Message<SuperAdminSettingsAdminDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage(user.getFullName() + " has been granted Super Admin access.")
                .setData(dto);
    }

    @Override
    public Message<List<SuperAdminInstructorDto>> getInstructors(String callerEmail)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Fetching instructors list for super admin");
        List<Tuple> rows = superAdminRepository.findAllInstructors();
        List<SuperAdminInstructorDto> dtos = rows.stream()
                .map(row -> SuperAdminInstructorDto.builder()
                        .id(toLong(row.get("id")))
                        .name(toString(row.get("full_name")))
                        .build())
                .collect(Collectors.toList());
        return new Message<List<SuperAdminInstructorDto>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Instructors fetched successfully.")
                .setData(dtos);
    }

    @Override
    public Message<String> deactivateAdmin(String callerEmail, Long userId)
            throws EntityNotFoundException, ForbiddenException {
        validateSuperAdmin(callerEmail);
        log.info("Deactivating admin userId: " + userId);

        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Caller not found: " + callerEmail));
        if (caller.getId().equals(userId)) {
            throw new ForbiddenException("You cannot deactivate your own account.", "FORBIDDEN");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Role studentRole = roleRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        user.setRole(studentRole);
        userRepository.save(user);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage(user.getFullName() + " has been deactivated.")
                .setData(null);
    }

    public Message<String> inviteUser(String email, String link) throws EntityNotFoundException {


        emailService.sendEmail(email, "Fastlearner invite","join Fastlearner by clicking this link : " + link,true);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Invite has been sent to " + email )
                .setData(null);

    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private String resolveSubscriptionStatus(Boolean isActive, Date endDate) {
        if (isActive == null || !isActive) return "None";
        if (endDate != null && endDate.before(new Date())) return "Expired";
        return "Active";
    }

    /**
     * Maps {@code transaction_history.subscription_status} (and legacy payment enums) to SuperAdmin UI labels.
     */
    private String resolvePaymentStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "Unsuccessful";
        }
        return switch (rawStatus.toUpperCase()) {
            case "SUCCESS", "CONTINUE", "PAID" -> "Successful";
            case "PENDING", "UNPAID" -> "Pending";
            case "DISCONTINUE", "FAILED" -> "Failed";
            case "TRIALED" -> "Trialed";
            default -> rawStatus;
        };
    }

    /**
     * Reverse-maps UI payment-status filters to {@code subscription_status} values used in native queries.
     * "Successful" / "Completed" map to {@code SUCCESS}, which the SQL expands to IN ('SUCCESS','CONTINUE').
     */
    private String reversePaymentStatus(String uiStatus) {
        if (uiStatus == null) {
            return null;
        }
        return switch (uiStatus) {
            case "Successful", "Completed" -> "SUCCESS";
            case "Pending" -> "PENDING";
            case "Failed" -> "DISCONTINUE";
            case "Trialed" -> "PENDING";
            case "PAID" -> "SUCCESS";
            case "UNPAID" -> "PENDING";
            case "FAILED" -> "DISCONTINUE";
            case "TRIALED" -> "PENDING";
            default -> uiStatus;
        };
    }

    private String formatUserId(Long id) {
        return id == null ? "" : String.format("USR-%04d", id);
    }

    private String formatCourseId(Long id) {
        return id == null ? "" : String.format("CRS-%03d", id);
    }

    private String formatSubscriptionId(Long id) {
        return id == null ? "" : String.format("SUB-%03d", id);
    }

    private String formatEnrollmentId(Long id) {
        return id == null ? "" : String.format("ENR-%03d", id);
    }

    private void syncCourseStatusToES(Long courseId, String status) {
        try {
            CourseContent courseContent = esCourseContentService.getCourseContentById(courseId.toString());
            courseContent.setStatus(status);
            esCourseContentService.save(List.of(courseContent));
        } catch (Exception e) {
            log.error("Failed to sync course status to Elasticsearch for courseId: {}", courseId, e);
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Strips display prefixes (e.g. CRS-003) to numeric id for repository search. */
    private String normalizeFormattedIdSearch(String search, String prefix) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String trimmed = search.trim();
        if (prefix != null && trimmed.toUpperCase().startsWith(prefix.toUpperCase())) {
            String digits = trimmed.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                return String.valueOf(Long.parseLong(digits));
            }
        }
        return trimmed;
    }

    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long l) return l;
        if (obj instanceof BigInteger bi) return bi.longValue();
        if (obj instanceof Number n) return n.longValue();
        return Long.parseLong(obj.toString());
    }

    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number n) return n.intValue();
        return Integer.parseInt(obj.toString());
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof BigDecimal bd) return bd.doubleValue();
        if (obj instanceof Number n) return n.doubleValue();
        return Double.parseDouble(obj.toString());
    }

    private boolean toBoolean(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean b) return b;
        return Boolean.parseBoolean(obj.toString());
    }

    private Boolean toNullableBoolean(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Boolean b) return b;
        return Boolean.parseBoolean(obj.toString());
    }

    private String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
