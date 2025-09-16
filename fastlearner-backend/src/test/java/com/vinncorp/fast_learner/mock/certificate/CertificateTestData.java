package com.vinncorp.fast_learner.mock.certificate;

import com.vinncorp.fast_learner.models.certificate.Certificate;

import java.util.Date;

public class CertificateTestData {
    public static Certificate certificate() {
        return Certificate.builder()
                .courseId(1L)
                .courseTitle("test course")
                .coursePreview("coursePreview")
                .courseDuration("test duration")
                .id(1L)
                .instructorProfileImage("instructorProfileImageUrl")
                .instructorName("test instructor")
                .studentId(1L)
                .studentName("test student")
                .studentProfileImage("studentProfileImage")
                .certifiedOn(new Date())
                .noOfSections(1L)
                .noOfTopics(1L)
                .build();
    }
}
