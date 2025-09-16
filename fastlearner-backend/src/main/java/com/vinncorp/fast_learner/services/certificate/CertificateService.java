package com.vinncorp.fast_learner.services.certificate;

import com.vinncorp.fast_learner.dtos.course.CourseDetailForCertificate;
import com.vinncorp.fast_learner.repositories.certificate.CertificateRepository;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.certificate.Certificate;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserCourseCompletion;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.controllers.youtube_video.user.CertificateResponse;
import com.vinncorp.fast_learner.services.course.course_review.ICourseReviewService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.notification.IMilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService implements ICertificateService{

    @Value("${frontend.domain.url}")
    private String FRONTEND_DOMAIN_URL;


    private final CertificateRepository repo;
    private final IUserService userService;
    private final IUserCourseProgressService userCourseProgressService;
    private final ICourseReviewService courseReviewService;
    private final ICourseService courseService;
    private final IUserProfileService userProfileService;
    private final IMilestoneAchievementNotificationService milestoneAchievementNotificationService;
    private final ICourseUrlService courseUrlService;

    @Override
    public Message<CertificateResponse> generateCertificate(Long courseId, String email) throws EntityNotFoundException, BadRequestException {
        log.info("Fetching certificate data...");
        if (!courseService.isExistCertificateEnabledByCourseId(courseId)) {
            throw new BadRequestException("The course has not provided a certificate for students.");
        }
        User user = userService.findByEmail(email);
        var percentage = userCourseProgressService.fetchCourseProgress(courseId, email);
        if (percentage.getData() <= UserCourseCompletion.MINIMUM_TOPIC_COMPLETION_PERCENTAGE) {
            throw new BadRequestException("The course progress should be greater than " +
                    UserCourseCompletion.MINIMUM_TOPIC_COMPLETION_PERCENTAGE+"% for generating certificate.");
        }
        Certificate certificate = repo.findByCourseIdAndStudentId(courseId, user.getId());
        if (Objects.isNull(certificate)) {
            // Generate and save the certificate into db
            UserProfile userProfile = userProfileService.getUserProfile(user.getId());
            certificate = generateAndSaveCertificate(courseId, user.getId(), user.getFullName(), userProfile.getProfilePicture());

            // Send course completion and certificate completion notification trigger
            Course course = courseService.findById(courseId);
            CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
            milestoneAchievementNotificationService.notifyCertificationCompletion(user.getFullName(), certificate.getCourseTitle(), courseUrl.getUrl(), certificate.getInstructorId(), course, user.getEmail());
            milestoneAchievementNotificationService.notifyToUserCertificateAwarded(certificate.getCourseTitle(), courseUrl.getId(), certificate.getInstructorId(), user.getId(), course);
        }

        CertificateResponse certificateResponse = mappedTo(certificate, false);

        return new Message<CertificateResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Certificate data is successfully fetched.")
                .setData(certificateResponse);
    }

    @Override
    public Message<String> verifyCertificate(String certificateId) throws BadRequestException {
        log.info("Verifying the certificate of a user.");

        if(Objects.isNull(certificateId)){throw new BadRequestException("Please enter a valid url");}

        Certificate certificate = repo.findByUuid(certificateId).orElseThrow(() -> new BadRequestException("Certificate is not valid."));

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Certificate is verified successfully.")
                .setData("Certificate is verified successfully.");
    }

    private Certificate generateAndSaveCertificate(Long courseId, Long userId, String name, String profileImage) throws EntityNotFoundException {
        log.info("Generating and saving the certificate into the system...");
        CourseDetailForCertificate detailForCertificate = courseService.getCourseDetailForCertificate(courseId);
        Certificate certificate = Certificate.builder()
                .id(detailForCertificate.getCourseId())
                .uuid(UUID.randomUUID().toString())
                .courseId(detailForCertificate.getCourseId())
                .courseTitle(detailForCertificate.getCourseTitle())
                .coursePreview(detailForCertificate.getCoursePreviewImage())
                .courseDuration(detailForCertificate.getCourseDuration())
                .noOfSections(detailForCertificate.getTotalSections())
                .noOfTopics(detailForCertificate.getTotalTopics())
                .instructorId(detailForCertificate.getInstructorId())
                .instructorName(detailForCertificate.getInstructorName())
                .instructorProfileImage(detailForCertificate.getInstructorImage())
                .studentId(userId)
                .studentName(name)
                .studentProfileImage(profileImage)
                .certifiedOn(new Date())
                .build();
        return repo.save(certificate);
    }

    private CertificateResponse mappedTo(Certificate certificate, boolean forVerification) throws EntityNotFoundException {
        log.info("Fetching all courses reviews of an instructor and an specific course's reviews\n" +
                "mapped to the course's response object.");

        CertificateResponse response = CertificateResponse.from(certificate);
        if(forVerification)
            return response;

        Tuple allCoursesReviews = courseReviewService.fetchAllReviewsForAnInstructorCourses(certificate.getInstructorId());
        Long allCoursesTotalReviewers = (Long) allCoursesReviews.get("total_reviewers");
        Number allCoursesAvgReviews = (Number) allCoursesReviews.get("avg_reviews");

        Tuple courseReviews = courseReviewService.fetchAllReviewOfACourse(certificate.getCourseId());
        Long courseTotalReviewers = (Long) courseReviews.get("total_reviewers");
        Number courseAvgReviews = (Number) courseReviews.get("avg_reviews");

        response.setInstructorAvgReviews(allCoursesAvgReviews);
        response.setInstructorReviewers(allCoursesTotalReviewers);
        response.setCourseAvgReviews(courseAvgReviews);
        response.setCourseReviewers(courseTotalReviewers);

        CourseUrl courseUrl = courseUrlService.findActiveUrlByCourseIdAndStatus(certificate.getCourseId(), GenericStatus.ACTIVE);
        response.setCourseUrl(courseUrl.getUrl());

        return response;
    }

    @Override
    public ResponseEntity<byte[]> downloadCertificate(Long courseId, Boolean isDownloadable, String uuid, Principal principal)
            throws BadRequestException, EntityNotFoundException, IOException, IOException {
        CertificateResponse certificate = null;
        System.out.println("downloading certificate");

        if (StringUtils.isNotBlank(uuid)) {
            var savedCertificate = repo.findByUuid(uuid).orElse(null);
            if (savedCertificate != null) {
                certificate = CertificateResponse.from(savedCertificate);
            }else
                throw new EntityNotFoundException("Certificate not verified with provided uuid.");
        } else {
            Message<CertificateResponse> certificateData = generateCertificate(courseId, principal.getName());
            if (certificateData != null && certificateData.getStatus() == HttpStatus.OK.value()) {
                certificate = certificateData.getData();
            }
        }
        String name = certificate.getStudentName();
        String imageUrl;
        if (name.length() > 25) {
            imageUrl = "https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/fBAxWZFs_profile_image.jpeg";
        } else {
            imageUrl = "https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/1z1ztRTr_profile_image.jpeg";
        }

        Date date = certificate.getCertifiedAt();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");

        String formattedDate = sdf.format(date);

        BufferedImage image = ImageIO.read(new URL(imageUrl));

        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color myColor = new Color(33, 33, 137);
        g.setColor(myColor);
        g.setFont(new Font("Poppins", Font.BOLD, 40));


        if (name.length() > 25) {
            // Find the last space before the 35th character
            int splitPosition = name.lastIndexOf(' ', 25);
            if (splitPosition == -1) {
                // If there's no space before 35th character, find the next space after it
                splitPosition = name.indexOf(' ', 25);
                if (splitPosition == -1) {
                    // If no space at all, use the full length
                    splitPosition = name.length();
                }
            }

            // Split the text into two lines
            String line1 = name.substring(0, splitPosition);
            String line2 = name.substring(splitPosition).trim();

            // Draw the two lines of text
            g.drawString(line1, 66, 260);
            g.drawString(line2, 66, 310);
        } else {
            // Draw the single line of text
            g.drawString(name, 66, 280);
        }
        g.setColor(Color.BLACK);

        // Add course name below the name
        String courseName = certificate.getCourseTitle(); // You need to implement this method in your service
        Font font = new Font("Montserrat", Font.BOLD, 15);
        g.setFont(font);
        g.drawString(courseName, 66, 370);

        Font font1 = new Font("Poppins ", Font.PLAIN, 13);
        g.setFont(font1);
        Color myColor1 = new Color(33, 33, 137, 255);
        g.setColor(myColor1);

        String fullUrl = FRONTEND_DOMAIN_URL + "/student/verify-certificate/" + certificate.getUuid();

        int splitPosition = 50; // Customize this index based on your needs
        if (splitPosition > fullUrl.length()) {
            splitPosition = fullUrl.length(); // Ensure we don't go out of bounds
        }
        String part1 = fullUrl.substring(0, splitPosition);
        String part2 = fullUrl.substring(splitPosition);

        g.drawString(part1, 760, 622); // First part
        g.drawString(part2, 700, 640);

        g.drawString(formattedDate, 66, 210);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] modifiedImageBytes = baos.toByteArray();
        baos.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        if(isDownloadable)
            headers.setContentDispositionFormData("attachment", "image.png");

        return new ResponseEntity<>(modifiedImageBytes, headers, HttpStatus.OK);
    }
}
