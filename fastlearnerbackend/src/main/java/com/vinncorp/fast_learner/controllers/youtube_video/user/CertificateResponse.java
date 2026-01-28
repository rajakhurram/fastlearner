package com.vinncorp.fast_learner.controllers.youtube_video.user;


import com.vinncorp.fast_learner.models.certificate.Certificate;
import lombok.*;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateResponse {

    private Long id;
    private String uuid;
    private Long courseId;
    private String courseTitle;
    private String coursePreview;
    private Long instructorId;
    private String instructorName;
    private String instructorProfileImage;
    private Long studentId;
    private String studentName;
    private String studentProfileImage;

    private Long noOfSections;
    private Long noOfTopics;
    private String courseDuration;

    private Number instructorAvgReviews;
    private Long instructorReviewers;

    private Number courseAvgReviews;
    private Long courseReviewers;
    private Date certifiedAt;

    private String courseUrl;

    public static CertificateResponse from(Certificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .uuid(certificate.getUuid())
                .courseId(certificate.getCourseId())
                .courseTitle(certificate.getCourseTitle())
                .coursePreview(certificate.getCoursePreview())
                .courseDuration(certificate.getCourseDuration())
                .instructorId(certificate.getInstructorId())
                .instructorName(certificate.getInstructorName())
                .instructorProfileImage(certificate.getInstructorProfileImage())
                .studentId(certificate.getStudentId())
                .studentName(certificate.getStudentName())
                .studentProfileImage(certificate.getStudentProfileImage())
                .noOfSections(certificate.getNoOfSections())
                .noOfTopics(certificate.getNoOfTopics())
                .certifiedAt(certificate.getCertifiedOn())
                .build();
    }
}
