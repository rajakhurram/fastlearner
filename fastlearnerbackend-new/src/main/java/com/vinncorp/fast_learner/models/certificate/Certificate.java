package com.vinncorp.fast_learner.models.certificate;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "certificate")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_title")
    private String courseTitle;

    @Column(name = "course_preview")
    private String coursePreview;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(name = "instructor_name", nullable = false)
    private String instructorName;

    @Column(name = "instructor_profile_image")
    private String instructorProfileImage;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "student_profile_image")
    private String studentProfileImage;

    @Column(name = "course_duration")
    private String courseDuration;

    @Column(name = "no_of_sections")
    private Long noOfSections;

    @Column(name = "no_of_topics")
    private Long noOfTopics;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date certifiedOn;
}
