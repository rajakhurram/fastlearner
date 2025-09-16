package com.vinncorp.fast_learner.mock.certificate;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.course.CourseUrlTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.certificate.Certificate;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.certificate.CertificateRepository;
import com.vinncorp.fast_learner.controllers.youtube_video.user.CertificateResponse;
import com.vinncorp.fast_learner.services.certificate.CertificateService;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.course.course_review.CourseReviewService;
import com.vinncorp.fast_learner.services.user.UserCourseProgressService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CertificateServiceTest {

    @Mock
    private CertificateRepository repo;

    @Mock
    private ICourseUrlService courseUrlService;

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @Mock
    private UserCourseProgressService userCourseProgressService;

    @Mock
    private CourseReviewService courseReviewService;

    @InjectMocks
    private CertificateService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should throw BadRequestException when course certificate is not enabled")
    public void generateCertificate_whenCourseCertificateNotEnabled_thenThrowsBadRequestException() {
        Long courseId = 1L;
        String email = "test@example.com";

        when(courseService.isExistCertificateEnabledByCourseId(courseId)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.generateCertificate(courseId, email));

        verify(courseService, times(1)).isExistCertificateEnabledByCourseId(courseId);
    }

    @Test
    @DisplayName("Should throw BadRequestException when course progress is below minimum required")
    public void generateCertificate_whenCourseProgressBelowMinimum_thenThrowsBadRequestException() throws Exception{
        Long courseId = 1L;
        String email = "test@example.com";
        User user = mock(User.class);
        Message<Double> courseProgress = new Message<Double>().setData(30.0);

        when(courseService.isExistCertificateEnabledByCourseId(courseId)).thenReturn(true);
        when(userService.findByEmail(email)).thenReturn(user);
        when(userCourseProgressService.fetchCourseProgress(courseId, email)).thenReturn(courseProgress);

        assertThrows(BadRequestException.class, () -> service.generateCertificate(courseId, email));

        verify(courseService, times(1)).isExistCertificateEnabledByCourseId(courseId);
        verify(userService, times(1)).findByEmail(email);
        verify(userCourseProgressService, times(1)).fetchCourseProgress(courseId, email);
        verify(repo, never()).findByCourseIdAndStudentId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should return CertificateResponse when certificate exists")
    public void generateCertificate_whenCertificateExists_thenReturnsCertificateResponse() throws Exception {
        // Prepare data
        Certificate certificate = CertificateTestData.certificate();
        Tuple allCoursesReviews = mock(Tuple.class);
        Tuple courseReviews = mock(Tuple.class);
        User user = UserTestData.userData();
        Message<Double> courseProgress = new Message<Double>().setData(100.0);

        // Mock courseReviewService responses
        when(courseService.isExistCertificateEnabledByCourseId(1L)).thenReturn(true);
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userCourseProgressService.fetchCourseProgress(1L, user.getEmail())).thenReturn(courseProgress);
        when(repo.findByCourseIdAndStudentId(1L, user.getId())).thenReturn(certificate);

        when(courseReviewService.fetchAllReviewsForAnInstructorCourses(certificate.getInstructorId()))
                .thenReturn(allCoursesReviews);
        when(courseReviewService.fetchAllReviewOfACourse(certificate.getCourseId()))
                .thenReturn(courseReviews);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(certificate.getCourseId(), GenericStatus.ACTIVE))
                .thenReturn(CourseUrlTestData.courseUrl());

        // Mock Tuple responses
        when(allCoursesReviews.get("total_reviewers")).thenReturn(100L);
        when(allCoursesReviews.get("avg_reviews")).thenReturn(4.5);
        when(courseReviews.get("total_reviewers")).thenReturn(50L);
        when(courseReviews.get("avg_reviews")).thenReturn(4.0);

        // Call the method
        CertificateResponse response = service.generateCertificate(1L, user.getEmail()).getData();

        // Assertions
        assertNotEquals(response, null);
        assertEquals(response.getInstructorReviewers(), 100L);
        assertEquals(response.getInstructorAvgReviews(), 4.5);
        assertEquals(response.getCourseReviewers(), 50L);
        assertEquals(response.getCourseAvgReviews(), 4.0);

        // Verify interactions
        verify(courseReviewService, times(1)).fetchAllReviewsForAnInstructorCourses(certificate.getInstructorId());
        verify(courseReviewService, times(1)).fetchAllReviewOfACourse(certificate.getCourseId());
    }

    @Test
    @DisplayName("Should return verified message when certificate is valid")
    public void verifyCertificate_whenCertificateIsValid_thenReturnsVerifiedMessage() throws BadRequestException {
        String certificateId = UUID.randomUUID().toString();
        Certificate certificate = mock(Certificate.class);

        when(repo.findByUuid(certificateId)).thenReturn(Optional.of(certificate));

        Message<String> response = service.verifyCertificate(certificateId);

        assertNotEquals(response, null);
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertEquals(response.getData(), "Certificate is verified successfully.");
        verify(repo, times(1)).findByUuid(certificateId);
    }

    @Test
    @DisplayName("Should throw BadRequestException when certificate is not valid")
    public void verifyCertificate_whenCertificateIsNotValid_thenThrowsBadRequestException() {
        String certificateId = UUID.randomUUID().toString();

        when(repo.findByUuid(certificateId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.verifyCertificate(certificateId));

        verify(repo, times(1)).findByUuid(certificateId);
    }

    @Test
    @DisplayName("Should return certificate image when certificate exists")
    public void downloadCertificate_whenCertificateExists_thenReturnsCertificateImage() throws Exception {
        Long courseId = 1L;
        String uuid = UUID.randomUUID().toString();
        Boolean isDownloadable = true;
        Principal principal = mock(Principal.class);
        Certificate certificate = CertificateTestData.certificate();

        when(repo.findByUuid(uuid)).thenReturn(Optional.of(certificate));

        ResponseEntity<byte[]> response = service.downloadCertificate(courseId, isDownloadable, uuid, principal);

        assertNotEquals(response, null);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getHeaders().getContentType(), MediaType.IMAGE_PNG);
        verify(repo, times(1)).findByUuid(uuid);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when certificate is not valid")
    public void downloadCertificate_whenCertificateIsNotValid_thenThrowsEntityNotFoundException() {
        Long courseId = 1L;
        String uuid = UUID.randomUUID().toString();
        Boolean isDownloadable = true;
        Principal principal = mock(Principal.class);

        when(repo.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.downloadCertificate(courseId, isDownloadable, uuid, principal));

        verify(repo, times(1)).findByUuid(uuid);
    }
}
